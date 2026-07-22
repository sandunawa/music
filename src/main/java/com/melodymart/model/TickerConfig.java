package com.melodymart.model;

public class TickerConfig {
    private String newsTickerText = "<span>⚡ Special Offer: Use coupon code <strong style=\"color: #9a3412;\">MELODY15</strong> for 15% off!</span> <span>🚚 Free Shipping over $50!</span> <span>⭐ Top Brands in stock!</span>";

    public TickerConfig() {
    }

    public TickerConfig(String newsTickerText) {
        this.newsTickerText = newsTickerText;
    }

    public String getNewsTickerText() {
        return newsTickerText;
    }

    public void setNewsTickerText(String newsTickerText) {
        this.newsTickerText = newsTickerText;
    }
}
