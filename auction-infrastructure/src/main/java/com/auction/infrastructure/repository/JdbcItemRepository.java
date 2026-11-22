package com.auction.infrastructure.repository;

import com.auction.application.port.ItemRepository;
import com.auction.contract.vocab.ItemType;
import com.auction.domain.model.item.Item;
import com.auction.infrastructure.dao.ItemDao;
import com.auction.infrastructure.dao.ItemDao.ItemRow;

import java.util.List;
import java.util.Optional;

public class JdbcItemRepository implements ItemRepository {
    private final ItemDao itemDao;

    public JdbcItemRepository(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    @Override
    public Optional<Item> findById(String itemId) {
        return itemDao.findById(itemId).map(this::mapToDomain);
    }

    @Override
    public List<Item> findBySellerId(String sellerId) {
        return itemDao.findAll().stream()
                .filter(r -> sellerId != null && sellerId.equals(r.sellerId()))
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public void save(Item item) {
        ItemRow row = new ItemRow(
                item.getId(),
                item.getSellerId(),
                item.getName(),
                item.getDescription(),
                item.getStartingPrice(),
                item.getType() != null ? item.getType().name() : ItemType.ELECTRONICS.name(),
                "AVAILABLE"
        );
        if (itemDao.findById(item.getId()).isPresent()) {
            itemDao.update(row);
        } else {
            itemDao.insert(row);
        }
    }

    @Override
    public void deleteById(String itemId) {
        itemDao.deleteById(itemId);
    }

    protected Item mapToDomain(ItemRow row) {
        ItemType itemType = row.type() != null ? ItemType.valueOf(row.type()) : ItemType.ELECTRONICS;
        String details = row.description() != null ? row.description() : "";
        return new Item(row.id(), row.sellerId(), row.name(), row.description(), row.startingPrice()) {
            @Override
            public ItemType getType() {
                return itemType;
            }

            @Override
            protected String describeDetails() {
                return details;
            }
        };
    }
}