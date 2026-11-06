package com.auction.contract.request;

import com.auction.contract.vocab.ItemType;
import java.math.BigDecimal;
import java.util.Map;

/** itemId = null khi tạo mới. */
public record ItemRequest(String itemId, ItemType type, String name, String description,
                          BigDecimal startingPrice, Map<String, String> attributes) {}