package com.melodymart.model;

import java.util.HashMap;
import java.util.Map;

public class Product {
    private String id;
    private String name;
    private Category category;
    private String brand;
    private String description;
    private double price;
    private double rentalPricePerDay;
    private int stockQuantity;
    private String imageUrl;
    private Map<String, String> specifications = new HashMap<>();

    // Peer-to-Peer Marketplace Fields
    private String ownerId = "admin"; // Default to admin (store-owned)
    private String ownerName = "MelodyMart";
    private boolean forSale = true;
    private boolean forRent = true;

    public Product() {
    }

    public Product(String id, String name, Category category, String brand, String description, double price, double rentalPricePerDay, int stockQuantity, String imageUrl, Map<String, String> specifications) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.rentalPricePerDay = rentalPricePerDay;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.specifications = specifications;
    }

    public Product(String id, String name, Category category, String brand, String description, double price, double rentalPricePerDay, int stockQuantity, String imageUrl, Map<String, String> specifications, String ownerId, String ownerName, boolean forSale, boolean forRent) {
        this(id, name, category, brand, description, price, rentalPricePerDay, stockQuantity, imageUrl, specifications);
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.forSale = forSale;
        this.forRent = forRent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRentalPricePerDay() {
        return rentalPricePerDay;
    }

    public void setRentalPricePerDay(double rentalPricePerDay) {
        this.rentalPricePerDay = rentalPricePerDay;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Map<String, String> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Map<String, String> specifications) {
        this.specifications = specifications;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public boolean isForRent() {
        return forRent;
    }

    public void setForRent(boolean forRent) {
        this.forRent = forRent;
    }
}
