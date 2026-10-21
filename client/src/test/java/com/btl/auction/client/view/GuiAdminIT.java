package com.btl.auction.client.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

/** Run explicitly against a demo-seeded server, not in the default headless build. */
class GuiAdminIT {
    @Test
    void adminListsSeededDataAndLogoutReturnsToLogin() throws Exception {
        int port = Integer.getInteger("auction.gui.port", 5555);
        String password = System.getenv().getOrDefault("AUCTION_DEMO_PASSWORD", "DemoPass123!");
        Platform.startup(() -> { });
        Stage stage = fx(Stage::new);
        AppController app = fx(() -> {
            AppController controller = new AppController(stage, "127.0.0.1", port);
            controller.show("login");
            stage.show();
            return controller;
        });
        try {
            set(stage, "username", "demo_admin");
            set(stage, "password", "wrong-password");
            click(stage, "Đăng nhập");
            waitFor(() -> label(stage, "feedback").contains("AUTH_FAILED"), 15_000);
            assertTrue(title(stage).endsWith("login"));
            set(stage, "password", password);
            click(stage, "Đăng nhập");
            waitFor(() -> title(stage).endsWith("auctions"), 15_000);
            assertEquals("ADMIN", fx(() -> app.user().role().name()));
            click(stage, "Quản trị");
            waitFor(() -> title(stage).endsWith("admin")
                    && listSize(stage, "items") == 3 && listSize(stage, "auctions") == 3,
                    15_000);
            click(stage, "← Danh sách");
            waitFor(() -> title(stage).endsWith("auctions"), 10_000);
            click(stage, "Đăng xuất");
            waitFor(() -> title(stage).endsWith("login"), 10_000);
            assertNull(fx(app::user));
        } finally {
            fx(() -> {
                app.close();
                stage.close();
                return null;
            });
            Platform.exit();
        }
    }

    private static void set(Stage stage, String id, String value) throws Exception {
        fx(() -> {
            ((TextInputControl) node(stage, id)).setText(value);
            return null;
        });
    }

    private static void click(Stage stage, String text) throws Exception {
        fx(() -> {
            Button button = stage.getScene().getRoot().lookupAll(".button").stream()
                    .filter(node -> node instanceof Button candidate && candidate.getText().equals(text))
                    .map(node -> (Button) node).findFirst().orElseThrow();
            button.fire();
            return null;
        });
    }

    private static Node node(Stage stage, String id) {
        Node found = stage.getScene().getRoot().lookup("#" + id);
        if (found == null) {
            throw new IllegalStateException("Missing fx:id " + id);
        }
        return found;
    }

    private static String label(Stage stage, String id) throws Exception {
        return fx(() -> ((Label) node(stage, id)).getText());
    }

    private static int listSize(Stage stage, String id) throws Exception {
        return fx(() -> ((ListView<?>) node(stage, id)).getItems().size());
    }

    private static String title(Stage stage) throws Exception {
        return fx(stage::getTitle);
    }

    private static void waitFor(Callable<Boolean> condition, long timeoutMs) throws Exception {
        long deadline = System.nanoTime() + timeoutMs * 1_000_000;
        while (System.nanoTime() < deadline) {
            if (condition.call()) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("GUI did not reach expected state within " + timeoutMs + " ms");
    }

    private static <T> T fx(Callable<T> task) throws Exception {
        FutureTask<T> future = new FutureTask<>(task);
        Platform.runLater(future);
        return future.get(10, TimeUnit.SECONDS);
    }
}
