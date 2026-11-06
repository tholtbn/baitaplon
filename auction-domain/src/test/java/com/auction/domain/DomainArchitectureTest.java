package com.auction.domain.model.auction;

import static org.assertj.core.api.Assertions.assertThat;

import com.auction.domain.rule.AntiSnipingPolicy;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuctionAntiSnipingTest {

    private static final Instant START = Instant.parse("2026-01-01T19:00:00Z");
    private static final Instant END = Instant.parse("2026-01-01T20:00:00Z");
    private static final AntiSnipingPolicy POLICY =
            new AntiSnipingPolicy(Duration.ofSeconds(10), Duration.ofSeconds(60));

    private Auction running(AntiSnipingPolicy policy) {
        Auction auction = new Auction(
                "a1", "i1", "seller", new BigDecimal("100"), START, END, policy);
        auction.start(START);
        return auction;
    }

    @Test
    void bidInsideWindowExtendsEndTime() {
        Auction auction = running(POLICY);
        auction.placeBid("b1", new BigDecimal("100"), Instant.parse("2026-01-01T19:59:50Z"));
        assertThat(auction.getEndTime()).isEqualTo(Instant.parse("2026-01-01T20:01:00Z"));
    }

    @Test
    void bidOutsideWindowDoesNotExtend() {
        Auction auction = running(POLICY);
        auction.placeBid("b1", new BigDecimal("100"), Instant.parse("2026-01-01T19:59:00Z"));
        assertThat(auction.getEndTime()).isEqualTo(END);
    }

    @Test
    void disabledPolicyNeverExtends() {
        Auction auction = running(AntiSnipingPolicy.disabled());
        auction.placeBid("b1", new BigDecimal("100"), Instant.parse("2026-01-01T19:59:59Z"));
        assertThat(auction.getEndTime()).isEqualTo(END);
    }

    @Test
    void bidIsAcceptedInExtendedPeriod() {
        Auction auction = running(POLICY);
        auction.placeBid("b1", new BigDecimal("100"), Instant.parse("2026-01-01T19:59:50Z"));
        auction.placeBid("b2", new BigDecimal("110"), Instant.parse("2026-01-01T20:00:30Z"));
        assertThat(auction.getLeaderId()).contains("b2");
    }
}