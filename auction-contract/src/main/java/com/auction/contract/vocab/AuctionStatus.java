package com.auction.contract.vocab;

/**
 * Trạng thái của phiên đấu giá. Đây chỉ là từ vựng dùng chung giữa client và server;
 * luật chuyển trạng thái nằm ở domain (AuctionStateMachine).
 */
public enum AuctionStatus {
    /** Đã tạo, chưa tới giờ bắt đầu. */
    OPEN,
    /** Đang nhận giá đấu. */
    RUNNING,
    /** Đã hết giờ, đã xác định người thắng. */
    FINISHED,
    /** Người thắng đã thanh toán. */
    PAID,
    /** Phiên bị hủy. */
    CANCELED
}