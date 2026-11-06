package com.auction.domain.model.item;

import com.auction.contract.vocab.ItemType;
import com.auction.domain.model.Entity;
import com.auction.domain.model.Guard;
import java.math.BigDecimal;

/**
 * Sản phẩm đem ra đấu giá. {@link #printInfo()} là template method: phần chung ở đây,
 * phần riêng của từng loại do {@link #describeDetails()} cung cấp.
 */
public abstract class Item extends Entity {

    private final String sellerId;
    private String name;
    private String description;
    private BigDecimal startingPrice;

    protected Item(
            String id, String sellerId, String name, String description, BigDecimal startingPrice) {
        super(id);
        this.sellerId = Guard.requireNotBlank(sellerId, "sellerId");
        this.name = Guard.requireNotBlank(name, "name");
        this.description = description == null ? "" : description;
        this.startingPrice = Guard.requirePositive(startingPrice, "startingPrice");
    }

    public void updateBasicInfo(String name, String description, BigDecimal startingPrice) {
        String newName = Guard.requireNotBlank(name, "name");
        BigDecimal newPrice = Guard.requirePositive(startingPrice, "startingPrice");
        this.name = newName;
        this.description = description == null ? "" : description;
        this.startingPrice = newPrice;
    }

    /** Loại sản phẩm, do lớp con quyết định. */
    public abstract ItemType getType();

    /** Thông tin đặc thù của từng loại sản phẩm. */
    protected abstract String describeDetails();

    public String getSellerId() {
        return sellerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    @Override
    public final String printInfo() {
        return "[" + getType() + "] " + name + " | starting price: " + startingPrice.toPlainString()
                + " | " + describeDetails();
    }
}