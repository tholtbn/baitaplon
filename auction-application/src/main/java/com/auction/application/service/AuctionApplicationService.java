package com.auction.application.service;

import com.auction.application.exception.ApplicationException;
import com.auction.application.port.out.*;
import com.auction.contract.dto.*;
import com.auction.contract.event.BidPlacedPayload;
import com.auction.contract.protocol.EventType;
import com.auction.contract.protocol.ServerEvent;
import com.auction.contract.request.*;
import com.auction.contract.vocab.*;
import com.auction.domain.model.auction.*;
import com.auction.domain.model.item.Item;
import com.auction.domain.model.user.User;
import com.auction.domain.rule.AntiSnipingPolicy;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public final class AuctionApplicationService extends AccessSupport {
    private final AuctionRepository auctions;
    private final ItemRepository items;
    private final AutoBidRepository autoBids;
    private final EventPublisher events;
    private final IdGenerator ids;
    private final Clock clock;
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public AuctionApplicationService(UserRepository users, AuctionRepository auctions,
                                     ItemRepository items, AutoBidRepository autoBids,
                                     EventPublisher events, IdGenerator ids, Clock clock) {
        super(users);
        this.auctions = auctions;
        this.items = items;
        this.autoBids = autoBids;
        this.events = events;
        this.ids = ids;
        this.clock = clock;
    }

    public AuctionDto create(String userId, CreateAuctionRequest request) {
        User seller = requireRole(userId, Role.SELLER);
        if (request == null || request.itemId() == null || request.itemId().isBlank()
                || request.startTime() == null || request.endTime() == null) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "Incomplete auction data");
        }
        Item item = items.findById(request.itemId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND, "Item not found"));
        if (!item.getSellerId().equals(seller.getId())) {
            throw new ApplicationException(ErrorCode.FORBIDDEN, "You do not own this item");
        }
        Duration window = seconds(request.snipingWindowSeconds());
        Duration extension = seconds(request.snipingExtensionSeconds());
        Auction auction = new Auction(ids.nextId(), item.getId(), seller.getId(),
                item.getStartingPrice(), request.startTime(), request.endTime(),
                new AntiSnipingPolicy(window, extension));
        Auction saved = auctions.save(auction);
        return DtoMapper.auction(saved, item);
    }

    public AuctionDto get(String auctionId) {
        Auction auction = findAuction(auctionId);
        Item item = items.findById(auction.getItemId()).orElse(null);
        return DtoMapper.auction(auction, item);
    }

    public List<AuctionDto> list() {
        return auctions.findAll().stream()
                .map(a -> DtoMapper.auction(a, items.findById(a.getItemId()).orElse(null)))
                .toList();
    }

    public List<AuctionDto> listMine(String userId) {
        User user = requireSellerOrAdmin(userId);
        return auctions.findBySellerId(user.getId()).stream()
                .map(a -> DtoMapper.auction(a, items.findById(a.getItemId()).orElse(null)))
                .toList();
    }

    public List<BidDto> bidHistory(String auctionId) {
        return findAuction(auctionId).getBids().stream().map(DtoMapper::bid).toList();
    }

    public List<BidDto> placeBid(String userId, PlaceBidRequest request) {
        requireRole(userId, Role.BIDDER);
        if (request == null || request.auctionId() == null || request.amount() == null) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "Auction id and bid amount are required");
        }
        return withLock(request.auctionId(), () -> {
            Auction auction = findAuction(request.auctionId());
            Instant now = clock.instant();
            if (auction.getStatus() == AuctionStatus.OPEN && !now.isBefore(auction.getStartTime())) {
                auction.start(now);
            }
            if (auction.getStatus() == AuctionStatus.RUNNING && !now.isBefore(auction.getEndTime())) {
                auction.finish(now);
                auctions.save(auction);
                publish(auction, EventType.AUCTION_FINISHED, DtoMapper.auction(auction,
                        items.findById(auction.getItemId()).orElse(null)));
                throw new ApplicationException(ErrorCode.AUCTION_CLOSED, "Auction has ended");
            }
            Instant oldEnd = auction.getEndTime();
            List<BidTransaction> placed = new ArrayList<>();
            placed.add(auction.placeBid(userId, request.amount(), now));
            List<AutoBid> registered = autoBids.findByAuctionId(auction.getId());
            placed.addAll(AutoBidResolver.resolve(auction, registered, now));
            auctions.save(auction);
            for (BidTransaction bid : placed) {
                BidPlacedPayload payload = new BidPlacedPayload(auction.getId(), DtoMapper.bid(bid),
                        auction.getCurrentPrice(), auction.getLeaderId().orElse(null), auction.getEndTime());
                events.publish(auction.getId(), ServerEvent.of(EventType.BID_PLACED, payload));
            }
            if (auction.getEndTime().isAfter(oldEnd)) {
                publish(auction, EventType.AUCTION_EXTENDED,
                        DtoMapper.auction(auction, items.findById(auction.getItemId()).orElse(null)));
            }
            return placed.stream().map(DtoMapper::bid).toList();
        });
    }

    public AutoBidDto setAutoBid(String userId, SetAutoBidRequest request) {
        requireRole(userId, Role.BIDDER);
        if (request == null || request.auctionId() == null || request.maxBid() == null
                || request.increment() == null) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "Incomplete auto-bid data");
        }
        return withLock(request.auctionId(), () -> {
            Auction auction = findAuction(request.auctionId());
            Instant now = clock.instant();
            if (auction.getStatus() != AuctionStatus.RUNNING || !now.isBefore(auction.getEndTime())) {
                throw new ApplicationException(ErrorCode.AUCTION_CLOSED, "Auction is not accepting auto-bids");
            }
            if (auction.getSellerId().equals(userId)) {
                throw new ApplicationException(ErrorCode.FORBIDDEN, "Seller cannot bid on own auction");
            }
            AutoBid autoBid = new AutoBid(userId, request.maxBid(), request.increment(), now);
            AutoBid saved = autoBids.save(auction.getId(), autoBid);
            return new AutoBidDto(auction.getId(), saved.bidderId(), saved.maxBid(),
                    saved.increment(), saved.registeredAt());
        });
    }

    public void cancel(String userId, AuctionIdRequest request) {
        if (request == null || request.auctionId() == null) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "auctionId is required");
        }
        User actor = requireSellerOrAdmin(userId);
        withLock(request.auctionId(), () -> {
            Auction auction = findAuction(request.auctionId());
            if (actor.getRole() != Role.ADMIN && !auction.getSellerId().equals(userId)) {
                throw new ApplicationException(ErrorCode.FORBIDDEN, "You do not own this auction");
            }
            auction.cancel();
            auctions.save(auction);
            publish(auction, EventType.AUCTION_CANCELED,
                    DtoMapper.auction(auction, items.findById(auction.getItemId()).orElse(null)));
            return null;
        });
    }

    public void pay(String userId, AuctionIdRequest request) {
        requireUser(userId);
        if (request == null || request.auctionId() == null) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "auctionId is required");
        }
        withLock(request.auctionId(), () -> {
            Auction auction = findAuction(request.auctionId());
            if (auction.getWinnerId().isEmpty() || !auction.getWinnerId().get().equals(userId)) {
                throw new ApplicationException(ErrorCode.FORBIDDEN, "Only the winner can pay");
            }
            auction.markPaid();
            auctions.save(auction);
            return null;
        });
    }

    public int startDueAuctions() {
        int changed = 0;
        Instant now = clock.instant();
        for (Auction auction : auctions.findAll()) {
            if (auction.getStatus() == AuctionStatus.OPEN && !now.isBefore(auction.getStartTime())) {
                boolean started = withLock(auction.getId(), () -> {
                    Auction current = findAuction(auction.getId());
                    if (current.getStatus() == AuctionStatus.OPEN && !now.isBefore(current.getStartTime())) {
                        current.start(now);
                        auctions.save(current);
                        publish(current, EventType.AUCTION_STARTED,
                                DtoMapper.auction(current, items.findById(current.getItemId()).orElse(null)));
                        return true;
                    }
                    return false;
                });
                if (started) changed++;
            }
        }
        return changed;
    }

    public int finishExpiredAuctions() {
        int changed = 0;
        Instant now = clock.instant();
        for (Auction auction : auctions.findAll()) {
            if (auction.getStatus() == AuctionStatus.RUNNING && !now.isBefore(auction.getEndTime())) {
                boolean finished = withLock(auction.getId(), () -> {
                    Auction current = findAuction(auction.getId());
                    if (current.getStatus() != AuctionStatus.RUNNING || now.isBefore(current.getEndTime())) return false;
                    current.finish(now);
                    auctions.save(current);
                    publish(current, EventType.AUCTION_FINISHED,
                            DtoMapper.auction(current, items.findById(current.getItemId()).orElse(null)));
                    return true;
                });
                if (finished) changed++;
            }
        }
        return changed;
    }

    private Auction findAuction(String id) {
        return auctions.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND, "Auction not found"));
    }

    private void publish(Auction auction, EventType type, Object payload) {
        events.publish(auction.getId(), ServerEvent.of(type, payload));
    }

    private static Duration seconds(Long value) {
        if (value == null) return Duration.ZERO;
        if (value < 0) throw new ApplicationException(ErrorCode.VALIDATION, "Anti-sniping duration cannot be negative");
        return Duration.ofSeconds(value);
    }

    private <T> T withLock(String auctionId, java.util.concurrent.Callable<T> action) {
        ReentrantLock lock = locks.computeIfAbsent(auctionId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return action.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ApplicationException(ErrorCode.INTERNAL, "Auction operation failed", e);
        } finally {
            lock.unlock();
        }
    }
}
