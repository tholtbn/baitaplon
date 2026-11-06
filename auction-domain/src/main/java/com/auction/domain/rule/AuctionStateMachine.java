package com.auction.domain.rule;

import com.auction.contract.vocab.AuctionStatus;
import com.auction.domain.exception.IllegalStateTransitionException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Bảng chuyển trạng thái hợp lệ của phiên đấu giá:
 * OPEN → RUNNING → FINISHED → PAID, và CANCELED từ mọi trạng thái chưa kết thúc hẳn.
 */
public final class AuctionStateMachine {

    private static final Map<AuctionStatus, Set<AuctionStatus>> TRANSITIONS = buildTransitions();

    private AuctionStateMachine() {}

    private static Map<AuctionStatus, Set<AuctionStatus>> buildTransitions() {
        Map<AuctionStatus, Set<AuctionStatus>> map = new EnumMap<>(AuctionStatus.class);
        map.put(AuctionStatus.OPEN, EnumSet.of(AuctionStatus.RUNNING, AuctionStatus.CANCELED));
        map.put(AuctionStatus.RUNNING, EnumSet.of(AuctionStatus.FINISHED, AuctionStatus.CANCELED));
        map.put(AuctionStatus.FINISHED, EnumSet.of(AuctionStatus.PAID, AuctionStatus.CANCELED));
        map.put(AuctionStatus.PAID, EnumSet.noneOf(AuctionStatus.class));
        map.put(AuctionStatus.CANCELED, EnumSet.noneOf(AuctionStatus.class));
        return map;
    }

    /** Cho biết có được chuyển từ {@code from} sang {@code to} hay không. */
    public static boolean canTransition(AuctionStatus from, AuctionStatus to) {
        return TRANSITIONS.get(from).contains(to);
    }

    /**
     * Kiểm tra chuyển trạng thái.
     *
     * @throws IllegalStateTransitionException nếu việc chuyển không hợp lệ
     */
    public static void requireTransition(AuctionStatus from, AuctionStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateTransitionException(from, to);
        }
    }
}