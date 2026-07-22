package com.melodymart.service;

import com.melodymart.model.Category;
import com.melodymart.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final String PRODUCTS_FILE = "products.json";

    @Autowired
    private FileStorageService fileStorageService;

    public List<Product> getAllProducts() {
        return fileStorageService.readList(PRODUCTS_FILE, Product.class);
    }

    public Optional<Product> findById(String id) {
        return getAllProducts().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    /**
     * Retrieve products uploaded by a specific owner/user.
     */
    public List<Product> getProductsByOwnerId(String ownerId) {
        return getAllProducts().stream()
                .filter(p -> p.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    /**
     * Filters products based on keyword search, category, price range, stock availability, and list types (sale vs rent).
     */
    public List<Product> searchAndFilter(String query, Category category, Double minPrice, Double maxPrice, Boolean inStockOnly, Boolean forSale, Boolean forRent) {
        return getAllProducts().stream()
                .filter(p -> {
                    if (query == null || query.trim().isEmpty()) {
                        return true;
                    }
                    String term = query.toLowerCase().trim();
                    return p.getName().toLowerCase().contains(term) ||
                           p.getBrand().toLowerCase().contains(term) ||
                           p.getDescription().toLowerCase().contains(term);
                })
                .filter(p -> category == null || p.getCategory() == category)
                .filter(p -> {
                    if (minPrice == null) return true;
                    double activePrice = (forRent != null && forRent) ? p.getRentalPricePerDay() : p.getPrice();
                    return activePrice >= minPrice;
                })
                .filter(p -> {
                    if (maxPrice == null) return true;
                    double activePrice = (forRent != null && forRent) ? p.getRentalPricePerDay() : p.getPrice();
                    return activePrice <= maxPrice;
                })
                .filter(p -> inStockOnly == null || !inStockOnly || p.getStockQuantity() > 0)
                .filter(p -> forSale == null || p.isForSale() == forSale)
                .filter(p -> forRent == null || p.isForRent() == forRent)
                .collect(Collectors.toList());
    }

    /**
     * Saves a new or modified product.
     */
    public synchronized Product save(Product product) {
        List<Product> products = getAllProducts();
        if (product.getId() == null || product.getId().trim().isEmpty()) {
            // New product
            product.setId("p-" + UUID.randomUUID().toString().substring(0, 8));
            products.add(product);
        } else {
            // Update existing
            boolean found = false;
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId().equals(product.getId())) {
                    products.set(i, product);
                    found = true;
                    break;
                }
            }
            if (!found) {
                products.add(product);
            }
        }
        fileStorageService.writeList(PRODUCTS_FILE, products);
        return product;
    }

    /**
     * Deletes a product from catalog inventory.
     */
    public synchronized void delete(String id) {
        List<Product> products = getAllProducts();
        boolean removed = products.removeIf(p -> p.getId().equals(id));
        if (removed) {
            fileStorageService.writeList(PRODUCTS_FILE, products);
        } else {
            throw new NoSuchElementException("Instrument with ID " + id + " not found.");
        }
    }

    /**
     * Atomic helper to decrease or increase instrument stock.
     */
    public synchronized void adjustStock(String productId, int quantityChange) {
        List<Product> products = getAllProducts();
        for (Product p : products) {
            if (p.getId().equals(productId)) {
                int newStock = p.getStockQuantity() + quantityChange;
                if (newStock < 0) {
                    throw new IllegalArgumentException("Insufficient inventory stock for " + p.getName());
                }
                p.setStockQuantity(newStock);
                fileStorageService.writeList(PRODUCTS_FILE, products);
                return;
            }
        }
        throw new NoSuchElementException("Product with ID " + productId + " not found.");
    }
}
