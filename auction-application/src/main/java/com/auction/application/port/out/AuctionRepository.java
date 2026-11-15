package com.auction.application.port.out;

import com.auction.domain.model.auction.Auction;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository {
    Optional<Auction> findById(String id);
    List<Auction> findAll();
    List<Auction> findBySellerId(String sellerId);
    Auction save(Auction auction);
}
