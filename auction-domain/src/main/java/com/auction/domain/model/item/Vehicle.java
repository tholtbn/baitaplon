package com.auction.domain.model.item;

import com.auction.contract.vocab.ItemType;
import com.auction.domain.model.Guard;
import java.math.BigDecimal;

/** Phương tiện. */
public class Vehicle extends Item {

    private final String make;
    private final String model;
    private final int manufactureYear;

    public Vehicle(
            String id,
            String sellerId,
            String name,
            String description,
            BigDecimal startingPrice,
            String make,
            String model,
            int manufactureYear) {
        super(id, sellerId, name, description, startingPrice);
        this.make = Guard.requireNotBlank(make, "make");
        this.model = Guard.requireNotBlank(model, "model");
        this.manufactureYear = Guard.requireAtLeast(manufactureYear, 1, "manufactureYear");
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public int getManufactureYear() {
        return manufactureYear;
    }

    @Override
    public ItemType getType() {
        return ItemType.VEHICLE;
    }

    @Override
    protected String describeDetails() {
        return "make: " + make + ", model: " + model + ", year: " + manufactureYear;
    }
}