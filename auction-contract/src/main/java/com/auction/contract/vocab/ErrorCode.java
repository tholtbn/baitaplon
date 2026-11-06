package com.auction.contract.vocab;

/** Mã lỗi gửi về client. Server map từ exception sang mã này. */
public enum ErrorCode {
    BAD_REQUEST,       // JSON hỏng, thiếu trường
    VALIDATION,        // ValidationException
    INVALID_BID,       // InvalidBidException
    AUCTION_CLOSED,    // AuctionClosedException
    INVALID_STATE,     // IllegalStateTransitionException
    UNAUTHENTICATED,   // chưa đăng nhập / token sai
    FORBIDDEN,         // sai vai trò hoặc không sở hữu
    NOT_FOUND,
    CONFLICT,          // trùng username...
    INTERNAL           // lỗi server/DB, không lộ chi tiết
}