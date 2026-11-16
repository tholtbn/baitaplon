package com.auction.application.service;

import com.auction.application.concurrency.AuctionLockRegistry;
import com.auction.application.exception.NotFoundException;
import com.auction.application.exception.UnauthorizedException;
import com.auction.application.port.AutoBidRepository;
import com.auction.application.port.AuctionRepository;
import com.auction.application.port.IdGenerator;
import com.auction.domain.model.auction.Auction;
import com.auction.domain.model.auction.AutoBid;
import com.auction.domain.model.auction.AutoBidResolver;
import com.auction.domain.model.auction.BidTransaction;
import com.auction.domain.model.user.User;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class AutoBidService {

    private final AutoBidRepository autoBidRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionLockRegistry lockRegistry;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public AutoBidService(
            AutoBidRepository autoBidRepository,
            AuctionRepository auctionRepository,
            AuctionLockRegistry lockRegistry,
            IdGenerator idGenerator,
            Clock clock) {
        this.autoBidRepository = autoBidRepository;
        this.auctionRepository = auctionRepository;
        this.lockRegistry = lockRegistry;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    public void setAutoBid(
            User bidder,
            String auctionId,
            BigDecimal maxBid,
            BigDecimal increment) {

        if (bidder == null || !"BIDDER".equals(bidder.getRole().name())) {
            throw new UnauthorizedException("Bidder permission required");
        }

        ReentrantLock lock = lockRegistry.getLock(auctionId);
        lock.lock();

        try {
            Auction auction = auctionRepository.findById(auctionId)
                    .orElseThrow(() ->
                            new NotFoundException("Auction not found: " + auctionId));

            AutoBid autoBid = new AutoBid(
                    bidder.getId(),
                    maxBid,
                    increment,
                    clock.instant()
            );

            autoBidRepository.save(auctionId, autoBid);

        } finally {
            lock.unlock();
        }
    }

    public void removeAutoBid(User bidder, String auctionId) {
        if (bidder == null || !"BIDDER".equals(bidder.getRole().name())) {
            throw new UnauthorizedException("Bidder permission required");
        }

        ReentrantLock lock = lockRegistry.getLock(auctionId);
        lock.lock();

        try {
            autoBidRepository.delete(auctionId, bidder.getId());
        } finally {
            lock.unlock();
        }
    }

    public List<AutoBid> getAutoBids(String auctionId) {
        return autoBidRepository.findByAuctionId(auctionId);
    }

    public List<BidTransaction> resolveAfterBid(String auctionId) {
        ReentrantLock lock = lockRegistry.getLock(auctionId);
        lock.lock();

        try {
            Auction auction = auctionRepository.findById(auctionId)
                    .orElseThrow(() ->
                            new NotFoundException("Auction not found: " + auctionId));

            List<AutoBid> autoBids =
                    autoBidRepository.findByAuctionId(auctionId);

            List<BidTransaction> generatedBids =
                    AutoBidResolver.resolve(auction, autoBids, clock.instant());

            if (!generatedBids.isEmpty()) {
                auctionRepository.save(auction);
            }

            return generatedBids;

        } finally {
            lock.unlock();
        }
    }
}