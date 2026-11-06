package com.auction.domain.model.auction;

import com.auction.contract.vocab.AuctionStatus;
import com.auction.domain.exception.AuctionClosedException;
import com.auction.domain.exception.IllegalStateTransitionException;
import com.auction.domain.exception.InvalidBidException;
import com.auction.domain.exception.ValidationException;
import com.auction.domain.model.Entity;
import com.auction.domain.model.Guard;
import com.auction.domain.rule.AuctionStateMachine;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Phiên đấu giá: giữ giá hiện tại, người dẫn đầu, lịch sử bid và trạng thái.
 *
 * <p>Lớp này <b>không thread-safe</b>. Tầng application phải tuần tự hóa các thao tác trên cùng
 * một phiên (khóa theo từng phiên) trước khi gọi {@link #placeBid}.
 */
public class Auction extends Entity {

    private final String itemId;
    private final String sellerId;
    private final BigDecimal startingPrice;
    private final Instant startTime;
    private final Instant endTime;
    private final List<BidTransaction> bids = new ArrayList<>();

    private AuctionStatus status = AuctionStatus.OPEN;
    private BigDecimal currentPrice;
    private String leaderId;

    /**
     * Tạo phiên mới ở trạng thái {@link AuctionStatus#OPEN}.
     *
     * @throws ValidationException nếu dữ liệu sai hoặc {@code endTime} không sau {@code startTime}
     */
    public Auction(
            String id,
            String itemId,
            String sellerId,
            BigDecimal startingPrice,
            Instant startTime,
            Instant endTime) {
        super(id);
        this.itemId = Guard.requireNotBlank(itemId, "itemId");
        this.sellerId = Guard.requireNotBlank(sellerId, "sellerId");
        this.startingPrice = Guard.requirePositive(startingPrice, "startingPrice");
        this.startTime = Guard.requireNonNull(startTime, "startTime");
        this.endTime = Guard.requireNonNull(endTime, "endTime");
        if (!endTime.isAfter(startTime)) {
            throw new ValidationException("endTime must be after startTime");
        }
        this.currentPrice = startingPrice;
    }

    /**
     * Đặt giá.
     *
     * @param bidderId người đặt giá
     * @param amount số tiền đặt
     * @param now thời điểm hiện tại (do tầng trên cung cấp qua Clock)
     * @return giao dịch đặt giá vừa được ghi nhận
     * @throws AuctionClosedException nếu phiên không RUNNING hoặc {@code now} ngoài khung giờ
     * @throws InvalidBidException nếu seller tự đấu giá hoặc giá không đủ cao
     */
    public BidTransaction placeBid(String bidderId, BigDecimal amount, Instant now) {
        Guard.requireNotBlank(bidderId, "bidderId");
        Guard.requireNonNull(amount, "amount");
        Guard.requireNonNull(now, "now");
        requireAcceptingBids(now);
        if (bidderId.equals(sellerId)) {
            throw new InvalidBidException("Seller cannot bid on their own auction");
        }
        requireAcceptableAmount(amount);

        BidTransaction bid = new BidTransaction(
                UUID.randomUUID().toString(), getId(), bidderId, amount, now);
        bids.add(bid);
        currentPrice = amount;
        leaderId = bidderId;
        return bid;
    }

    /**
     * OPEN → RUNNING.
     *
     * @throws IllegalStateTransitionException nếu sai trạng thái hoặc chưa tới giờ bắt đầu
     */
    public void start(Instant now) {
        Guard.requireNonNull(now, "now");
        AuctionStateMachine.requireTransition(status, AuctionStatus.RUNNING);
        if (now.isBefore(startTime)) {
            throw new IllegalStateTransitionException("Auction has not reached its start time");
        }
        status = AuctionStatus.RUNNING;
    }

    /**
     * RUNNING → FINISHED.
     *
     * @throws IllegalStateTransitionException nếu sai trạng thái hoặc chưa tới giờ kết thúc
     */
    public void finish(Instant now) {
        Guard.requireNonNull(now, "now");
        AuctionStateMachine.requireTransition(status, AuctionStatus.FINISHED);
        if (now.isBefore(endTime)) {
            throw new IllegalStateTransitionException("Auction has not reached its end time");
        }
        status = AuctionStatus.FINISHED;
    }

    /**
     * FINISHED → PAID. Chỉ phiên có người thắng mới thanh toán được.
     *
     * @throws IllegalStateTransitionException nếu sai trạng thái hoặc không có người thắng
     */
    public void markPaid() {
        AuctionStateMachine.requireTransition(status, AuctionStatus.PAID);
        if (leaderId == null) {
            throw new IllegalStateTransitionException("Auction without bids has no winner to pay");
        }
        status = AuctionStatus.PAID;
    }

    /**
     * Hủy phiên (từ OPEN, RUNNING hoặc FINISHED).
     *
     * @throws IllegalStateTransitionException nếu phiên đã PAID hoặc đã CANCELED
     */
    public void cancel() {
        AuctionStateMachine.requireTransition(status, AuctionStatus.CANCELED);
        status = AuctionStatus.CANCELED;
    }

    private void requireAcceptingBids(Instant now) {
        if (status != AuctionStatus.RUNNING) {
            throw new AuctionClosedException("Auction is not running, current status: " + status);
        }
        if (now.isBefore(startTime) || !now.isBefore(endTime)) {
            throw new AuctionClosedException("Auction is outside its bidding time window");
        }
    }

    private void requireAcceptableAmount(BigDecimal amount) {
        int comparison = amount.compareTo(currentPrice);
        if (bids.isEmpty() && comparison < 0) {
            throw new InvalidBidException(
                    "Bid must be at least the starting price " + startingPrice.toPlainString());
        }
        if (!bids.isEmpty() && comparison <= 0) {
            throw new InvalidBidException(
                    "Bid must be higher than the current price " + currentPrice.toPlainString());
        }
    }

    public String getItemId() {
        return itemId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /** Người đang dẫn đầu, rỗng nếu chưa có bid nào. */
    public Optional<String> getLeaderId() {
        return Optional.ofNullable(leaderId);
    }

    /** Người thắng cuộc: chỉ có giá trị khi phiên đã FINISHED hoặc PAID và có ít nhất một bid. */
    public Optional<String> getWinnerId() {
        boolean decided = status == AuctionStatus.FINISHED || status == AuctionStatus.PAID;
        return decided ? getLeaderId() : Optional.empty();
    }

    /** Lịch sử bid theo thứ tự thời gian (chỉ đọc). */
    public List<BidTransaction> getBids() {
        return Collections.unmodifiableList(bids);
    }

    @Override
    public String printInfo() {
        return "Auction " + getId() + " [" + status + "] item " + itemId + ", current price: "
                + currentPrice.toPlainString() + ", bids: " + bids.size();
    }
}