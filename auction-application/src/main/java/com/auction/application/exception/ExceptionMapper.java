package com.auction.application.exception;

import com.auction.contract.vocab.ErrorCode;
import com.auction.domain.exception.AuctionClosedException;
import com.auction.domain.exception.DomainException;
import com.auction.domain.exception.IllegalStateTransitionException;
import com.auction.domain.exception.InvalidBidException;
import com.auction.domain.exception.ValidationException;

public final class ExceptionMapper {
    private ExceptionMapper() {}

    public static ErrorCode toErrorCode(Throwable error) {
        if (error instanceof ApplicationException e) return e.getErrorCode();
        if (error instanceof AuctionClosedException) return ErrorCode.AUCTION_CLOSED;
        if (error instanceof InvalidBidException) return ErrorCode.INVALID_BID;
        if (error instanceof IllegalStateTransitionException) return ErrorCode.INVALID_STATE;
        if (error instanceof ValidationException) return ErrorCode.VALIDATION;
        if (error instanceof DomainException) return ErrorCode.VALIDATION;
        if (error instanceof IllegalArgumentException) return ErrorCode.BAD_REQUEST;
        return ErrorCode.INTERNAL;
    }
}
