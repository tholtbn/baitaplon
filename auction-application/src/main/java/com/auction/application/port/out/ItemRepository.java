package com.auction.application.port.out;

import com.auction.domain.model.item.Item;
import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Item> findById(String id);
    List<Item> findAll();
    List<Item> findBySellerId(String sellerId);
    Item save(Item item);
    void deleteById(String id);
}
