package com.auction.application.service;

import com.auction.application.exception.NotFoundException;
import com.auction.application.exception.UnauthorizedException;
import com.auction.application.port.AuctionRepository;
import com.auction.application.port.IdGenerator;
import com.auction.application.port.ItemRepository;
import com.auction.domain.model.auction.Auction;
import com.auction.contract.vocab.AuctionStatus;
import com.auction.domain.model.item.Item;
import com.auction.domain.model.user.User;
import com.auction.domain.rule.AntiSnipingPolicy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final ItemRepository itemRepository;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public AuctionService(
            AuctionRepository auctionRepository,
            ItemRepository itemRepository,
            IdGenerator idGenerator,
            Clock clock) {
        this.auctionRepository = auctionRepository;
        this.itemRepository = itemRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    public Auction createAuction(
            User seller,
            String itemId,
            BigDecimal startingPrice,
            Instant startTime,
            Instant endTime,
            AntiSnipingPolicy antiSnipingPolicy) {

        checkSeller(seller);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new NotFoundException("Item", itemId));

        if (!seller.getId().equals(item.getSellerId())) {
            throw new UnauthorizedException("You do not own this item");
        }

        Auction auction = new Auction(
                idGenerator.newId(),
                itemId,
                seller.getId(),
                startingPrice,
                startTime,
                endTime,
                antiSnipingPolicy
        );

        auctionRepository.save(auction);
        return auction;
    }

    public void cancelAuction(User seller, String auctionId) {
        checkSeller(seller);

        Auction auction = findAuction(auctionId);

        if (!seller.getId().equals(auction.getSellerId())) {
            throw new UnauthorizedException("You do not own this auction");
        }

        auction.cancel();
        auctionRepository.save(auction);
    }

    public Auction getAuction(String auctionId) {
        return findAuction(auctionId);
    }

    public List<Auction> listAuctions() {
        return auctionRepository.findAll();
    }

    public List<Auction> listAuctionsByStatus(AuctionStatus status) {
        return auctionRepository.findByStatus(status);
    }

    public void markPaid(String auctionId) {
        Auction auction = findAuction(auctionId);
        auction.markPaid();
        auctionRepository.save(auction);
    }

    private Auction findAuction(String auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() ->
                        new NotFoundException("Auction", auctionId));
    }

    private void checkSeller(User user) {
        if (user == null || !"SELLER".equals(user.getRole().name())) {
            throw new UnauthorizedException("Seller permission required");
        }
    }
}