package com.auction.domain.exception;

/** Đặt giá khi phiên không ở trạng thái nhận giá hoặc ngoài khung thời gian. */
public class AuctionClosedException extends DomainException {

    public AuctionClosedException(String message) {
        super(message);
    }
}