package com.btl.auction.common.model;

public final class Bidder extends User {
    public Bidder(long id, String username, String displayName) {
        super(id, username, displayName);
    }

    @Override
    public UserRole getRole() {
        return UserRole.BIDDER;
    }
}
