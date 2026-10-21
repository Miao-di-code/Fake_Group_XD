package com.btl.auction.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.btl.auction.common.model.AuctionStatus;
import com.btl.auction.server.db.Database;
import com.btl.auction.server.db.DatabaseInitializer;
import com.btl.auction.server.db.Transactions;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DemoSeederTest {
    @TempDir Path directory;

    @Test
    void createsAllDemoRolesItemsAndSessionsExactlyOnceWithHashedPassword() throws Exception {
        Path path = directory.resolve("demo.db");
        assertEquals(new DemoSeeder.Result(4, 3, 3),
                DemoSeeder.seed(path, Clock.systemUTC(), "DemoPass123!"));
        DemoSeeder.seed(path, Clock.systemUTC(), "DemoPass123!");
        Database database = DatabaseInitializer.initialize(path);
        assertEquals(4, count(database, "users"));
        assertEquals(3, count(database, "items"));
        assertEquals(3, count(database, "auctions"));
        AuctionService auctions = new AuctionService(database, Clock.systemUTC(),
                new com.btl.auction.server.event.AuctionEventPublisher());
        assertEquals(1, auctions.listAuctions(null).stream()
                .filter(auction -> auction.status() == AuctionStatus.RUNNING).count());
        assertEquals(2, auctions.listAuctions(null).stream()
                .filter(auction -> auction.status() == AuctionStatus.OPEN).count());
        AuthService auth = new AuthService(database, Clock.systemUTC());
        assertEquals("ADMIN", auth.login("demo_admin", "DemoPass123!").user().role().name());
        String storedHash = Transactions.read(database, connection -> {
            try (Statement sql = connection.createStatement();
                 ResultSet row = sql.executeQuery("SELECT password_hash FROM users "
                         + "WHERE username='demo_admin'")) {
                row.next();
                return row.getString(1);
            }
        });
        assertNotEquals("DemoPass123!", storedHash);
        assertFalse(storedHash.contains("DemoPass123!"));
    }

    private int count(Database database, String table) {
        return Transactions.read(database, connection -> {
            try (Statement sql = connection.createStatement();
                 ResultSet row = sql.executeQuery("SELECT COUNT(*) FROM " + table)) {
                row.next();
                return row.getInt(1);
            }
        });
    }
}
