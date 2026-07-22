package com.melodymart.model;

public enum Category {
    GUITAR("Guitars"),
    KEYBOARD("Keyboards"),
    DRUMS("Drums & Percussion"),
    STUDIO_AUDIO("Studio & Audio Gear"),
    BRASS_WOODWIND("Brass & Woodwinds"),
    ACCESSORIES("Accessories");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
