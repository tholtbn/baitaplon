package com.auction.domain.exception;

import com.auction.contract.vocab.AuctionStatus;

/** Chuyển trạng thái phiên đấu giá không hợp lệ. */
public class IllegalStateTransitionException extends DomainException {

    public IllegalStateTransitionException(AuctionStatus from, AuctionStatus to) {
        super("Illegal auction status transition: " + from + " -> " + to);
    }

    public IllegalStateTransitionException(String message) {
        super(message);
    }
}