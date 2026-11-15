package com.auction.application.port.out;

import com.auction.domain.model.auction.AutoBid;
import java.util.List;
import java.util.Optional;

public interface AutoBidRepository {
    List<AutoBid> findByAuctionId(String auctionId);
    Optional<AutoBid> findByAuctionIdAndBidderId(String auctionId, String bidderId);
    AutoBid save(String auctionId, AutoBid autoBid);
    void delete(String auctionId, String bidderId);
}
