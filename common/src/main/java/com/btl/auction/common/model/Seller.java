package com.btl.auction.common.model;

public final class Seller extends User {
    public Seller(long id, String username, String displayName) {
        super(id, username, displayName);
    }

    @Override
    public UserRole getRole() {
        return UserRole.SELLER;
    }
}
