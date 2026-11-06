
package com.auction.application.dto;

import com.auction.contract.vocab.ItemType;
public record ItemDto(String id, String sellerId, String name, String description,
                      BigDecimal startingPrice, ItemType type, Map<String, String> attributes) {}