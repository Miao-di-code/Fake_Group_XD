package com.btl.auction.common.model;

import java.util.Objects;

public final class ItemFactory {
    private ItemFactory() {
    }

    public static Item create(ItemCategory category, long id, long sellerId, String name,
                              String description, long startingPriceVnd, String detail) {
        return switch (Objects.requireNonNull(category, "category")) {
            case ELECTRONICS -> new Electronics(id, sellerId, name, description,
                    startingPriceVnd, detail);
            case ART -> new Art(id, sellerId, name, description, startingPriceVnd, detail);
            case VEHICLE -> new Vehicle(id, sellerId, name, description, startingPriceVnd, detail);
        };
    }
}
