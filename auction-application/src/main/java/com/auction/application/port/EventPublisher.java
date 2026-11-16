package com.auction.application.port;

import com.auction.application.event.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent event);
}