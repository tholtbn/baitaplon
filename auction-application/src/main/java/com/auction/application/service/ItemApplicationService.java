package com.auction.application.service;

import com.auction.application.exception.ApplicationException;
import com.auction.application.port.out.IdGenerator;
import com.auction.application.port.out.ItemRepository;
import com.auction.application.port.out.UserRepository;
import com.auction.contract.dto.ItemDto;
import com.auction.contract.request.ItemIdRequest;
import com.auction.contract.request.ItemRequest;
import com.auction.contract.vocab.ErrorCode;
import com.auction.contract.vocab.ItemType;
import com.auction.contract.vocab.Role;
import com.auction.domain.model.item.*;
import com.auction.domain.model.user.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public final class ItemApplicationService extends AccessSupport {
    private final ItemRepository items;
    private final IdGenerator ids;

    public ItemApplicationService(UserRepository users, ItemRepository items, IdGenerator ids) {
        super(users);
        this.items = items;
        this.ids = ids;
    }

    public ItemDto create(String userId, ItemRequest request) {
        User seller = requireRole(userId, Role.SELLER);
        validateRequest(request);
        Item item = build(request, ids.nextId(), seller.getId());
        return DtoMapper.item(items.save(item));
    }

    public ItemDto update(String userId, ItemRequest request) {
        if (request == null || request.itemId() == null || request.itemId().isBlank()) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "itemId is required");
        }
        User actor = requireSellerOrAdmin(userId);
        Item existing = findItem(request.itemId());
        if (actor.getRole() != Role.ADMIN && !existing.getSellerId().equals(actor.getId())) {
            throw new ApplicationException(ErrorCode.FORBIDDEN, "You do not own this item");
        }
        validateRequest(request);
        if (existing.getType() != request.type()) {
            throw new ApplicationException(ErrorCode.VALIDATION, "Item type cannot be changed");
        }
        existing.updateBasicInfo(request.name(), request.description(), request.startingPrice());
        return DtoMapper.item(items.save(existing));
    }

    public void delete(String userId, ItemIdRequest request) {
        if (request == null || request.itemId() == null || request.itemId().isBlank()) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "itemId is required");
        }
        User actor = requireSellerOrAdmin(userId);
        Item item = findItem(request.itemId());
        if (actor.getRole() != Role.ADMIN && !item.getSellerId().equals(actor.getId())) {
            throw new ApplicationException(ErrorCode.FORBIDDEN, "You do not own this item");
        }
        items.deleteById(item.getId());
    }

    public List<ItemDto> listMine(String userId) {
        requireRole(userId, Role.SELLER);
        return items.findBySellerId(userId).stream().map(DtoMapper::item).toList();
    }

    private Item findItem(String id) {
        return items.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.NOT_FOUND, "Item not found"));
    }

    private static void validateRequest(ItemRequest request) {
        if (request == null || request.type() == null || request.startingPrice() == null) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "Incomplete item data");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION, "Item name is required");
        }
    }

    private static Item build(ItemRequest r, String id, String sellerId) {
        Map<String, String> a = r.attributes() == null ? Map.of() : r.attributes();
        return switch (r.type()) {
            case ELECTRONICS -> new Electronics(id, sellerId, r.name(), r.description(), r.startingPrice(),
                    required(a, "brand"), integer(a, "warrantyMonths"));
            case ART -> new Art(id, sellerId, r.name(), r.description(), r.startingPrice(),
                    required(a, "artist"), integer(a, "creationYear"));
            case VEHICLE -> new Vehicle(id, sellerId, r.name(), r.description(), r.startingPrice(),
                    required(a, "make"), required(a, "model"), integer(a, "manufactureYear"));
        };
    }

    private static String required(Map<String, String> map, String key) {
        String value = map.get(key);
        if (value == null || value.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION, "Missing item attribute: " + key);
        }
        return value.trim();
    }

    private static int integer(Map<String, String> map, String key) {
        try {
            return Integer.parseInt(required(map, key));
        } catch (NumberFormatException e) {
            throw new ApplicationException(ErrorCode.VALIDATION, "Invalid integer attribute: " + key);
        }
    }
}
