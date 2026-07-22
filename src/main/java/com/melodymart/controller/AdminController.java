package com.melodymart.controller;

import com.melodymart.config.AdminRequired;
import com.melodymart.model.Category;
import com.melodymart.model.OrderStatus;
import com.melodymart.model.Product;
import com.melodymart.model.RentalStatus;
import com.melodymart.service.ChatService;
import com.melodymart.service.OrderService;
import com.melodymart.service.ProductService;
import com.melodymart.service.RentalService;
import com.melodymart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.melodymart.model.BannerConfig;
import com.melodymart.service.BannerService;
import com.melodymart.model.TickerConfig;
import com.melodymart.service.TickerService;

@Controller
@RequestMapping("/admin")
@AdminRequired
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private UserService userService;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private TickerService tickerService;

    @Autowired
    private ChatService chatService;

    /**
     * Renders administrator central metrics dashboard.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Calculate key store metrics
        long totalProducts = productService.getAllProducts().size();
        long activeRentals = rentalService.getAllRentals().stream()
                .filter(r -> r.getRentalStatus() == RentalStatus.ACTIVE)
                .count();
        double totalSales = orderService.getAllOrders().stream()
                .mapToDouble(o -> o.getTotalAmount())
                .sum();
        double totalRentalIncome = rentalService.getAllRentals().stream()
                .mapToDouble(r -> r.getTotalCost())
                .sum();
        long totalCustomers = userService.getAllUsers().stream()
                .filter(u -> u.getRole() == com.melodymart.model.Role.CUSTOMER)
                .count();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeRentals", activeRentals);
        model.addAttribute("totalSales", Math.round(totalSales * 100.0) / 100.0);
        model.addAttribute("totalRentalIncome", Math.round(totalRentalIncome * 100.0) / 100.0);
        model.addAttribute("totalCustomers", totalCustomers);

        // Fetch recent lists
        model.addAttribute("recentOrders", orderService.getAllOrders().stream()
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .limit(5)
                .toList());

        model.addAttribute("recentRentals", rentalService.getAllRentals().stream()
                .sorted((r1, r2) -> r2.getBookingDate().compareTo(r1.getBookingDate()))
                .limit(5)
                .toList());

        model.addAttribute("orderStatuses", OrderStatus.values());

        return "admin/dashboard";
    }

    /**
     * Lists all products for inventory management.
     */
    @GetMapping("/inventory")
    public String inventory(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/inventory";
    }

    /**
     * Form to add a new product.
     */
    @GetMapping("/inventory/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", Category.values());
        model.addAttribute("isEdit", false);
        return "admin/product-form";
    }

    /**
     * Form to edit an existing product.
     */
    @GetMapping("/inventory/edit/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model) {
        Product product = productService.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/admin/inventory?error=Product+not+found";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", Category.values());
        model.addAttribute("isEdit", true);
        return "admin/product-form";
    }

    /**
     * Handles adding or editing a product, parsing dynamic key-value specifications.
     */
    @PostMapping("/inventory/save")
    public String saveProduct(
            @ModelAttribute Product product,
            @RequestParam(value = "specKeys", required = false) List<String> specKeys,
            @RequestParam(value = "specValues", required = false) List<String> specValues) {

        Map<String, String> specs = new HashMap<>();
        if (specKeys != null && specValues != null) {
            for (int i = 0; i < specKeys.size(); i++) {
                String key = specKeys.get(i).trim();
                String val = specValues.get(i).trim();
                if (!key.isEmpty() && !val.isEmpty()) {
                    specs.put(key, val);
                }
            }
        }
        product.setSpecifications(specs);
        productService.save(product);
        return "redirect:/admin/inventory?saved=true";
    }

    /**
     * Handles product deletion.
     */
    @PostMapping("/inventory/delete/{id}")
    public String deleteProduct(@PathVariable("id") String id) {
        try {
            productService.delete(id);
            return "redirect:/admin/inventory?deleted=true";
        } catch (Exception e) {
            return "redirect:/admin/inventory?error=" + e.getMessage();
        }
    }

    /**
     * Updates status of an order.
     */
    @PostMapping("/orders/status")
    public String updateOrderStatus(
            @RequestParam("orderId") String orderId,
            @RequestParam("status") OrderStatus status) {
        try {
            orderService.updateOrderStatus(orderId, status);
            return "redirect:/admin/dashboard?statusUpdated=true";
        } catch (Exception e) {
            return "redirect:/admin/dashboard?error=" + e.getMessage();
        }
    }
    /**
     * Lists all registered users.
     */
    @GetMapping("/users")
    public String users(Model model) {
        List<com.melodymart.model.User> users = userService.getAllUsers();
        long customerCount = users.stream().filter(u -> u.getRole() == com.melodymart.model.Role.CUSTOMER).count();
        long adminCount = users.stream().filter(u -> u.getRole() == com.melodymart.model.Role.ADMIN).count();

        model.addAttribute("users", users);
        model.addAttribute("customerCount", customerCount);
        model.addAttribute("adminCount", adminCount);
        return "admin/users";
    }

    /**
     * Lists all purchase orders.
     */
    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/orders";
    }

    /**
     * Lists all rentals.
     */
    @GetMapping("/rentals")
    public String rentals(Model model) {
        model.addAttribute("rentals", rentalService.getAllRentals());
        return "admin/rentals";
    }

    /**
     * Manages Welcome Banner settings.
     */
    @GetMapping("/banner")
    public String bannerSettings(Model model) {
        model.addAttribute("bannerConfig", bannerService.getBannerConfig());
        return "admin/banner";
    }

    @PostMapping("/banner/update")
    public String updateBanner(
            @RequestParam(value = "enabled", required = false, defaultValue = "false") boolean enabled,
            @RequestParam("badgeText") String badgeText,
            @RequestParam("title") String title,
            @RequestParam("subtitle") String subtitle,
            @RequestParam("couponCode") String couponCode,
            @RequestParam("imageUrl") String imageUrl,
            @RequestParam("buttonText") String buttonText,
            @RequestParam("buttonLink") String buttonLink) {

        BannerConfig config = new BannerConfig(enabled, badgeText, title, subtitle, couponCode, imageUrl, buttonText, buttonLink);
        bannerService.saveBannerConfig(config);
        return "redirect:/admin/banner?saved=true";
    }

    /**
     * Manages News Ticker settings.
     */
    @GetMapping("/ticker")
    public String tickerSettings(Model model) {
        model.addAttribute("tickerConfig", tickerService.getTickerConfig());
        return "admin/ticker";
    }

    @PostMapping("/ticker/update")
    public String updateTicker(@RequestParam("newsTickerText") String newsTickerText) {
        TickerConfig config = new TickerConfig(newsTickerText);
        tickerService.saveTickerConfig(config);
        return "redirect:/admin/ticker?saved=true";
    }

    /**
     * Live Chat support panel — view all customer conversations.
     */
    @GetMapping("/chat")
    public String chat() {
        return "admin/chat";
    }
}
