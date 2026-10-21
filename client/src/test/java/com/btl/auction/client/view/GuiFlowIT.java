package com.btl.auction.client.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.btl.auction.common.model.UserRole;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

/** Run explicitly against a real server with a temporary database. */
class GuiFlowIT {
    private static final String PASSWORD = "AuctionPass123";

    @Test
    void sellerAndTwoBiddersCompleteRealtimeAuctionThroughJavaFx() throws Exception {
        int port = Integer.getInteger("auction.gui.port", 5555);
        Platform.startup(() -> { });
        AppController seller = null;
        AppController bidderA = null;
        AppController bidderB = null;
        try {
            Stage sellerStage = fx(Stage::new);
            seller = fx(() -> open(sellerStage, port));
            String suffix = UUID.randomUUID().toString().substring(0, 8);
            register(sellerStage, "seller_" + suffix, UserRole.SELLER);
            login(sellerStage, "seller_" + suffix);
            click(sellerStage, "Quản lý sản phẩm");
            set(sellerStage, "name", "GUI demo " + suffix);
            set(sellerStage, "description", "Sản phẩm kiểm thử GUI");
            set(sellerStage, "price", "100");
            set(sellerStage, "detail", "Hãng thử nghiệm");
            click(sellerStage, "Thêm sản phẩm");
            waitFor(() -> listSize(sellerStage, "items") == 1, 15_000);
            fx(() -> { list(sellerStage, "items").getSelectionModel().select(0); return null; });
            long now = System.currentTimeMillis();
            set(sellerStage, "start", Ui.date(now + 8_000));
            set(sellerStage, "end", Ui.date(now + 45_000));
            click(sellerStage, "Tạo phiên");
            waitFor(() -> listSize(sellerStage, "ownAuctions") == 1, 15_000);

            Stage stageA = fx(Stage::new);
            bidderA = fx(() -> open(stageA, port));
            register(stageA, "bid_a_" + suffix, UserRole.BIDDER);
            login(stageA, "bid_a_" + suffix);
            openFirstAuction(stageA);

            Stage stageB = fx(Stage::new);
            bidderB = fx(() -> open(stageB, port));
            register(stageB, "bid_b_" + suffix, UserRole.BIDDER);
            login(stageB, "bid_b_" + suffix);
            openFirstAuction(stageB);

            waitFor(() -> label(stageA, "status").equals("RUNNING")
                    && label(stageB, "status").equals("RUNNING"), 20_000);
            set(stageA, "amount", "150");
            click(stageA, "Đặt giá");
            waitFor(() -> label(stageB, "currentPrice").contains("150"), 10_000);
            set(stageB, "amount", "200");
            click(stageB, "Đặt giá");
            waitFor(() -> label(stageA, "currentPrice").contains("200"), 10_000);
            waitFor(() -> chartPoints(stageA) == 2, 10_000);
            set(stageA, "maxBid", "250");
            set(stageA, "increment", "10");
            click(stageA, "Lưu auto-bid");
            waitFor(() -> label(stageB, "currentPrice").contains("210")
                    && chartPoints(stageB) == 3, 10_000);
            click(stageA, "Tắt auto-bid");
            waitFor(() -> label(stageA, "autoStatus").contains("chưa bật"), 10_000);
            waitFor(() -> label(stageA, "status").equals("FINISHED")
                    && label(stageB, "status").equals("FINISHED"), 100_000);
            click(stageA, "Xem màn kết quả");
            waitFor(() -> title(stageA).endsWith("result")
                    && label(stageA, "finalPrice").contains("210"), 10_000);
            assertEquals("FINISHED", label(stageA, "status"));
            assertTrue(label(stageA, "winner").contains("Bidder #"));
        } finally {
            AppController closeSeller = seller;
            AppController closeA = bidderA;
            AppController closeB = bidderB;
            fx(() -> {
                if (closeSeller != null) { closeSeller.close(); }
                if (closeA != null) { closeA.close(); }
                if (closeB != null) { closeB.close(); }
                for (javafx.stage.Window window : new java.util.ArrayList<>(
                        javafx.stage.Window.getWindows())) {
                    if (window instanceof Stage stage) { stage.close(); }
                }
                return null;
            });
            Platform.exit();
        }
    }

    private static AppController open(Stage stage, int port) {
        AppController app = new AppController(stage, "127.0.0.1", port);
        app.show("login");
        stage.show();
        return app;
    }

    private static void register(Stage stage, String username, UserRole role) throws Exception {
        click(stage, "Tạo tài khoản");
        set(stage, "username", username);
        set(stage, "displayName", username);
        set(stage, "password", PASSWORD);
        set(stage, "confirm", PASSWORD);
        fx(() -> { ((ChoiceBox<UserRole>) node(stage, "role")).setValue(role); return null; });
        click(stage, "Đăng ký");
        waitFor(() -> label(stage, "feedback").contains("Đăng ký thành công"), 15_000);
        click(stage, "Quay lại đăng nhập");
    }

    private static void login(Stage stage, String username) throws Exception {
        set(stage, "username", username);
        set(stage, "password", PASSWORD);
        click(stage, "Đăng nhập");
        waitFor(() -> title(stage).endsWith("auctions"), 15_000);
    }

    private static void openFirstAuction(Stage stage) throws Exception {
        waitFor(() -> listSize(stage, "auctions") > 0, 15_000);
        fx(() -> { list(stage, "auctions").getSelectionModel().select(0); return null; });
        click(stage, "Xem chi tiết");
        waitFor(() -> title(stage).endsWith("detail")
                && !label(stage, "currentPrice").isEmpty(), 15_000);
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

    private static void set(Stage stage, String id, String value) throws Exception {
        fx(() -> { ((TextInputControl) node(stage, id)).setText(value); return null; });
    }

    private static Node node(Stage stage, String id) {
        Node found = stage.getScene().getRoot().lookup("#" + id);
        if (found == null) { throw new IllegalStateException("Missing fx:id " + id); }
        return found;
    }

    private static ListView<?> list(Stage stage, String id) {
        return (ListView<?>) node(stage, id);
    }

    private static int listSize(Stage stage, String id) throws Exception {
        return fx(() -> list(stage, id).getItems().size());
    }

    private static int chartPoints(Stage stage) throws Exception {
        return fx(() -> ((LineChart<?, ?>) node(stage, "bidChart"))
                .getData().get(0).getData().size());
    }

    private static String label(Stage stage, String id) throws Exception {
        return fx(() -> ((Label) node(stage, id)).getText());
    }

    private static String title(Stage stage) throws Exception {
        return fx(stage::getTitle);
    }

    private static void waitFor(Callable<Boolean> condition, long timeoutMs) throws Exception {
        long deadline = System.nanoTime() + timeoutMs * 1_000_000;
        while (System.nanoTime() < deadline) {
            if (condition.call()) { return; }
            Thread.sleep(100);
        }
        throw new AssertionError("GUI did not reach expected state within " + timeoutMs + " ms");
    }

    private static <T> T fx(Callable<T> task) throws Exception {
        FutureTask<T> future = new FutureTask<>(task);
        Platform.runLater(future);
        return future.get(10, java.util.concurrent.TimeUnit.SECONDS);
    }
}
