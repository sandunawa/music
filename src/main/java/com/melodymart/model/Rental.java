package com.melodymart.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Rental {
    private String id;
    private String userId;
    private String customerName;
    private String customerEmail;
    private String productId;
    private String productName;
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDays;
    private double pricePerDay;
    private double totalCost;
    private RentalStatus rentalStatus;
    private LocalDate actualReturnDate;
    private String paymentStatus; // "PAID", "UNPAID"
    private LocalDateTime bookingDate;

    public Rental() {
    }

    public Rental(String id, String userId, String customerName, String customerEmail, String productId, String productName, LocalDate startDate, LocalDate endDate, long totalDays, double pricePerDay, double totalCost, RentalStatus rentalStatus, LocalDate actualReturnDate, String paymentStatus, LocalDateTime bookingDate) {
        this.id = id;
        this.userId = userId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.productId = productId;
        this.productName = productName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
        this.pricePerDay = pricePerDay;
        this.totalCost = totalCost;
        this.rentalStatus = rentalStatus;
        this.actualReturnDate = actualReturnDate;
        this.paymentStatus = paymentStatus;
        this.bookingDate = bookingDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public long getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(long totalDays) {
        this.totalDays = totalDays;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public RentalStatus getRentalStatus() {
        return rentalStatus;
    }

    public void setRentalStatus(RentalStatus rentalStatus) {
        this.rentalStatus = rentalStatus;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }
}
