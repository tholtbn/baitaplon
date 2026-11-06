package com.auction.domain.model.item;

import com.auction.contract.vocab.ItemType;
import com.auction.domain.model.Guard;
import java.math.BigDecimal;

/** Tác phẩm nghệ thuật. */
public class Art extends Item {

    private final String artist;
    private final int creationYear;

    public Art(
            String id,
            String sellerId,
            String name,
            String description,
            BigDecimal startingPrice,
            String artist,
            int creationYear) {
        super(id, sellerId, name, description, startingPrice);
        this.artist = Guard.requireNotBlank(artist, "artist");
        this.creationYear = Guard.requireAtLeast(creationYear, 1, "creationYear");
    }

    public String getArtist() {
        return artist;
    }

    public int getCreationYear() {
        return creationYear;
    }

    @Override
    public ItemType getType() {
        return ItemType.ART;
    }

    @Override
    protected String describeDetails() {
        return "artist: " + artist + ", year: " + creationYear;
    }
}