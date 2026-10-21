package com.btl.auction.common.model;

public final class Admin extends User {
    public Admin(long id, String username, String displayName) {
        super(id, username, displayName);
    }

    @Override
    public UserRole getRole() {
        return UserRole.ADMIN;
    }
}
