package com.auction.application.port;

import com.auction.domain.model.auction.Auction;
import com.auction.domain.model.auction.AuctionStatus;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository {

    Optional<Auction> findById(String auctionId);

    List<Auction> findAll();

    List<Auction> findByStatus(AuctionStatus status);

    List<Auction> findBySellerId(String sellerId);

    void save(Auction auction);

    void deleteById(String auctionId);
}