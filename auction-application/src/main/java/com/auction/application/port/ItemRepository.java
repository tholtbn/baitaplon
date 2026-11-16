package com.auction.application.port;

import com.auction.domain.model.item.Item;
import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Item> findById(String itemId);
    List<Item> findBySellerId(String sellerId);
    void save(Item item);
    void deleteById(String itemId);
}
