package com.example.campuscore.models;

public class FeatureCard {
    private final int iconResId;
    private final String title;
    private final String description;
    private final boolean availableSoon;

    public FeatureCard(int iconResId, String title, String description, boolean availableSoon) {
        this.iconResId = iconResId;
        this.title = title;
        this.description = description;
        this.availableSoon = availableSoon;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAvailableSoon() {
        return availableSoon;
    }
}
