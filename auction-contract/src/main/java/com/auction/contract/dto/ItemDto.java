// dto/ItemDto.java
package com.auction.contract.dto;

import com.auction.contract.vocab.ItemType;
import java.math.BigDecimal;
import java.util.Map;

public record ItemDto(String id, String sellerId, String name, String description,
                      BigDecimal startingPrice, ItemType type, Map<String, String> attributes) {}