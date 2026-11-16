package com.auction.application.port;

import com.auction.domain.model.auction.AutoBid;
import java.util.List;

public interface AutoBidRepository {
    List<AutoBid> findByAuctionId(String auctionId);
    void save(String auctionId, AutoBid autoBid);
    void delete(String auctionId, String bidderId);
}
