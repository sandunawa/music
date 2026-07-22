package com.melodymart.service;

import com.melodymart.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RentalService {

    private static final String RENTALS_FILE = "rentals.json";

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ProductService productService;

    public List<Rental> getAllRentals() {
        return fileStorageService.readList(RENTALS_FILE, Rental.class);
    }

    public List<Rental> getRentalsByUserId(String userId) {
        return getAllRentals().stream()
                .filter(r -> r.getUserId().equals(userId))
                .sorted((r1, r2) -> r2.getBookingDate().compareTo(r1.getBookingDate()))
                .collect(Collectors.toList());
    }

    public Optional<Rental> findById(String id) {
        return getAllRentals().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    public List<Rental> getRentalsReceivedForOwner(String ownerId) {
        return getAllRentals().stream()
                .filter(r -> {
                    Optional<Product> p = productService.findById(r.getProductId());
                    return p.isPresent() && ownerId.equals(p.get().getOwnerId());
                })
                .sorted((r1, r2) -> r2.getBookingDate().compareTo(r1.getBookingDate()))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a product has available stock to rent during the requested date range.
     * Computes overlap with active bookings.
     */
    public boolean isProductAvailableForRental(String productId, LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            return false;
        }

        Product product = productService.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));

        // Get all active bookings for this product
        List<Rental> activeRentals = getAllRentals().stream()
                .filter(r -> r.getProductId().equals(productId))
                .filter(r -> r.getRentalStatus() == RentalStatus.ACTIVE)
                .collect(Collectors.toList());

        // Count overlapping active bookings
        long overlappingCount = activeRentals.stream()
                .filter(r -> {
                    // Overlap logic: !(r.endDate < start || r.startDate > end)
                    return !(r.getEndDate().isBefore(start) || r.getStartDate().isAfter(end));
                })
                .count();

        // Available if overlapping rentals are less than overall stock
        return overlappingCount < product.getStockQuantity();
    }

    /**
     * Books a rental for a user. Validates availability and processes simulated payment.
     */
    public synchronized Rental bookRental(User user, String productId, LocalDate start, LocalDate end, String paymentMethod) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and End dates are required.");
        }
        if (start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Rental start date cannot be in the past.");
        }
        if (end.isBefore(start) || end.equals(start)) {
            throw new IllegalArgumentException("Rental end date must be after start date.");
        }

        Product product = productService.findById(productId)
                .orElseThrow(() -> new NoSuchElementException("Product not found."));

        if (!isProductAvailableForRental(productId, start, end)) {
            throw new IllegalArgumentException("This instrument is already booked by other customers during this period.");
        }

        long days = ChronoUnit.DAYS.between(start, end);
        double totalCost = days * product.getRentalPricePerDay();

        Rental rental = new Rental();
        rental.setId("r-" + UUID.randomUUID().toString().substring(0, 8));
        rental.setUserId(user.getId());
        rental.setCustomerName(user.getName());
        rental.setCustomerEmail(user.getEmail());
        rental.setProductId(productId);
        rental.setProductName(product.getName());
        rental.setStartDate(start);
        rental.setEndDate(end);
        rental.setTotalDays(days);
        rental.setPricePerDay(product.getRentalPricePerDay());
        rental.setTotalCost(Math.round(totalCost * 100.0) / 100.0);
        rental.setRentalStatus(RentalStatus.ACTIVE);
        rental.setPaymentStatus("PAID"); // Simulated successful payment
        rental.setBookingDate(LocalDateTime.now());

        List<Rental> rentals = getAllRentals();
        rentals.add(rental);
        fileStorageService.writeList(RENTALS_FILE, rentals);

        return rental;
    }

    /**
     * Logs the return of a rented instrument.
     */
    public synchronized Rental returnInstrument(String rentalId) {
        List<Rental> rentals = getAllRentals();
        for (Rental r : rentals) {
            if (r.getId().equals(rentalId)) {
                if (r.getRentalStatus() == RentalStatus.RETURNED) {
                    throw new IllegalStateException("This rental has already been marked as returned.");
                }
                r.setActualReturnDate(LocalDate.now());
                r.setRentalStatus(RentalStatus.RETURNED);
                fileStorageService.writeList(RENTALS_FILE, rentals);
                return r;
            }
        }
        throw new NoSuchElementException("Rental record not found for ID: " + rentalId);
    }
}
