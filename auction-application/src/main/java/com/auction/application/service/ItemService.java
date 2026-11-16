package com.auction.application.service;

import com.auction.application.exception.NotFoundException;
import com.auction.application.exception.UnauthorizedException;
import com.auction.application.port.AuctionRepository;
import com.auction.application.port.ItemRepository;
import com.auction.domain.model.auction.Auction;
import com.auction.domain.model.auction.AuctionStatus;
import com.auction.domain.model.item.Item;
import com.auction.domain.model.user.User;

import java.math.BigDecimal;
import java.util.List;

public class ItemService {

    private final ItemRepository itemRepository;
    private final AuctionRepository auctionRepository;

    public ItemService(
            ItemRepository itemRepository,
            AuctionRepository auctionRepository) {
        this.itemRepository = itemRepository;
        this.auctionRepository = auctionRepository;
    }

    public void createItem(User seller, Item item) {
        checkSeller(seller);

        if (!seller.getId().equals(item.getSellerId())) {
            throw new UnauthorizedException("Cannot create item for another seller");
        }

        itemRepository.save(item);
    }

    public void updateItem(
            User seller,
            String itemId,
            String name,
            String description,
            BigDecimal startingPrice) {

        checkSeller(seller);

        Item item = findItem(itemId);
        checkOwnership(seller, item);

        item.updateBasicInfo(name, description, startingPrice);
        itemRepository.save(item);
    }

    public void deleteItem(User seller, String itemId) {
        checkSeller(seller);

        Item item = findItem(itemId);
        checkOwnership(seller, item);

        boolean hasActiveAuction = auctionRepository.findAll()
                .stream()
                .anyMatch(auction ->
                        auction.getItemId().equals(itemId)
                                && (auction.getStatus() == AuctionStatus.OPEN
                                || auction.getStatus() == AuctionStatus.RUNNING));

        if (hasActiveAuction) {
            throw new IllegalStateException(
                    "Cannot delete an item in an active auction");
        }

        itemRepository.delete(itemId);
    }

    public List<Item> listMyItems(User seller) {
        checkSeller(seller);
        return itemRepository.findBySeller(seller.getId());
    }

    private Item findItem(String itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new NotFoundException("Item not found: " + itemId));
    }

    private void checkOwnership(User seller, Item item) {
        if (!seller.getId().equals(item.getSellerId())) {
            throw new UnauthorizedException("You do not own this item");
        }
    }

    private void checkSeller(User user) {
        if (user == null || !"SELLER".equals(user.getRole().name())) {
            throw new UnauthorizedException("Seller permission required");
        }
    }
}