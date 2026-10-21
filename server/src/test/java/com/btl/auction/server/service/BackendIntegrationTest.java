package com.btl.auction.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.btl.auction.common.dto.AuctionDto;
import com.btl.auction.common.dto.ItemDto;
import com.btl.auction.common.model.AuctionStatus;
import com.btl.auction.common.model.ItemCategory;
import com.btl.auction.common.model.UserRole;
import com.btl.auction.common.protocol.ErrorCode;
import com.btl.auction.common.protocol.EventType;
import com.btl.auction.server.db.Database;
import com.btl.auction.server.db.DatabaseInitializer;
import com.btl.auction.server.db.Transactions;
import com.btl.auction.server.event.AuctionEventPublisher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackendIntegrationTest {
    @TempDir
    Path directory;

    private Database database;
    private MutableClock clock;
    private AuthService auth;
    private ProductService products;
    private AuctionService auctions;
    private AuctionEventPublisher events;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        database = DatabaseInitializer.initialize(directory.resolve("auction.db"));
        clock = new MutableClock(1_000_000);
        auth = new AuthService(database, clock);
        products = new ProductService(database);
        events = new AuctionEventPublisher();
        auctions = new AuctionService(database, clock, events);
    }

    @Test
    void registrationLoginSessionAndAdminRules() {
        var bidder = auth.register("bidder_1", "Bidder", "strong passphrase", UserRole.BIDDER);
        assertEquals(UserRole.BIDDER, bidder.role());
        assertEquals(ErrorCode.CONFLICT, code(() -> auth.register("bidder_1", "B", "password123",
                UserRole.BIDDER)));
        assertEquals(ErrorCode.FORBIDDEN, code(() -> auth.register("admin1", "Admin", "password123",
                UserRole.ADMIN)));
        assertEquals(ErrorCode.VALIDATION_ERROR, code(() -> auth.register("bad name", "B",
                "password123", UserRole.BIDDER)));
        assertThrows(AuthenticationException.class, () -> auth.login("bidder_1", "wrongpass"));
        var login = auth.login("bidder_1", "strong passphrase");
        assertEquals(bidder, auth.requireUser(login.token()));
        assertEquals(ErrorCode.FORBIDDEN, code(() -> auth.requireRole(login.token(), UserRole.SELLER)));
        auth.logout(login.token());
        assertEquals(ErrorCode.AUTH_REQUIRED, code(() -> auth.requireUser(login.token())));
        var second = auth.login("bidder_1", "strong passphrase");
        clock.set(1_000_000 + TimeUnit.HOURS.toMillis(8));
        assertEquals(ErrorCode.AUTH_REQUIRED, code(() -> auth.requireUser(second.token())));
        String stored = Transactions.read(database, connection -> {
            try (Statement sql = connection.createStatement();
                 ResultSet row = sql.executeQuery("SELECT password_hash FROM users")) {
                row.next();
                return row.getString(1);
            }
        });
        assertTrue(stored.startsWith("pbkdf2$"));
        assertFalse(stored.contains("strong passphrase"));
    }

    @Test
    void productOwnershipAndAuctionProtection() {
        long seller = user("seller_a", UserRole.SELLER);
        long other = user("seller_b", UserRole.SELLER);
        long admin = auth.createLocalAdmin("admin_a", "Admin", "password123").id();
        ItemDto item = products.createItem(seller, ItemCategory.ART, "Painting", "Oil", 100, "A. Artist");
        assertEquals(1, products.listOwnItems(seller).size());
        assertEquals(ErrorCode.FORBIDDEN, code(() -> products.updateItem(other, item.id(),
                "Changed", "Desc", 100, "Detail")));
        assertEquals(ErrorCode.FORBIDDEN, code(() -> products.deleteItem(other, item.id())));
        assertEquals("Changed", products.updateItem(admin, item.id(), "Changed", "Desc", 120,
                "Detail").name());
        assertEquals(120, products.getItem(item.id()).startingPriceVnd());
        auctions.createAuction(seller, item.id(), clock.millis() + 100, clock.millis() + 1000);
        assertEquals(ErrorCode.CONFLICT, code(() -> products.deleteItem(seller, item.id())));
        assertEquals(ErrorCode.CONFLICT, code(() -> products.updateItem(admin, item.id(),
                "Again", "Desc", 130, "Detail")));
        ItemDto spare = products.createItem(seller, ItemCategory.ELECTRONICS, "Radio", "Old", 10,
                "Maker");
        products.deleteItem(admin, spare.id());
        assertEquals(ErrorCode.NOT_FOUND, code(() -> products.getItem(spare.id())));
    }

    @Test
    void biddingTransitionsWinnerAndFinalization() {
        long seller = user("seller_c", UserRole.SELLER);
        long bidder = user("bidder_c", UserRole.BIDDER);
        long second = user("bidder_d", UserRole.BIDDER);
        AuctionDto auction = newAuction(seller, 1_000_100, 1_001_000);
        List<EventType> seen = new ArrayList<>();
        events.subscribe(auction.id(), event -> seen.add(event.type()));
        assertEquals(ErrorCode.AUCTION_NOT_RUNNING, code(() -> auctions.placeBid(bidder,
                auction.id(), 101)));
        clock.set(1_000_100);
        assertEquals(AuctionStatus.RUNNING, auctions.getAuction(auction.id()).status());
        assertEquals(ErrorCode.BID_TOO_LOW, code(() -> auctions.placeBid(bidder, auction.id(), -1)));
        assertEquals(ErrorCode.BID_TOO_LOW, code(() -> auctions.placeBid(bidder, auction.id(), 100)));
        assertEquals(ErrorCode.CONFLICT, code(() -> auctions.markPaid(seller, auction.id())));
        var first = auctions.placeBid(bidder, auction.id(), 101);
        var next = auctions.placeBid(second, auction.id(), 150);
        assertEquals(150, next.auction().currentPriceVnd());
        assertEquals(second, next.auction().leaderUserId());
        assertEquals(2, auctions.getBids(auction.id()).size());
        assertTrue(first.bid().id() < next.bid().id());
        clock.set(1_001_000);
        assertEquals(ErrorCode.AUCTION_NOT_RUNNING, code(() -> auctions.placeBid(bidder,
                auction.id(), 151)));
        assertEquals(AuctionStatus.FINISHED, auctions.getAuction(auction.id()).status());
        assertEquals(second, auctions.getAuction(auction.id()).leaderUserId());
        assertEquals(AuctionStatus.PAID, auctions.markPaid(seller, auction.id()).status());
        assertEquals(ErrorCode.CONFLICT, code(() -> auctions.markPaid(seller, auction.id())));
        assertEquals(ErrorCode.CONFLICT, code(() -> auctions.cancel(seller, auction.id())));
        assertEquals(List.of(EventType.AUCTION_STARTED, EventType.BID_UPDATED,
                EventType.BID_UPDATED, EventType.AUCTION_FINISHED), seen);
    }

    @Test
    void noBidMeansNoWinnerAndCancellation() {
        long seller = user("seller_d", UserRole.SELLER);
        AuctionDto auction = newAuction(seller, 1_000_100, 1_000_200);
        clock.set(1_000_200);
        assertEquals(2, auctions.closeExpired(clock.millis()));
        assertNull(auctions.getAuction(auction.id()).leaderUserId());
        assertEquals(ErrorCode.CONFLICT, code(() -> auctions.markPaid(seller, auction.id())));
        assertEquals(AuctionStatus.CANCELED, auctions.cancel(seller, auction.id()).status());
    }

    @Test
    void concurrentBidsHaveOneConsistentLeaderAndNoLostHistory() throws Exception {
        long seller = user("seller_e", UserRole.SELLER);
        long first = user("bidder_e", UserRole.BIDDER);
        long second = user("bidder_f", UserRole.BIDDER);
        AuctionDto auction = newAuction(seller, clock.millis(), clock.millis() + 1000);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorServiceHolder pool = new ExecutorServiceHolder(2)) {
            Future<?> a = pool.executor.submit(() -> bidAfter(start, first, auction.id(), 110));
            Future<?> b = pool.executor.submit(() -> bidAfter(start, second, auction.id(), 120));
            start.countDown();
            a.get();
            b.get();
        }
        AuctionDto result = auctions.getAuction(auction.id());
        assertEquals(120, result.currentPriceVnd());
        assertEquals(second, result.leaderUserId());
        List<com.btl.auction.common.dto.BidDto> history = auctions.getBids(auction.id());
        assertEquals(120, history.get(history.size() - 1).amountVnd());
        clock.set(1_001_000);
        assertEquals(second, auctions.getAuction(auction.id()).leaderUserId());
    }

    @Test
    void manyThreadsDoNotLoseAcceptedBids() throws Exception {
        long seller = user("seller_f", UserRole.SELLER);
        long bidder = user("bidder_g", UserRole.BIDDER);
        AuctionDto auction = newAuction(seller, clock.millis(), clock.millis() + 1000);
        AtomicInteger accepted = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorServiceHolder pool = new ExecutorServiceHolder(8)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                long amount = 101 + i;
                futures.add(pool.executor.submit(() -> {
                    try {
                        start.await();
                        auctions.placeBid(bidder, auction.id(), amount);
                        accepted.incrementAndGet();
                    } catch (ServiceException ex) {
                        assertEquals(ErrorCode.BID_TOO_LOW, ex.getCode());
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ex);
                    }
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        }
        assertEquals(124, auctions.getAuction(auction.id()).currentPriceVnd());
        assertEquals(accepted.get(), auctions.getBids(auction.id()).size());
    }

    @Test
    void databaseFailureRollsBackBidAndDoesNotPublish() throws SQLException {
        long seller = user("seller_g", UserRole.SELLER);
        long bidder = user("bidder_h", UserRole.BIDDER);
        AuctionDto auction = newAuction(seller, clock.millis(), clock.millis() + 1000);
        AtomicInteger eventsSeen = new AtomicInteger();
        events.subscribe(auction.id(), event -> eventsSeen.incrementAndGet());
        try (Connection connection = database.openConnection(); Statement sql = connection.createStatement()) {
            sql.execute("CREATE TRIGGER fail_price BEFORE UPDATE OF current_price_vnd ON auctions "
                    + "BEGIN SELECT RAISE(ABORT, 'forced failure'); END");
        }
        assertEquals(ErrorCode.INTERNAL_ERROR, code(() -> auctions.placeBid(bidder,
                auction.id(), 150)));
        assertEquals(0, auctions.getBids(auction.id()).size());
        assertEquals(100, auctions.getAuction(auction.id()).currentPriceVnd());
        assertEquals(0, eventsSeen.get());
    }

    @Test
    void snapshotRoundTripKeepsRelatedRowsAndRejectsOverwrite() throws Exception {
        long seller = user("seller_h", UserRole.SELLER);
        long bidder = user("bidder_i", UserRole.BIDDER);
        AuctionDto auction = newAuction(seller, clock.millis(), clock.millis() + 1000);
        auctions.placeBid(bidder, auction.id(), 210);
        Path json = directory.resolve("snapshot.json");
        assertThrows(IOException.class, () -> new SnapshotService(database)
                .exportSnapshot(directory.resolve("auction.db")));
        new SnapshotService(database).exportSnapshot(json);
        assertFalse(Files.readString(json).contains("password123"));
        Path restoredPath = directory.resolve("restored.db");
        Database restored = SnapshotService.restoreToNewDatabase(json, restoredPath);
        int userCount = Transactions.read(restored, connection -> count(connection, "users"));
        int itemCount = Transactions.read(restored, connection -> count(connection, "items"));
        int auctionCount = Transactions.read(restored, connection -> count(connection, "auctions"));
        int bidCount = Transactions.read(restored, connection -> count(connection, "bids"));
        assertEquals(2, userCount);
        assertEquals(1, itemCount);
        assertEquals(1, auctionCount);
        assertEquals(1, bidCount);
        assertEquals(210, new AuctionService(restored, clock, new AuctionEventPublisher())
                .getAuction(auction.id()).currentPriceVnd());
        assertThrows(IOException.class, () -> SnapshotService.restoreToNewDatabase(json, restoredPath));
    }

    @Test
    void observerDoesNotHoldAuctionLock() throws Exception {
        long seller = user("seller_i", UserRole.SELLER);
        long bidder = user("bidder_j", UserRole.BIDDER);
        AuctionDto auction = newAuction(seller, clock.millis(), clock.millis() + 1000);
        CountDownLatch observing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        List<Long> observedPrices = new CopyOnWriteArrayList<>();
        events.subscribe(auction.id(), event -> {
            if (event.type() == EventType.BID_UPDATED) {
                long amount = event.payload().getAsJsonObject("bid").get("amountVnd").getAsLong();
                observedPrices.add(amount);
                if (amount != 110) {
                    return;
                }
                observing.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        try (ExecutorServiceHolder pool = new ExecutorServiceHolder(2)) {
            Future<?> first = pool.executor.submit(() -> auctions.placeBid(bidder, auction.id(), 110));
            assertTrue(observing.await(5, TimeUnit.SECONDS));
            Future<?> second = pool.executor.submit(() -> auctions.placeBid(bidder, auction.id(), 120));
            assertEquals(120, ((AuctionService.BidResult) second.get(3, TimeUnit.SECONDS))
                    .auction().currentPriceVnd());
            release.countDown();
            first.get(3, TimeUnit.SECONDS);
            assertEquals(List.of(110L, 120L), observedPrices);
        } finally {
            release.countDown();
        }
    }

    @Test
    void unauthorizedAuctionActionsAndDatabaseReopen() {
        long seller = user("seller_j", UserRole.SELLER);
        long outsider = user("seller_k", UserRole.SELLER);
        long bidder = user("bidder_k", UserRole.BIDDER);
        ItemDto item = products.createItem(seller, ItemCategory.VEHICLE, "Car", "Old", 100,
                "Brand");
        assertEquals(ErrorCode.FORBIDDEN, code(() -> auctions.createAuction(outsider, item.id(),
                clock.millis(), clock.millis() + 1000)));
        AuctionDto auction = auctions.createAuction(seller, item.id(), clock.millis(),
                clock.millis() + 1000);
        assertEquals(ErrorCode.FORBIDDEN, code(() -> auctions.placeBid(seller, auction.id(), 110)));
        auctions.placeBid(bidder, auction.id(), 110);
        clock.set(1_001_000);
        assertEquals(ErrorCode.FORBIDDEN, code(() -> auctions.markPaid(outsider, auction.id())));
        assertEquals(110, new AuctionService(new Database(directory.resolve("auction.db")), clock,
                new AuctionEventPublisher()).getAuction(auction.id()).currentPriceVnd());
    }

    private static int count(Connection connection, String table) throws SQLException {
        try (Statement sql = connection.createStatement();
             ResultSet result = sql.executeQuery("SELECT COUNT(*) FROM " + table)) {
            result.next();
            return result.getInt(1);
        }
    }

    private void bidAfter(CountDownLatch latch, long bidder, long auction, long amount) {
        try {
            latch.await();
            auctions.placeBid(bidder, auction, amount);
        } catch (ServiceException ex) {
            assertEquals(ErrorCode.BID_TOO_LOW, ex.getCode());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }

    private AuctionDto newAuction(long seller, long start, long end) {
        ItemDto item = products.createItem(seller, ItemCategory.ART, "Painting", "Oil", 100,
                "Artist");
        return auctions.createAuction(seller, item.id(), start, end);
    }

    private long user(String username, UserRole role) {
        return auth.register(username, username, "password123", role).id();
    }

    private ErrorCode code(Runnable action) {
        return assertThrows(ServiceException.class, action::run).getCode();
    }

    private static final class MutableClock extends Clock {
        private final AtomicLong millis;

        private MutableClock(long now) {
            millis = new AtomicLong(now);
        }

        void set(long now) {
            millis.set(now);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis.get());
        }
    }

    private static final class ExecutorServiceHolder implements AutoCloseable {
        private final ExecutorService executor;

        private ExecutorServiceHolder(int threads) {
            executor = Executors.newFixedThreadPool(threads);
        }

        @Override
        public void close() {
            executor.shutdownNow();
        }
    }
}
