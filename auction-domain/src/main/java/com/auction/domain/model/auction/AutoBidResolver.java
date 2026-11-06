package com.auction.domain.model.auction;

import com.auction.domain.model.Guard;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Giải quyết các auto-bid sau một bid mới. Lặp: người đăng ký sớm nhất (chưa dẫn đầu, còn đủ
 * maxBid) đặt giá {@code giá hiện tại + increment}, cho tới khi không ai còn bid được nữa.
 * Không bao giờ vượt maxBid. Tầng application phải gọi hàm này trong cùng khóa của phiên.
 */
public final class AutoBidResolver {

    private static final int MAX_STEPS = 1000;

    private static final Comparator<AutoBid> PRIORITY = Comparator
            .comparing(AutoBid::registeredAt)
            .thenComparing(AutoBid::maxBid, Comparator.reverseOrder())
            .thenComparing(AutoBid::bidderId);

    private AutoBidResolver() {}

    /** Trả về các bid tự động đã được đặt, theo thứ tự. */
    public static List<BidTransaction> resolve(
            Auction auction, Collection<AutoBid> autoBids, Instant now) {
        Guard.requireNonNull(auction, "auction");
        Guard.requireNonNull(autoBids, "autoBids");
        Guard.requireNonNull(now, "now");
        List<AutoBid> byPriority = autoBids.stream().sorted(PRIORITY).toList();
        List<BidTransaction> placed = new ArrayList<>();
        for (int step = 0; step < MAX_STEPS; step++) {
            Optional<AutoBid> next = nextBidder(auction, byPriority);
            if (next.isEmpty()) {
                break;
            }
            AutoBid bid = next.get();
            placed.add(auction.placeBid(bid.bidderId(), nextAmount(auction, bid), now));
        }
        return placed;
    }

    private static Optional<AutoBid> nextBidder(Auction auction, List<AutoBid> byPriority) {
        String leader = auction.getLeaderId().orElse(null);
        return byPriority.stream()
                .filter(a -> !a.bidderId().equals(leader))
                .filter(a -> !a.bidderId().equals(auction.getSellerId()))
                .filter(a -> a.canAfford(nextAmount(auction, a)))
                .findFirst();
    }

    private static BigDecimal nextAmount(Auction auction, AutoBid bid) {
        return auction.getBids().isEmpty()
                ? auction.getCurrentPrice()
                : auction.getCurrentPrice().add(bid.increment());
    }
}