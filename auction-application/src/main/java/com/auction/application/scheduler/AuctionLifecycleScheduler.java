package com.auction.application.scheduler;

import com.auction.application.concurrency.AuctionLockRegistry;
import com.auction.application.event.AuctionFinishedEvent;
import com.auction.application.port.AuctionRepository;
import com.auction.application.port.EventPublisher;
import com.auction.domain.model.auction.Auction;
import com.auction.domain.model.auction.AuctionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuctionLifecycleScheduler implements AutoCloseable {

    private static final Logger log =
            LoggerFactory.getLogger(AuctionLifecycleScheduler.class);

    private final AuctionRepository auctionRepository;
    private final AuctionLockRegistry lockRegistry;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();

    private final AtomicBoolean started = new AtomicBoolean(false);

    public AuctionLifecycleScheduler(
            AuctionRepository auctionRepository,
            AuctionLockRegistry lockRegistry,
            EventPublisher eventPublisher,
            Clock clock) {

        this.auctionRepository = auctionRepository;
        this.lockRegistry = lockRegistry;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            executor.scheduleAtFixedRate(
                    this::processAuctions,
                    0,
                    1,
                    TimeUnit.SECONDS
            );
        }
    }

    public void processAuctions() {
        Instant now = clock.instant();

        for (Auction auction : auctionRepository.findAll()) {
            processAuction(auction.getId(), now);
        }
    }

    private void processAuction(String auctionId, Instant now) {
        AuctionFinishedEvent finishedEvent = null;

        var lock = lockRegistry.getLock(auctionId);
        lock.lock();

        try {
            Auction auction = auctionRepository.findById(auctionId)
                    .orElse(null);

            if (auction == null) {
                return;
            }

            if (auction.getStatus() == AuctionStatus.OPEN
                    && !now.isBefore(auction.getStartTime())) {

                auction.start(now);
                auctionRepository.save(auction);
            }

            if (auction.getStatus() == AuctionStatus.RUNNING
                    && !now.isBefore(auction.getEndTime())) {

                auction.finish(now);
                auctionRepository.save(auction);

                finishedEvent = new AuctionFinishedEvent(
                        auction.getId(),
                        auction.getWinnerId().orElse(null),
                        auction.getCurrentPrice(),
                        clock.instant()
                );
            }

        } finally {
            lock.unlock();
        }

        if (finishedEvent != null) {
            eventPublisher.publish(finishedEvent);
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}