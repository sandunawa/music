package com.melodymart.model;

public class BannerConfig {
    private boolean enabled = true;
    private String badgeText = "🎁 SPECIAL WELCOME OFFER";
    private String title = "Get 15% OFF + Free Delivery!";
    private String subtitle = "Welcome to MelodyMart — Your destination for premium instruments & rentals.";
    private String couponCode = "MELODY15";
    private String imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600";
    private String buttonText = "Explore Products & Rent Now";
    private String buttonLink = "/catalog/buy";

    public BannerConfig() {
    }

    public BannerConfig(boolean enabled, String badgeText, String title, String subtitle, String couponCode, String imageUrl, String buttonText, String buttonLink) {
        this.enabled = enabled;
        this.badgeText = badgeText;
        this.title = title;
        this.subtitle = subtitle;
        this.couponCode = couponCode;
        this.imageUrl = imageUrl;
        this.buttonText = buttonText;
        this.buttonLink = buttonLink;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public String getButtonLink() {
        return buttonLink;
    }

    public void setButtonLink(String buttonLink) {
        this.buttonLink = buttonLink;
    }
}
