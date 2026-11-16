package com.auction.application.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/** Cấp một khóa riêng cho mỗi phiên đấu giá; các phiên khác nhau không chặn nhau. */
public final class AuctionLockRegistry {
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();
   /**
     +     * Lấy (hoặc tạo mới) khóa của một phiên đấu giá. Cùng một {@code auctionId} luôn trả về
     +     * cùng một khóa.
     +     *
     +     * @throws IllegalArgumentException nếu {@code auctionId} null hoặc rỗng
     +     */
    public ReentrantLock getLock(String auctionId) {
                if (auctionId == null || auctionId.isBlank()) {
                        throw new IllegalArgumentException("auctionId is required");
                    }
        return locks.computeIfAbsent(auctionId, ignored -> new ReentrantLock());
    }

    public int lockCount() {
                return locks.size();
            }
}