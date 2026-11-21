package com.auction.application.factory;

import com.auction.domain.model.item.Art;
import com.auction.domain.model.item.Electronics;
import com.auction.domain.model.item.Item;
import com.auction.domain.model.item.Vehicle;
import com.auction.contract.vocab.ItemType;

import java.math.BigDecimal;
import java.util.Map;

public class ItemFactory {

    public Item create(
            ItemType type,
            String id,
            String sellerId,
            String name,
            String description,
            BigDecimal startingPrice,
            Map<String, String> attributes
    ) {
        return switch (type) {
            case ELECTRONICS -> new Electronics(
                    id,
                    sellerId,
                    name,
                    description,
                    startingPrice,
                    attributes.get("brand"),
                    Integer.parseInt(attributes.get("warrantyMonths"))
            );

            case ART -> new Art(
                    id,
                    sellerId,
                    name,
                    description,
                    startingPrice,
                    attributes.get("artist"),
                    Integer.parseInt(attributes.get("creationYear"))
            );

            case VEHICLE -> new Vehicle(
                    id,
                    sellerId,
                    name,
                    description,
                    startingPrice,
                    attributes.get("make"),
                    attributes.get("model"),
                    Integer.parseInt(attributes.get("manufactureYear"))
            );
        };
    }
}
