package com.auction.application.service;

import com.auction.application.concurrency.AuctionLockRegistry;
import com.auction.application.event.AuctionExtendedEvent;
import com.auction.application.event.BidPlacedEvent;
import com.auction.application.exception.NotFoundException;
import com.auction.application.exception.UnauthorizedException;
import com.auction.application.port.AuctionRepository;
import com.auction.application.port.EventPublisher;
import com.auction.application.port.IdGenerator;
import com.auction.domain.model.auction.Auction;
import com.auction.domain.model.auction.BidTransaction;
import com.auction.domain.model.user.User;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class BiddingService {

    private final AuctionRepository auctionRepository;
    private final AuctionLockRegistry lockRegistry;
    private final EventPublisher eventPublisher;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public BiddingService(
            AuctionRepository auctionRepository,
            AuctionLockRegistry lockRegistry,
            EventPublisher eventPublisher,
            IdGenerator idGenerator,
            Clock clock) {
        this.auctionRepository = auctionRepository;
        this.lockRegistry = lockRegistry;
        this.eventPublisher = eventPublisher;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    public BidTransaction placeBid(
            String auctionId,
            User bidder,
            BigDecimal amount) {

        if (bidder == null || !"BIDDER".equals(bidder.getRole().name())) {
            throw new UnauthorizedException("Bidder permission required");
        }

        List<Object> events = new ArrayList<>();
        BidTransaction result;

        ReentrantLock lock = lockRegistry.getLock(auctionId);
        lock.lock();

        try {
            Auction auction = auctionRepository.findById(auctionId)
                    .orElseThrow(() ->
                            new NotFoundException("Auction not found: " + auctionId));

            Instant now = clock.instant();
            Instant previousEndTime = auction.getEndTime();

            result = auction.placeBid(bidder.getId(), amount, now);

            auctionRepository.save(auction);

            events.add(new BidPlacedEvent(
                    auctionId,
                    result,
                    auction.getCurrentPrice(),
                    auction.getLeaderId().orElse(null),
                    auction.getEndTime(),
                    now
            ));

            if (!previousEndTime.equals(auction.getEndTime())) {
                events.add(new AuctionExtendedEvent(
                        auctionId,
                        previousEndTime,
                        auction.getEndTime(),
                        now
                ));
            }

        } finally {
            lock.unlock();
        }

        for (Object event : events) {
            eventPublisher.publish((com.auction.application.event.DomainEvent) event);
        }

        return result;
    }
}