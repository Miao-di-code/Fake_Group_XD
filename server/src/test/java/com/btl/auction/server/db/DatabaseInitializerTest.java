package com.btl.auction.server.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseInitializerTest {
    @TempDir
    Path tempDir;

    @Test
    void createsSchemaIdempotentlyWithForeignKeysEnabled() throws Exception {
        Path path = tempDir.resolve("auction.db");
        Database database = DatabaseInitializer.initialize(path);
        DatabaseInitializer.initialize(path);
        try (Connection connection = database.openConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet result = statement.executeQuery("SELECT count(*) FROM sqlite_master "
                    + "WHERE type='table' AND name IN ('users','items','auctions','bids')")) {
                result.next();
                assertEquals(4, result.getInt(1));
            }
            assertThrows(SQLException.class, () -> statement.executeUpdate(
                    "INSERT INTO items(seller_id,category,name,description,starting_price_vnd,"
                    + "detail_json) VALUES (999,'ART','A','B',100,'{}')"));
        }
    }

    @Test
    void upgradesExistingV1DatabaseWithoutLosingRows() throws Exception {
        Path path = tempDir.resolve("old.db");
        Database old = new Database(path);
        String schema;
        try (InputStream input = getClass().getResourceAsStream("/db/schema.sql")) {
            schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Connection connection = old.openConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : schema.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
            statement.executeUpdate("INSERT INTO users(username,display_name,password_hash,role,"
                    + "created_at_ms) VALUES ('old','Old','hash','BIDDER',1)");
            statement.execute("PRAGMA user_version=1");
        }
        Database upgraded = DatabaseInitializer.initialize(path);
        try (Connection connection = upgraded.openConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet users = statement.executeQuery("SELECT COUNT(*) FROM users")) {
                users.next();
                assertEquals(1, users.getInt(1));
            }
            try (ResultSet autoBids = statement.executeQuery("SELECT COUNT(*) FROM auto_bids")) {
                autoBids.next();
                assertEquals(0, autoBids.getInt(1));
            }
            try (ResultSet version = statement.executeQuery("PRAGMA user_version")) {
                version.next();
                assertEquals(2, version.getInt(1));
            }
        }
    }
}
