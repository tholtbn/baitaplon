package com.auction.domain.model.item;

import com.auction.contract.vocab.ItemType;
import com.auction.domain.model.Guard;
import java.math.BigDecimal;

/** Sản phẩm điện tử. */
public class Electronics extends Item {

    private final String brand;
    private final int warrantyMonths;

    public Electronics(
            String id,
            String sellerId,
            String name,
            String description,
            BigDecimal startingPrice,
            String brand,
            int warrantyMonths) {
        super(id, sellerId, name, description, startingPrice);
        this.brand = Guard.requireNotBlank(brand, "brand");
        this.warrantyMonths = Guard.requireAtLeast(warrantyMonths, 0, "warrantyMonths");
    }

    public String getBrand() {
        return brand;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    @Override
    public ItemType getType() {
        return ItemType.ELECTRONICS;
    }

    @Override
    protected String describeDetails() {
        return "brand: " + brand + ", warranty: " + warrantyMonths + " months";
    }
}