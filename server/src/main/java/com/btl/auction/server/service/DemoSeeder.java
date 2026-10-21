package com.btl.auction.server.service;

import com.btl.auction.common.dto.ItemDto;
import com.btl.auction.common.dto.UserDto;
import com.btl.auction.common.model.ItemCategory;
import com.btl.auction.common.model.UserRole;
import com.btl.auction.server.dao.UserDao;
import com.btl.auction.server.db.Database;
import com.btl.auction.server.db.DatabaseInitializer;
import com.btl.auction.server.db.Transactions;
import java.nio.file.Path;
import java.time.Clock;

/** Local-only, repeatable sample data. Existing user credentials must match. */
public final class DemoSeeder {
    public record Result(int users, int items, int auctions) {
    }

    private DemoSeeder() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: DemoSeeder <database path>");
        }
        String password = System.getenv().getOrDefault("AUCTION_DEMO_PASSWORD", "DemoPass123!");
        Result result = seed(Path.of(args[0]), Clock.systemUTC(), password);
        System.out.println("Demo ready: " + result.users() + " users, " + result.items()
                + " items, " + result.auctions() + " auctions");
    }

    public static Result seed(Path path, Clock clock, String password) throws Exception {
        Database database = DatabaseInitializer.initialize(path);
        Clock seedingClock = Clock.fixed(clock.instant(), clock.getZone());
        AuthService auth = new AuthService(database, seedingClock);
        ProductService products = new ProductService(database);
        AuctionService auctions = new AuctionService(database, seedingClock,
                new com.btl.auction.server.event.AuctionEventPublisher());
        ensureUser(database, auth, "demo_admin", "Demo Admin", UserRole.ADMIN, password);
        UserDto seller = ensureUser(database, auth, "demo_seller", "Demo Seller",
                UserRole.SELLER, password);
        ensureUser(database, auth, "demo_bidder_a", "Demo Bidder A", UserRole.BIDDER, password);
        ensureUser(database, auth, "demo_bidder_b", "Demo Bidder B", UserRole.BIDDER, password);
        ItemDto laptop = ensureItem(products, seller.id(), ItemCategory.ELECTRONICS,
                "Demo Laptop", "Laptop used for the live auction", 1_000_000, "DemoTech");
        ItemDto painting = ensureItem(products, seller.id(), ItemCategory.ART,
                "Demo Painting", "Canvas painting for upcoming auction", 500_000, "Demo Artist");
        ItemDto bicycle = ensureItem(products, seller.id(), ItemCategory.VEHICLE,
                "Demo Bicycle", "Bicycle for upcoming auction", 2_000_000, "Demo Bikes");
        long now = seedingClock.millis();
        ensureAuction(auctions, seller.id(), laptop.id(), now, now + 30 * 60_000L);
        ensureAuction(auctions, seller.id(), painting.id(), now + 10 * 60_000L,
                now + 40 * 60_000L);
        ensureAuction(auctions, seller.id(), bicycle.id(), now + 15 * 60_000L,
                now + 45 * 60_000L);
        return new Result(4, 3, 3);
    }

    private static UserDto ensureUser(Database database, AuthService auth, String username,
                                      String display, UserRole role, String password) {
        UserDao.StoredUser existing = Transactions.read(database,
                connection -> new UserDao().findByUsername(connection, username));
        if (existing == null) {
            return role == UserRole.ADMIN ? auth.createLocalAdmin(username, display, password)
                    : auth.register(username, display, password, role);
        }
        UserDto verified = auth.login(username, password).user();
        if (verified.role() != role) {
            throw new IllegalStateException("Existing demo username has a different role: " + username);
        }
        return verified;
    }

    private static ItemDto ensureItem(ProductService products, long sellerId,
                                      ItemCategory category, String name, String description,
                                      long price, String detail) {
        return products.listOwnItems(sellerId).stream()
                .filter(item -> item.name().equals(name) && item.category() == category)
                .findFirst().orElseGet(() -> products.createItem(sellerId, category,
                        name, description, price, detail));
    }

    private static void ensureAuction(AuctionService auctions, long sellerId, long itemId,
                                      long start, long end) {
        if (auctions.listAuctions(null).stream().noneMatch(auction -> auction.itemId() == itemId)) {
            auctions.createAuction(sellerId, itemId, start, end);
        }
    }
}
