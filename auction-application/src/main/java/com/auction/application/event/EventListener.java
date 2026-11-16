package com.auction.application.event;

@FunctionalInterface
public interface EventListener<E extends DomainEvent> {
    void on(E event);
}