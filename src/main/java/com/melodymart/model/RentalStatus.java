package com.melodymart.model;

public enum RentalStatus {
    ACTIVE("Active"),
    RETURNED("Returned"),
    OVERDUE("Overdue");

    private final String displayName;

    RentalStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
