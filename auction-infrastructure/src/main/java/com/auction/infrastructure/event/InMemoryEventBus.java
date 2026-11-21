package com.auction.infrastructure.event;

import com.auction.application.event.DomainEvent;
import com.auction.application.event.EventListener;
import com.auction.application.port.EventPublisher;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryEventBus implements EventPublisher {
    private final List<EventListener<DomainEvent>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        for (EventListener<DomainEvent> listener : listeners) {
            try {
                listener.on(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <E extends DomainEvent> void register(EventListener<E> listener) {
        if (listener != null) {
            listeners.add((EventListener<DomainEvent>) listener);
        }
    }
}