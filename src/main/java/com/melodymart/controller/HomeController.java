package com.melodymart.controller;

import com.melodymart.model.Category;
import com.melodymart.model.Product;
import com.melodymart.model.User;
import com.melodymart.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.melodymart.service.BannerService;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private BannerService bannerService;

    @GetMapping("/")
    public String home(Model model) {
        List<Product> allProducts = productService.getAllProducts();

        // Tab 1: Top Rated — highest priced (proxy for premium items)
        List<Product> topRated = allProducts.stream()
                .sorted((a, b) -> Double.compare(b.getPrice(), a.getPrice()))
                .limit(6)
                .collect(Collectors.toList());

        // Tab 2: New Arrivals — last added items
        List<Product> newArrivals = allProducts.stream()
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .limit(6)
                .collect(Collectors.toList());

        // Tab 3: Best Rentals — items available for rent, sorted by lowest rental price
        List<Product> bestRentals = allProducts.stream()
                .filter(p -> p.isForRent() && p.getRentalPricePerDay() > 0)
                .sorted((a, b) -> Double.compare(a.getRentalPricePerDay(), b.getRentalPricePerDay()))
                .limit(6)
                .collect(Collectors.toList());

        model.addAttribute("topRated", topRated);
        model.addAttribute("newArrivals", newArrivals);
        model.addAttribute("bestRentals", bestRentals);
        model.addAttribute("categories", Category.values());
        model.addAttribute("bannerConfig", bannerService.getBannerConfig());
        return "index";
    }

    @GetMapping("/catalog")
    public String catalogRedirect() {
        return "redirect:/catalog/buy";
    }

    @GetMapping("/catalog/buy")
    public String catalogBuy(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "category", required = false) Category category,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "inStockOnly", required = false) Boolean inStockOnly,
            Model model) {

        List<Product> products = productService.searchAndFilter(query, category, minPrice, maxPrice, inStockOnly, true, null);

        model.addAttribute("products", products);
        model.addAttribute("categories", Category.values());
        model.addAttribute("isBuyPage", true);
        model.addAttribute("isRentPage", false);
        
        // Preserve parameters
        model.addAttribute("selectedQuery", query);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedInStockOnly", inStockOnly != null && inStockOnly);

        return "catalog";
    }

    @GetMapping("/catalog/rent")
    public String catalogRent(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "category", required = false) Category category,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "inStockOnly", required = false) Boolean inStockOnly,
            Model model) {

        List<Product> products = productService.searchAndFilter(query, category, minPrice, maxPrice, inStockOnly, null, true);

        model.addAttribute("products", products);
        model.addAttribute("categories", Category.values());
        model.addAttribute("isBuyPage", false);
        model.addAttribute("isRentPage", true);
        
        // Preserve parameters
        model.addAttribute("selectedQuery", query);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        model.addAttribute("selectedInStockOnly", inStockOnly != null && inStockOnly);

        return "catalog";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") String id, HttpSession session, Model model) {
        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            model.addAttribute("product", product);

            // Check if current user is the owner
            User currentUser = (User) session.getAttribute("currentUser");
            boolean isOwner = currentUser != null && product.getOwnerId().equals(currentUser.getId());
            model.addAttribute("isOwner", isOwner);

            return "product-detail";
        }
        return "redirect:/error?message=Product+not+found.";
    }
}
