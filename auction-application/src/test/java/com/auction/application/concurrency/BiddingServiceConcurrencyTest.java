package com.auction.application.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import com.auction.application.event.EventBus;
import com.auction.application.port.AuctionRepository;
import com.auction.application.service.BiddingService;
import com.auction.domain.model.auction.Auction;
import com.auction.domain.model.auction.BidTransaction;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class BiddingServiceConcurrencyTest {
    @Test
    void concurrentBidsAreSerializedPerAuction() throws Exception {
        Instant now = Instant.parse("2026-11-15T10:00:00Z");
        Auction auction = new Auction("a1", "i1", "seller", new BigDecimal("100"),
                now.minusSeconds(60), now.plusSeconds(3600));
        auction.start(now);
        InMemoryAuctions repository = new InMemoryAuctions(auction);
        BiddingService service = new BiddingService(repository, new AuctionLockRegistry(),
                new EventBus(), Clock.fixed(now, ZoneOffset.UTC));
        int workers = 32;
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger accepted = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < workers; i++) {
                final int index = i;
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    go.await();
                    try {
                        service.placeBid("a1", "bidder-" + index, new BigDecimal("200"));
                        accepted.incrementAndGet();
                    } catch (RuntimeException rejected) {
                        assertThat(rejected).isInstanceOf(RuntimeException.class);
                    }
                    return null;
                }));
            }
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            go.countDown();
            for (Future<?> future : futures) future.get(5, TimeUnit.SECONDS);
        } finally { pool.shutdownNow(); }
        Auction result = repository.findById("a1").orElseThrow();
        assertThat(accepted.get()).isEqualTo(1);
        assertThat(result.getBids()).hasSize(1);
        assertThat(result.getCurrentPrice()).isEqualByComparingTo("200");
        assertThat(result.getLeaderId()).isPresent();
    }

    private static final class InMemoryAuctions implements AuctionRepository {
        private final java.util.concurrent.ConcurrentHashMap<String, Auction> data = new java.util.concurrent.ConcurrentHashMap<>();
        private InMemoryAuctions(Auction initial) { data.put(initial.getId(), initial); }
        public Optional<Auction> findById(String id) { return Optional.ofNullable(data.get(id)); }
        public List<Auction> findAll() { return List.copyOf(data.values()); }
        public List<Auction> findBySellerId(String sellerId) { return data.values().stream().filter(a -> a.getSellerId().equals(sellerId)).toList(); }
        public void save(Auction auction) { data.put(auction.getId(), auction); }
        public void deleteById(String id) { data.remove(id); }
    }
}
