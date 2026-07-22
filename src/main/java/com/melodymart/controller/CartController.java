package com.melodymart.controller;

import com.melodymart.model.OrderItem;
import com.melodymart.model.Product;
import com.melodymart.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class CartController {

    @Autowired
    private ProductService productService;

    @SuppressWarnings("unchecked")
    private Map<String, Integer> getCartFromSession(HttpSession session) {
        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    /**
     * View full cart page.
     */
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        Map<String, Integer> cartSession = getCartFromSession(session);
        List<OrderItem> cartItems = new ArrayList<>();
        double subtotal = 0.0;

        for (Map.Entry<String, Integer> entry : cartSession.entrySet()) {
            Product product = productService.findById(entry.getKey()).orElse(null);
            if (product != null) {
                OrderItem item = new OrderItem(product.getId(), product.getName(), entry.getValue(), product.getPrice());
                cartItems.add(item);
                subtotal += item.getSubtotal();
            }
        }

        double tax = subtotal * 0.10; // 10% simulated VAT
        double total = subtotal + tax;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", Math.round(subtotal * 100.0) / 100.0);
        model.addAttribute("tax", Math.round(tax * 100.0) / 100.0);
        model.addAttribute("total", Math.round(total * 100.0) / 100.0);

        return "cart";
    }

    // --- REST-like AJAX API for Premium UX ---

    /**
     * AJAX endpoint to add item.
     */
    @PostMapping("/api/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam("productId") String productId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        Product product = productService.findById(productId).orElse(null);
        if (product == null) {
            response.put("success", false);
            response.put("message", "Product not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Map<String, Integer> cart = getCartFromSession(session);
        int currentQty = cart.getOrDefault(productId, 0);
        int newQty = currentQty + quantity;

        if (newQty > product.getStockQuantity()) {
            response.put("success", false);
            response.put("message", "Cannot add requested quantity. Only " + product.getStockQuantity() + " units available in inventory.");
            return ResponseEntity.ok(response);
        }

        cart.put(productId, newQty);
        session.setAttribute("cart", cart);

        int totalCount = cart.values().stream().mapToInt(Integer::intValue).sum();

        response.put("success", true);
        response.put("message", "Added '" + product.getName() + "' to cart successfully!");
        response.put("cartCount", totalCount);
        return ResponseEntity.ok(response);
    }

    /**
     * AJAX endpoint to update quantity.
     */
    @PostMapping("/api/cart/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartQuantity(
            @RequestParam("productId") String productId,
            @RequestParam("quantity") int quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        Product product = productService.findById(productId).orElse(null);
        if (product == null || quantity <= 0) {
            response.put("success", false);
            response.put("message", "Invalid product or quantity.");
            return ResponseEntity.badRequest().body(response);
        }

        if (quantity > product.getStockQuantity()) {
            response.put("success", false);
            response.put("message", "Only " + product.getStockQuantity() + " units available.");
            return ResponseEntity.ok(response);
        }

        Map<String, Integer> cart = getCartFromSession(session);
        cart.put(productId, quantity);
        session.setAttribute("cart", cart);

        // Compute updated totals
        double subtotal = 0.0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product p = productService.findById(entry.getKey()).orElse(null);
            if (p != null) {
                subtotal += p.getPrice() * entry.getValue();
            }
        }
        double tax = subtotal * 0.10;
        double total = subtotal + tax;
        int totalCount = cart.values().stream().mapToInt(Integer::intValue).sum();

        response.put("success", true);
        response.put("cartCount", totalCount);
        response.put("itemSubtotal", Math.round((product.getPrice() * quantity) * 100.0) / 100.0);
        response.put("subtotal", Math.round(subtotal * 100.0) / 100.0);
        response.put("tax", Math.round(tax * 100.0) / 100.0);
        response.put("total", Math.round(total * 100.0) / 100.0);

        return ResponseEntity.ok(response);
    }

    /**
     * AJAX endpoint to remove item.
     */
    @PostMapping("/api/cart/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @RequestParam("productId") String productId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        Map<String, Integer> cart = getCartFromSession(session);
        cart.remove(productId);
        session.setAttribute("cart", cart);

        // Recalculate
        double subtotal = 0.0;
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            Product p = productService.findById(entry.getKey()).orElse(null);
            if (p != null) {
                subtotal += p.getPrice() * entry.getValue();
            }
        }
        double tax = subtotal * 0.10;
        double total = subtotal + tax;
        int totalCount = cart.values().stream().mapToInt(Integer::intValue).sum();

        response.put("success", true);
        response.put("cartCount", totalCount);
        response.put("subtotal", Math.round(subtotal * 100.0) / 100.0);
        response.put("tax", Math.round(tax * 100.0) / 100.0);
        response.put("total", Math.round(total * 100.0) / 100.0);

        return ResponseEntity.ok(response);
    }
}
