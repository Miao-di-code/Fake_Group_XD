package com.btl.auction.client.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.parsers.DocumentBuilderFactory;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class FxmlContractTest {
    @Test
    void allScreensHaveControllersFieldsAndActions() throws Exception {
        for (String name : new String[] {"login", "register", "auctions", "detail", "seller", "admin", "result"}) {
            String path = "/view/" + name + ".fxml";
            try (InputStream input = getClass().getResourceAsStream(path)) {
                assertNotNull(input, path);
                var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
                String controllerName = document.getDocumentElement().getAttribute("fx:controller");
                Class<?> controller = Class.forName(controllerName);
                assertTrue(Screen.class.isAssignableFrom(controller), controllerName);
                NodeList nodes = document.getElementsByTagName("*");
                for (int index = 0; index < nodes.getLength(); index++) {
                    Element element = (Element) nodes.item(index);
                    String id = element.getAttribute("fx:id");
                    if (!id.isEmpty()) {
                        Field field = controller.getDeclaredField(id);
                        assertNotNull(field);
                    }
                    String action = element.getAttribute("onAction");
                    if (action.startsWith("#")) {
                        Method method = controller.getDeclaredMethod(action.substring(1));
                        assertNotNull(method);
                    }
                }
            }
        }
        assertNotNull(getClass().getResource("/view/app.css"));
    }

    @Test
    void javaFxLoadsEveryScreenWhenDesktopIsAvailable() throws Exception {
        assumeTrue(System.getProperty("os.name").startsWith("Windows")
                && !GraphicsEnvironment.isHeadless());
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.startup(() -> {
            try {
                for (String name : new String[] {"login", "register", "auctions", "detail", "seller", "admin", "result"}) {
                    assertNotNull(FXMLLoader.load(getClass().getResource("/view/" + name + ".fxml")));
                }
            } catch (Throwable ex) {
                failure.set(ex);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(30, TimeUnit.SECONDS));
        Platform.exit();
        if (failure.get() != null) {
            throw new AssertionError("FXML loading failed", failure.get());
        }
    }
}
