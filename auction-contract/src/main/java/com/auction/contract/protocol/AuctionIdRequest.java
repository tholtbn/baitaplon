// request/AuctionIdRequest.java  (GET_AUCTION, GET_BID_HISTORY, CANCEL_AUCTION, PAY_AUCTION)
package com.auction.contract.request;

public record AuctionIdRequest(String auctionId) {}