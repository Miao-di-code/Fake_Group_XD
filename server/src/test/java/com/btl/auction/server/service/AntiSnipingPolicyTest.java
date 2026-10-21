package com.btl.auction.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AntiSnipingPolicyTest {
    @Test
    void extendsAtInclusiveWindowStartAndRejectsOldEnd() {
        AntiSnipingPolicy rule = new AntiSnipingPolicy(10_000, 20_000);
        assertEquals(100_000, rule.endAfterAcceptedBid(89_999, 100_000));
        assertEquals(120_000, rule.endAfterAcceptedBid(90_000, 100_000));
        assertEquals(120_000, rule.endAfterAcceptedBid(99_999, 100_000));
        assertEquals(100_000, rule.endAfterAcceptedBid(100_000, 100_000));
        assertEquals(100_000, AntiSnipingPolicy.disabled()
                .endAfterAcceptedBid(99_999, 100_000));
    }

    @Test
    void rejectsHalfEnabledOrNegativePolicy() {
        assertThrows(IllegalArgumentException.class, () -> new AntiSnipingPolicy(10, 0));
        assertThrows(IllegalArgumentException.class, () -> new AntiSnipingPolicy(-1, 10));
    }
}
