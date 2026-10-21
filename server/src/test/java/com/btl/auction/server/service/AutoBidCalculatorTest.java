package com.btl.auction.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.btl.auction.common.dto.AutoBidDto;
import java.util.List;
import org.junit.jupiter.api.Test;

class AutoBidCalculatorTest {
    @Test
    void higherCapWinsWithoutIterativeBidding() {
        AutoBidDto first = config(1, 11, 200, 10, 100);
        AutoBidDto second = config(2, 12, 180, 5, 101);
        var decision = AutoBidCalculator.next(List.of(second, first), 100, null);
        assertEquals(11, decision.bidder().bidderUserId());
        assertEquals(190, decision.amountVnd());
        assertNull(AutoBidCalculator.next(List.of(second, first), 190, 11L));
    }

    @Test
    void earlierRegistrationWinsEqualCapsAndNeverExceedsMaximum() {
        AutoBidDto early = config(1, 11, 200, 10, 100);
        AutoBidDto late = config(2, 12, 200, 50, 101);
        var decision = AutoBidCalculator.next(List.of(late, early), 110, 12L);
        assertEquals(11, decision.bidder().bidderUserId());
        assertEquals(200, decision.amountVnd());
        assertNull(AutoBidCalculator.next(List.of(early, late), 200, 11L));
    }

    @Test
    void saturatedAdditionCannotOverflowOrLoop() {
        AutoBidDto maximum = config(1, 11, Long.MAX_VALUE, Long.MAX_VALUE, 100);
        var decision = AutoBidCalculator.next(List.of(maximum), 100, null);
        assertEquals(Long.MAX_VALUE, decision.amountVnd());
    }

    private static AutoBidDto config(long id, long bidder, long max, long step, long registered) {
        return new AutoBidDto(id, 1, bidder, max, step, registered);
    }
}
