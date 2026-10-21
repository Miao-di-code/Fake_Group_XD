package com.btl.auction.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.btl.auction.common.dto.AuctionDto;
import com.btl.auction.common.model.AuctionStatus;
import com.btl.auction.common.model.ItemCategory;
import com.btl.auction.common.model.UserRole;
import com.btl.auction.common.protocol.ErrorCode;
import com.btl.auction.common.protocol.EventType;
import com.btl.auction.server.db.Database;
import com.btl.auction.server.db.DatabaseInitializer;
import com.btl.auction.server.db.Transactions;
import com.btl.auction.server.event.AuctionEventPublisher;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AdvancedFeaturesIntegrationTest {
    @TempDir Path directory;
    private Database database;
    private MutableClock clock;
    private AuthService auth;
    private ProductService products;
    private AuctionEventPublisher publisher;

    @BeforeEach
    void setup() throws Exception {
        database = DatabaseInitializer.initialize(directory.resolve("auction.db"));
        clock = new MutableClock(1_000_000);
        auth = new AuthService(database, clock);
        products = new ProductService(database);
        publisher = new AuctionEventPublisher();
    }

    @Test
    void autoBidCapsPriorityAuthorizationAndHistoryRemainConsistent() {
        AuctionService service = service(AntiSnipingPolicy.disabled());
        long seller = user("seller_adv", UserRole.SELLER);
        long first = user("bidder_adv_a", UserRole.BIDDER);
        long second = user("bidder_adv_b", UserRole.BIDDER);
        long manual = user("bidder_adv_c", UserRole.BIDDER);
        AuctionDto auction = auction(service, seller, 1_060_000);
        assertEquals(ErrorCode.FORBIDDEN, code(() -> service.setAutoBid(seller,
                auction.id(), 200, 10)));
        assertEquals(ErrorCode.VALIDATION_ERROR, code(() -> service.setAutoBid(first,
                auction.id(), 100, 10)));
        assertEquals(110, service.setAutoBid(first, auction.id(), 200, 10)
                .auction().currentPriceVnd());
        assertEquals(190, service.setAutoBid(second, auction.id(), 180, 5)
                .auction().currentPriceVnd());
        assertEquals(first, service.getAuction(auction.id()).leaderUserId());
        assertNull(service.getAutoBid(manual, auction.id()));
        assertEquals(200, service.placeBid(manual, auction.id(), 195)
                .auction().currentPriceVnd());
        assertEquals(ErrorCode.BID_TOO_LOW, code(() -> service.placeBid(manual,
                auction.id(), 200)));
        assertEquals(201, service.placeBid(manual, auction.id(), 201)
                .auction().currentPriceVnd());
        assertEquals(manual, service.getAuction(auction.id()).leaderUserId());
        assertEquals(List.of(110L, 190L, 195L, 200L, 201L), service.getBids(auction.id())
                .stream().map(bid -> bid.amountVnd()).toList());
        service.removeAutoBid(second, auction.id());
        assertNull(service.getAutoBid(second, auction.id()));
    }

    @Test
    void equalMaxUsesEarlierAutoRegistrationAndPersistsAcrossRestartAndSnapshot()
            throws Exception {
        AuctionService service = service(AntiSnipingPolicy.disabled());
        long seller = user("seller_tie", UserRole.SELLER);
        long early = user("bidder_tie_a", UserRole.BIDDER);
        long late = user("bidder_tie_b", UserRole.BIDDER);
        AuctionDto auction = auction(service, seller, 1_060_000);
        service.setAutoBid(early, auction.id(), 200, 10);
        service.setAutoBid(late, auction.id(), 200, 20);
        assertEquals(200, service.getAuction(auction.id()).currentPriceVnd());
        assertEquals(early, service.getAuction(auction.id()).leaderUserId());
        Database reopened = DatabaseInitializer.initialize(database.path());
        AuctionService afterRestart = new AuctionService(reopened, clock,
                new AuctionEventPublisher());
        assertEquals(200, afterRestart.getAutoBid(early, auction.id()).maxBidVnd());
        int version = Transactions.read(reopened, connection -> {
            try (Statement sql = connection.createStatement();
                 ResultSet row = sql.executeQuery("PRAGMA user_version")) {
                row.next();
                return row.getInt(1);
            }
        });
        assertEquals(2, version);
        Path snapshot = directory.resolve("snapshot.json");
        new SnapshotService(reopened).exportSnapshot(snapshot);
        JsonObject document = JsonParser.parseString(Files.readString(snapshot)).getAsJsonObject();
        assertEquals(2, document.get("version").getAsInt());
        Database restored = SnapshotService.restoreToNewDatabase(snapshot,
                directory.resolve("restored.db"));
        int configurationCount = Transactions.read(restored, connection -> {
            try (Statement sql = connection.createStatement();
                 ResultSet row = sql.executeQuery("SELECT COUNT(*) FROM auto_bids")) {
                row.next();
                return row.getInt(1);
            }
        });
        assertEquals(2, configurationCount);
        document.addProperty("version", 1);
        document.remove("auto_bids");
        Files.writeString(directory.resolve("v1.json"), document.toString());
        Database oldRestored = SnapshotService.restoreToNewDatabase(directory.resolve("v1.json"),
                directory.resolve("restored-v1.db"));
        assertNull(new AuctionService(oldRestored, clock, new AuctionEventPublisher())
                .getAutoBid(early, auction.id()));
    }

    @Test
    void extensionUsesNewEndAndOnlyAcceptedBidBroadcastsIt() {
        AuctionService service = service(new AntiSnipingPolicy(10_000, 20_000));
        long seller = user("seller_snipe", UserRole.SELLER);
        long bidder = user("bidder_snipe", UserRole.BIDDER);
        AuctionDto auction = auction(service, seller, 1_060_000);
        List<EventType> events = new ArrayList<>();
        publisher.subscribe(auction.id(), event -> events.add(event.type()));
        clock.set(1_049_999);
        assertEquals(1_060_000, service.placeBid(bidder, auction.id(), 110)
                .auction().endAtEpochMs());
        clock.set(1_050_000);
        assertEquals(1_080_000, service.placeBid(bidder, auction.id(), 120)
                .auction().endAtEpochMs());
        assertEquals(ErrorCode.BID_TOO_LOW, code(() -> service.placeBid(bidder,
                auction.id(), 120)));
        assertEquals(1, events.stream().filter(type -> type == EventType.AUCTION_EXTENDED).count());
        clock.set(1_060_000);
        assertEquals(0, service.closeExpired(clock.millis()));
        assertEquals(AuctionStatus.RUNNING, service.getAuction(auction.id()).status());
        clock.set(1_080_000);
        assertEquals(AuctionStatus.FINISHED, service.getAuction(auction.id()).status());
        assertTrue(events.contains(EventType.AUCTION_FINISHED));
    }

    @Test
    void concurrentManualBidsAndAutoBidLeaveOnePersistedLeader() throws Exception {
        AuctionService service = service(AntiSnipingPolicy.disabled());
        long seller = user("seller_concurrent_adv", UserRole.SELLER);
        long proxy = user("proxy_concurrent_adv", UserRole.BIDDER);
        long challenger = user("manual_concurrent_adv", UserRole.BIDDER);
        AuctionDto auction = auction(service, seller, 1_060_000);
        service.setAutoBid(proxy, auction.id(), 200, 10);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (long amount = 120; amount <= 190; amount += 10) {
                long bidAmount = amount;
                futures.add(pool.submit(() -> {
                    start.await();
                    try {
                        service.placeBid(challenger, auction.id(), bidAmount);
                    } catch (ServiceException ex) {
                        assertEquals(ErrorCode.BID_TOO_LOW, ex.getCode());
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            pool.shutdownNow();
        }
        AuctionDto finalAuction = service.getAuction(auction.id());
        assertEquals(proxy, finalAuction.leaderUserId());
        assertTrue(finalAuction.currentPriceVnd() <= 200);
        List<Long> prices = service.getBids(auction.id()).stream()
                .map(bid -> bid.amountVnd()).toList();
        for (int index = 1; index < prices.size(); index++) {
            assertTrue(prices.get(index) > prices.get(index - 1));
        }
        assertEquals(finalAuction.currentPriceVnd(), prices.get(prices.size() - 1));
    }

    private AuctionService service(AntiSnipingPolicy policy) {
        return new AuctionService(database, clock, publisher, policy);
    }

    private AuctionDto auction(AuctionService service, long seller, long end) {
        long item = products.createItem(seller, ItemCategory.ART, "Art", "Description", 100,
                "Artist").id();
        return service.createAuction(seller, item, clock.millis(), end);
    }

    private long user(String username, UserRole role) {
        return auth.register(username, username, "password123", role).id();
    }

    private ErrorCode code(Runnable action) {
        return assertThrows(ServiceException.class, action::run).getCode();
    }

    private static final class MutableClock extends Clock {
        private final AtomicLong now;

        private MutableClock(long initial) {
            now = new AtomicLong(initial);
        }

        void set(long time) {
            now.set(time);
        }

        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(now.get()); }
    }
}
