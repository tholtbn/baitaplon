package com.auction.application.event;

import com.auction.application.port.EventPublisher;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryEventBus implements EventPublisher {

    private final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventListener<?>>> listeners =
            new ConcurrentHashMap<>();

    public <E extends DomainEvent> void subscribe(
            Class<E> eventType,
            EventListener<E> listener) {

        listeners.computeIfAbsent(
                eventType,
                key -> new CopyOnWriteArrayList<>()
        ).add(listener);
    }

    public <E extends DomainEvent> void unsubscribe(
            Class<E> eventType,
            EventListener<E> listener) {

        List<EventListener<?>> eventListeners = listeners.get(eventType);

        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    @Override
    public void publish(DomainEvent event) {
        List<EventListener<?>> eventListeners = listeners.get(event.getClass());

        if (eventListeners == null) {
            return;
        }

        for (EventListener<?> listener : eventListeners) {
            notifyListener(listener, event);
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends DomainEvent> void notifyListener(
            EventListener<?> listener,
            DomainEvent event) {

        ((EventListener<E>) listener).on((E) event);
    }
}