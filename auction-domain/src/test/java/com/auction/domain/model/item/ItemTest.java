package com.auction.domain.model.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auction.contract.vocab.ItemType;
import com.auction.domain.exception.ValidationException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ItemTest {

    private static final BigDecimal PRICE = new BigDecimal("100");

    @Test
    void printInfoIsPolymorphicAcrossItemTypes() {
        List<Item> items = List.of(
                new Electronics("1", "s1", "Phone", "desc", PRICE, "Apple", 12),
                new Art("2", "s1", "Mona", "desc", PRICE, "Leonardo", 1503),
                new Vehicle("3", "s1", "Car", "desc", PRICE, "Toyota", "Camry", 2020));

        assertThat(items).extracting(Item::getType)
                .containsExactly(ItemType.ELECTRONICS, ItemType.ART, ItemType.VEHICLE);
        assertThat(items.get(0).printInfo()).contains("Apple").contains("12 months");
        assertThat(items.get(1).printInfo()).contains("Leonardo").contains("1503");
        assertThat(items.get(2).printInfo()).contains("Toyota").contains("Camry");
    }

    @Test
    void rejectsNonPositiveStartingPrice() {
        assertThatThrownBy(
                () -> new Art("2", "s1", "Mona", "desc", BigDecimal.ZERO, "Leonardo", 1503))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(
                () -> new Electronics("1", "s1", " ", "desc", PRICE, "Apple", 12))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void rejectsNegativeWarranty() {
        assertThatThrownBy(
                () -> new Electronics("1", "s1", "Phone", "desc", PRICE, "Apple", -1))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void nullDescriptionBecomesEmpty() {
        Item item = new Art("2", "s1", "Mona", null, PRICE, "Leonardo", 1503);

        assertThat(item.getDescription()).isEmpty();
    }
}