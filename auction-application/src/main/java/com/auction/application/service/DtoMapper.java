package com.auction.application.service;

import com.auction.contract.dto.*;
import com.auction.contract.vocab.ItemType;
import com.auction.domain.model.auction.Auction;
import com.auction.domain.model.auction.BidTransaction;
import com.auction.domain.model.item.*;
import com.auction.domain.model.user.User;
import java.util.LinkedHashMap;
import java.util.Map;

final class DtoMapper {
    private DtoMapper() {}

    static UserDto user(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    static ItemDto item(Item item) {
        Map<String, String> attributes = new LinkedHashMap<>();
        ItemType type = item.getType();
        if (item instanceof Electronics electronics) {
            attributes.put("brand", electronics.getBrand());
            attributes.put("warrantyMonths", Integer.toString(electronics.getWarrantyMonths()));
        } else if (item instanceof Art art) {
            attributes.put("artist", art.getArtist());
            attributes.put("creationYear", Integer.toString(art.getCreationYear()));
        } else if (item instanceof Vehicle vehicle) {
            attributes.put("make", vehicle.getMake());
            attributes.put("model", vehicle.getModel());
            attributes.put("manufactureYear", Integer.toString(vehicle.getManufactureYear()));
        }
        return new ItemDto(item.getId(), item.getSellerId(), item.getName(), item.getDescription(),
                item.getStartingPrice(), type, attributes);
    }

    static BidDto bid(BidTransaction bid) {
        return new BidDto(bid.getId(), bid.getAuctionId(), bid.getBidderId(),
                bid.getAmount(), bid.getPlacedAt());
    }

    static AuctionDto auction(Auction auction, Item item) {
        return new AuctionDto(auction.getId(), item == null ? null : item(item),
                auction.getSellerId(), auction.getStatus(), auction.getCurrentPrice(),
                auction.getLeaderId().orElse(null), auction.getStartTime(),
                auction.getEndTime(), auction.getBids().size());
    }
}
