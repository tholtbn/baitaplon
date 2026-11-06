package com.auction.domain.exception;

/** Giá đấu vi phạm luật (thấp hơn giá hiện tại, seller tự đấu giá...). */
public class InvalidBidException extends DomainException {

    public InvalidBidException(String message) {
        super(message);
    }
}