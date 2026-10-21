package com.btl.auction.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class AuctionManagerTest {
    @Test
    void singletonSerializesTasksForSameAuction() throws Exception {
        AuctionManager manager = AuctionManager.getInstance();
        assertSame(manager, AuctionManager.getInstance());
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch secondEntered = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = executor.submit(() -> manager.withAuctionLock(1, () -> {
                entered.countDown();
                release.await();
                return "first";
            }));
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            Future<String> second = executor.submit(() -> {
                secondStarted.countDown();
                return manager.withAuctionLock(1, () -> {
                    secondEntered.countDown();
                    return "second";
                });
            });
            assertTrue(secondStarted.await(2, TimeUnit.SECONDS));
            assertFalse(secondEntered.await(100, TimeUnit.MILLISECONDS));
            assertFalse(second.isDone());
            release.countDown();
            assertEquals("first", first.get(2, TimeUnit.SECONDS));
            assertEquals("second", second.get(2, TimeUnit.SECONDS));
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }
}
