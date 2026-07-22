package com.melodymart.service;

import com.melodymart.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final String ORDERS_FILE = "orders.json";

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ProductService productService;

    public List<Order> getAllOrders() {
        return fileStorageService.readList(ORDERS_FILE, Order.class);
    }

    public List<Order> getOrdersByUserId(String userId) {
        return getAllOrders().stream()
                .filter(o -> o.getUserId().equals(userId))
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .collect(Collectors.toList());
    }

    public Optional<Order> findById(String id) {
        return getAllOrders().stream()
                .filter(o -> o.getId().equals(id))
                .findFirst();
    }

    public List<Order> getOrdersReceivedForOwner(String ownerId) {
        return getAllOrders().stream()
                .filter(o -> o.getItems().stream().anyMatch(item -> {
                    Optional<Product> p = productService.findById(item.getProductId());
                    return p.isPresent() && ownerId.equals(p.get().getOwnerId());
                }))
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .collect(Collectors.toList());
    }

    /**
     * Creates and persists a retail order after validating stock. Deducts product stock.
     */
    public synchronized Order placeOrder(User user, List<OrderItem> cartItems, String shippingAddress, String phoneNumber, String paymentMethod) {
        return placeOrder(user, cartItems, shippingAddress, phoneNumber, paymentMethod, 0.0);
    }

    public synchronized Order placeOrder(User user, List<OrderItem> cartItems, String shippingAddress, String phoneNumber, String paymentMethod, double discountPercent) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order with an empty shopping cart.");
        }

        // Validate stock before taking action
        for (OrderItem item : cartItems) {
            Product product = productService.findById(item.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("Product not found: " + item.getProductName()));
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for instrument: " + product.getName() + 
                        " (Available: " + product.getStockQuantity() + ")");
            }
        }

        // Deduct inventory stock
        for (OrderItem item : cartItems) {
            productService.adjustStock(item.getProductId(), -item.getQuantity());
        }

        // Compute subtotal, discount, tax and final total amount
        double subtotal = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        double discountAmount = subtotal * (Math.max(0.0, Math.min(100.0, discountPercent)) / 100.0);
        double tax = (subtotal - discountAmount) * 0.10;
        double total = (subtotal - discountAmount) + tax;

        Order order = new Order();
        order.setId("o-" + UUID.randomUUID().toString().substring(0, 8));
        order.setUserId(user.getId());
        order.setCustomerName(user.getName());
        order.setCustomerEmail(user.getEmail());
        order.setShippingAddress(shippingAddress);
        order.setPhoneNumber(phoneNumber);
        order.setItems(cartItems);
        order.setTotalAmount(Math.round(total * 100.0) / 100.0);
        order.setPaymentStatus("PAID"); // Simulated successful payment check
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);

        List<Order> orders = getAllOrders();
        orders.add(order);
        fileStorageService.writeList(ORDERS_FILE, orders);

        return order;
    }

    /**
     * Updates status of an order (e.g. from PENDING to SHIPPED).
     */
    public synchronized Order updateOrderStatus(String orderId, OrderStatus status) {
        List<Order> orders = getAllOrders();
        for (Order o : orders) {
            if (o.getId().equals(orderId)) {
                o.setOrderStatus(status);
                fileStorageService.writeList(ORDERS_FILE, orders);
                return o;
            }
        }
        throw new NoSuchElementException("Order with ID " + orderId + " not found.");
    }
}
