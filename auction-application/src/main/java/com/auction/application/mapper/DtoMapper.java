package com.auction.application.mapper;

import com.auction.contract.dto.AuctionDto;
import com.auction.contract.dto.BidDto;
import com.auction.contract.dto.ItemDto;
import com.auction.contract.dto.UserDto;
import com.auction.contract.vocab.AuctionStatus;
import com.auction.contract.vocab.ItemType;
import com.auction.contract.vocab.Role;
import com.auction.domain.model.auction.Auction;
import com.auction.domain.model.auction.BidTransaction;
import com.auction.domain.model.item.Art;
import com.auction.domain.model.item.Electronics;
import com.auction.domain.model.item.Item;
import com.auction.domain.model.item.Vehicle;
import com.auction.domain.model.user.User;

import java.util.List;
import java.util.Map;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static ItemDto toItemDto(Item item) {
        Map<String, String> attributes = switch (item) {
            case Electronics electronics -> Map.of(
                    "brand", electronics.getBrand(),
                    "warrantyMonths", String.valueOf(electronics.getWarrantyMonths())
            );
            case Art art -> Map.of(
                    "artist", art.getArtist(),
                    "creationYear", String.valueOf(art.getCreationYear())
            );
            case Vehicle vehicle -> Map.of(
                    "make", vehicle.getMake(),
                    "model", vehicle.getModel(),
                    "manufactureYear", String.valueOf(vehicle.getManufactureYear())
            );
            default -> Map.of();
        };

        return new ItemDto(
                item.getId(),
                item.getSellerId(),
                item.getName(),
                item.getDescription(),
                item.getStartingPrice(),
                ItemType.valueOf(item.getType().name()),
                attributes
        );
    }

    public static BidDto toBidDto(BidTransaction bid) {
        return new BidDto(
                bid.getId(),
                bid.getAuctionId(),
                bid.getBidderId(),
                bid.getAmount(),
                bid.getPlacedAt()
        );
    }

    public static AuctionDto toAuctionDto(Auction auction, Item item) {
        return new AuctionDto(
                auction.getId(),
                toItemDto(item),
                auction.getSellerId(),
                AuctionStatus.valueOf(auction.getStatus().name()),
                auction.getCurrentPrice(),
                auction.getLeaderId().orElse(null),
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getBids().size()
        );
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                Role.valueOf(user.getRole().name())
        );
    }

    public static List<BidDto> toBidDtos(List<BidTransaction> bids) {
        return bids.stream()
                .map(DtoMapper::toBidDto)
                .toList();
    }
}