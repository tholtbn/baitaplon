package com.auction.application.port.out;

import com.auction.contract.protocol.ServerEvent;

public interface EventPublisher {
    void publish(String auctionId, ServerEvent event);
}
