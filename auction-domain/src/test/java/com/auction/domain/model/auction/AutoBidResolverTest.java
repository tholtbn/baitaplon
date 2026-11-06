package com.auction.domain.model.auction;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class AutoBidResolverTest {

    private static final Instant START = Instant.parse("2026-01-01T19:00:00Z");
    private static final Instant END = Instant.parse("2026-01-01T20:00:00Z");
    private static final Instant NOW = Instant.parse("2026-01-01T19:30:00Z");

    private Auction runningAuction() {
        Auction auction = new Auction("a1", "i1", "seller", new BigDecimal("100"), START, END);
        auction.start(START);
        return auction;
    }

    private AutoBid auto(String id, String max, String inc, String registeredAt) {
        return new AutoBid(id, new BigDecimal(max), new BigDecimal(inc),
                Instant.parse(registeredAt));
    }

    @Test
    void earlierAutoBidWinsAndNeverExceedsMax() {
        Auction auction = runningAuction();
        auction.placeBid("manual", new BigDecimal("100"), NOW);
        AutoBid a = auto("A", "200", "10", "2026-01-01T19:10:00Z");
        AutoBid b = auto("B", "150", "5", "2026-01-01T19:20:00Z");

        List<BidTransaction> placed = AutoBidResolver.resolve(auction, List.of(b, a), NOW);

        assertThat(placed).extracting(BidTransaction::getAmount).containsExactly(
                new BigDecimal("110"), new BigDecimal("115"), new BigDecimal("125"),
                new BigDecimal("130"), new BigDecimal("140"), new BigDecimal("145"),
                new BigDecimal("155"));
        assertThat(auction.getLeaderId()).contains("A");
        assertThat(auction.getCurrentPrice()).isEqualByComparingTo("155");
    }

    @Test
    void autoBidStopsWhenNextStepWouldExceedMax() {
        Auction auction = runningAuction();
        auction.placeBid("manual", new BigDecimal("100"), NOW);
        AutoBid a = auto("A", "108", "10", "2026-01-01T19:10:00Z");

        assertThat(AutoBidResolver.resolve(auction, List.of(a), NOW)).isEmpty();
        assertThat(auction.getLeaderId()).contains("manual");
    }

    @Test
    void leaderDoesNotOutbidItself() {
        Auction auction = runningAuction();
        auction.placeBid("A", new BigDecimal("100"), NOW);
        AutoBid a = auto("A", "500", "10", "2026-01-01T19:10:00Z");

        assertThat(AutoBidResolver.resolve(auction, List.of(a), NOW)).isEmpty();
    }
}