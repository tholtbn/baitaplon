package com.auction.domain.model.auction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auction.contract.vocab.AuctionStatus;
import com.auction.domain.exception.AuctionClosedException;
import com.auction.domain.exception.IllegalStateTransitionException;
import com.auction.domain.exception.InvalidBidException;
import com.auction.domain.exception.ValidationException;

import java.beans.Beans;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuctionTest {

    private static final Instant START = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant END = START.plusSeconds(3600);
    private static final Instant DURING = START.plusSeconds(60);

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static Auction openAuction() {
        return new Auction("a1", "item1", "seller1", money("100"), START, END);
    }

    private static Auction runningAuction() {
        Auction auction = openAuction();
        auction.start(START);
        return auction;
    }

    @Test
    void newAuctionIsOpenWithStartingPriceAndNoLeader() {
        Auction auction = openAuction();

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.OPEN);
        assertThat(auction.getCurrentPrice()).isEqualByComparingTo("100");
        assertThat(auction.getLeaderId()).isEmpty();
    }

    @Test
    void rejectsEndTimeNotAfterStartTime() {
        assertThatThrownBy(() -> new Auction("a1", "item1", "seller1", money("100"), START, START))
                .isInstanceOf(ValidationException.class);
    }

    private Beans assertThatThrownBy(Object o) {
    }

    @Test
    void cannotStartBeforeStartTime() {
        Auction auction = openAuction();

        assertThatThrownBy(() -> auction.start(START.minusSeconds(1)))
                .isInstanceOf(IllegalStateTransitionException.class);
    }

    @Test
    void firstBidAtStartingPriceIsAccepted() {
        Auction auction = runningAuction();

        BidTransaction bid = auction.placeBid("bidder1", money("100"), DURING);

        assertThat(bid.getAmount()).isEqualByComparingTo("100");
        assertThat(auction.getLeaderId()).contains("bidder1");
        assertThat(auction.getBids()).hasSize(1);
    }

    @Test
    void firstBidBelowStartingPriceIsRejected() {
        Auction auction = runningAuction();

        assertThatThrownBy(() -> auction.placeBid("bidder1", money("99"), DURING))
                .isInstanceOf(InvalidBidException.class);
    }

    @Test
    void higherBidReplacesLeaderAndPrice() {
        Auction auction = runningAuction();
        auction.placeBid("bidder1", money("100"), DURING);

        auction.placeBid("bidder2", money("150"), DURING.plusSeconds(1));

        assertThat(auction.getCurrentPrice()).isEqualByComparingTo("150");
        assertThat(auction.getLeaderId()).contains("bidder2");
        assertThat(auction.getBids()).hasSize(2);
    }

    @Test
    void bidEqualToCurrentPriceIsRejectedAfterFirstBid() {
        Auction auction = runningAuction();
        auction.placeBid("bidder1", money("100"), DURING);

        assertThatThrownBy(() -> auction.placeBid("bidder2", money("100"), DURING))
                .isInstanceOf(InvalidBidException.class);
        assertThat(auction.getLeaderId()).contains("bidder1");
    }

    @Test
    void sellerCannotBidOnOwnAuction() {
        Auction auction = runningAuction();

        assertThatThrownBy(() -> auction.placeBid("seller1", money("200"), DURING))
                .isInstanceOf(InvalidBidException.class);
    }

    @Test
    void cannotBidWhenAuctionIsNotRunning() {
        Auction auction = openAuction();

        assertThatThrownBy(() -> auction.placeBid("bidder1", money("200"), DURING))
                .isInstanceOf(AuctionClosedException.class);
    }

    @Test
    void cannotBidAtOrAfterEndTime() {
        Auction auction = runningAuction();

        assertThatThrownBy(() -> auction.placeBid("bidder1", money("200"), END))
                .isInstanceOf(AuctionClosedException.class);
    }

    @Test
    void cannotFinishBeforeEndTime() {
        Auction auction = runningAuction();

        assertThatThrownBy(() -> auction.finish(END.minusSeconds(1)))
                .isInstanceOf(IllegalStateTransitionException.class);
    }

    @Test
    void finishedAuctionExposesWinnerAndRejectsBids() {
        Auction auction = runningAuction();
        auction.placeBid("bidder1", money("120"), DURING);

        auction.finish(END);

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.FINISHED);
        assertThat(auction.getWinnerId()).contains("bidder1");
        assertThatThrownBy(() -> auction.placeBid("bidder2", money("500"), END))
                .isInstanceOf(AuctionClosedException.class);
    }

    @Test
    void winnerIsEmptyWhileAuctionIsStillRunning() {
        Auction auction = runningAuction();
        auction.placeBid("bidder1", money("120"), DURING);

        assertThat(auction.getWinnerId()).isEmpty();
    }

    @Test
    void markPaidMovesFinishedAuctionWithWinnerToPaid() {
        Auction auction = runningAuction();
        auction.placeBid("bidder1", money("120"), DURING);
        auction.finish(END);

        auction.markPaid();

        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.PAID);
    }

    @Test
    void markPaidFailsWhenNobodyBid() {
        Auction auction = runningAuction();
        auction.finish(END);

        assertThatThrownBy(auction::markPaid)
                .isInstanceOf(IllegalStateTransitionException.class);
    }

    @Test
    void canCancelFinishedAuctionButNotPaidOne() {
        Auction finished = runningAuction();
        finished.placeBid("bidder1", money("120"), DURING);
        finished.finish(END);
        finished.cancel();
        assertThat(finished.getStatus()).isEqualTo(AuctionStatus.CANCELED);

        Auction paid = runningAuction();
        paid.placeBid("bidder1", money("120"), DURING);
        paid.finish(END);
        paid.markPaid();
        assertThatThrownBy(paid::cancel).isInstanceOf(IllegalStateTransitionException.class);
    }

    @Test
    void bidHistoryIsReadOnly() {
        Auction auction = runningAuction();

        assertThatThrownBy(() -> auction.getBids().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}