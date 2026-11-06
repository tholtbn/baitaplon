package com.auction.contract.request;

public record ItemRequest(String itemId, ItemType type, String name, String description,
                          BigDecimal startingPrice, Map<String, String> attributes) {}   // itemId = null khi tạo mới