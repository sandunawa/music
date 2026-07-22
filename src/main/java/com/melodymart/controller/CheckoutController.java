package com.melodymart.controller;

import com.melodymart.config.LoginRequired;
import com.melodymart.model.*;
import com.melodymart.service.InvoiceService;
import com.melodymart.service.OrderService;
import com.melodymart.service.ProductService;
import com.melodymart.service.RentalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@LoginRequired
public class CheckoutController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private InvoiceService invoiceService;

    @SuppressWarnings("unchecked")
    private Map<String, Integer> getCartFromSession(HttpSession session) {
        Map<String, Integer> cart = (Map<String, Integer>) session.getAttribute("cart");
        return (cart != null) ? cart : new HashMap<>();
    }

    private double parseCouponDiscount(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) return 0.0;
        String code = couponCode.trim().toUpperCase();
        if (code.equals("SPIN5")) return 5.0;
        if (code.equals("SPIN10")) return 10.0;
        if (code.equals("SPIN15")) return 15.0;
        if (code.equals("SPIN20")) return 20.0;
        if (code.equals("SPIN25")) return 25.0;
        if (code.equals("SPIN50")) return 50.0;
        if (code.equals("MELODY15")) return 15.0;
        return 0.0;
    }

    /**
     * Shows order checkout page.
     */
    @GetMapping("/checkout")
    public String showCheckout(
            @RequestParam(value = "couponCode", required = false) String couponCode,
            HttpSession session, Model model) {
        Map<String, Integer> cartSession = getCartFromSession(session);
        if (cartSession.isEmpty()) {
            return "redirect:/cart?error=empty";
        }

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

        double discountPercent = parseCouponDiscount(couponCode);
        double discountAmount = subtotal * (discountPercent / 100.0);
        double tax = (subtotal - discountAmount) * 0.10;
        double total = (subtotal - discountAmount) + tax;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", Math.round(subtotal * 100.0) / 100.0);
        model.addAttribute("couponCode", couponCode != null ? couponCode.trim().toUpperCase() : "");
        model.addAttribute("discountPercent", (int) Math.round(discountPercent));
        model.addAttribute("discountAmount", Math.round(discountAmount * 100.0) / 100.0);
        model.addAttribute("tax", Math.round(tax * 100.0) / 100.0);
        model.addAttribute("total", Math.round(total * 100.0) / 100.0);

        // Prepopulate shipping fields from current user details if available
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("user", currentUser);

        return "checkout";
    }

    /**
     * Submits payment and places the order.
     */
    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam("shippingAddress") String shippingAddress,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("cardNumber") String cardNumber,
            @RequestParam("cardExpiry") String cardExpiry,
            @RequestParam("cardCvv") String cardCvv,
            @RequestParam(value = "couponCode", required = false) String couponCode,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        Map<String, Integer> cartSession = getCartFromSession(session);

        if (cartSession.isEmpty()) {
            return "redirect:/cart?error=empty";
        }

        // Card validations
        if (cardNumber.replaceAll("\\s", "").length() < 16 || cardCvv.trim().length() < 3) {
            model.addAttribute("error", "Invalid credit card details provided.");
            return showCheckout(couponCode, session, model);
        }

        // Map cart session items to OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : cartSession.entrySet()) {
            Product product = productService.findById(entry.getKey()).orElse(null);
            if (product != null) {
                orderItems.add(new OrderItem(product.getId(), product.getName(), entry.getValue(), product.getPrice()));
            }
        }

        try {
            double discountPercent = parseCouponDiscount(couponCode);
            Order placedOrder = orderService.placeOrder(currentUser, orderItems, shippingAddress, phoneNumber, paymentMethod, discountPercent);
            
            // Clear shopping cart session
            session.setAttribute("cart", new HashMap<String, Integer>());
            
            return "redirect:/invoice/order/" + placedOrder.getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return showCheckout(couponCode, session, model);
        }
    }

    /**
     * Renders printable invoice for an Order.
     */
    @GetMapping("/invoice/order/{orderId}")
    public String showOrderInvoice(@PathVariable("orderId") String orderId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        Order order = orderService.findById(orderId).orElse(null);

        if (order == null) {
            return "redirect:/error?message=Order+not+found.";
        }

        // Security check: Only owner or admin can view
        if (currentUser.getRole() != Role.ADMIN && !order.getUserId().equals(currentUser.getId())) {
            return "redirect:/error?message=Access+denied.";
        }

        Invoice invoice = invoiceService.generateOrderInvoice(order);
        model.addAttribute("invoice", invoice);
        return "invoice";
    }

    /**
     * Renders printable invoice for a Rental.
     */
    @GetMapping("/invoice/rental/{rentalId}")
    public String showRentalInvoice(@PathVariable("rentalId") String rentalId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        Rental rental = rentalService.findById(rentalId).orElse(null);

        if (rental == null) {
            return "redirect:/error?message=Rental+record+not+found.";
        }

        // Security check: Only owner or admin can view
        if (currentUser.getRole() != Role.ADMIN && !rental.getUserId().equals(currentUser.getId())) {
            return "redirect:/error?message=Access+denied.";
        }

        Invoice invoice = invoiceService.generateRentalInvoice(rental);
        model.addAttribute("invoice", invoice);
        return "invoice";
    }

    /**
     * Renders printable parcel shipping label with recipient details, photo, and barcode for sellers.
     */
    @GetMapping("/shipping-label/order/{orderId}")
    public String showShippingLabel(@PathVariable("orderId") String orderId, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        Order order = orderService.findById(orderId).orElse(null);

        if (order == null) {
            return "redirect:/error?message=Order+not+found.";
        }

        // Map products for image & details lookup
        Map<String, Product> productsMap = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            productService.findById(item.getProductId()).ifPresent(p -> productsMap.put(p.getId(), p));
        }

        model.addAttribute("order", order);
        model.addAttribute("productsMap", productsMap);
        model.addAttribute("currentUser", currentUser);
        return "shipping-label";
    }
}
