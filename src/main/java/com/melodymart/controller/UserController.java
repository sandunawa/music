package com.melodymart.controller;

import com.melodymart.config.LoginRequired;
import com.melodymart.model.Category;
import com.melodymart.model.Order;
import com.melodymart.model.OrderStatus;
import com.melodymart.model.Product;
import com.melodymart.model.Rental;
import com.melodymart.model.User;
import com.melodymart.service.OrderService;
import com.melodymart.service.ProductService;
import com.melodymart.service.RentalService;
import com.melodymart.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@LoginRequired
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private ProductService productService;

    /**
     * Renders customer profile panel including past transactions and their active marketplace listings.
     */
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model,
                             @RequestParam(value = "returned", required = false) Boolean returned,
                             @RequestParam(value = "profileUpdated", required = false) Boolean profileUpdated,
                             @RequestParam(value = "listingsSaved", required = false) Boolean listingsSaved,
                             @RequestParam(value = "listingsDeleted", required = false) Boolean listingsDeleted,
                             @RequestParam(value = "statusUpdated", required = false) Boolean statusUpdated,
                             @RequestParam(value = "error", required = false) String error) {
        
        User currentUser = (User) session.getAttribute("currentUser");
        
        // Fetch specific history lists
        List<Order> orders = orderService.getOrdersByUserId(currentUser.getId());
        List<Rental> rentals = rentalService.getRentalsByUserId(currentUser.getId());
        List<Product> myListings = productService.getProductsByOwnerId(currentUser.getId());
        List<Order> receivedOrders = orderService.getOrdersReceivedForOwner(currentUser.getId());
        List<Rental> receivedRentals = rentalService.getRentalsReceivedForOwner(currentUser.getId());

        model.addAttribute("user", currentUser);
        model.addAttribute("orders", orders);
        model.addAttribute("rentals", rentals);
        model.addAttribute("myListings", myListings);
        model.addAttribute("receivedOrders", receivedOrders);
        model.addAttribute("receivedRentals", receivedRentals);
        model.addAttribute("orderStatuses", OrderStatus.values());

        // Feedback triggers
        if (returned != null && returned) {
            model.addAttribute("successMessage", "Instrument returned successfully!");
        }
        if (profileUpdated != null && profileUpdated) {
            model.addAttribute("successMessage", "Profile updated successfully!");
        }
        if (listingsSaved != null && listingsSaved) {
            model.addAttribute("successMessage", "Marketplace listing saved successfully!");
        }
        if (listingsDeleted != null && listingsDeleted) {
            model.addAttribute("successMessage", "Listing removed from marketplace.");
        }
        if (statusUpdated != null && statusUpdated) {
            model.addAttribute("successMessage", "Received order status updated successfully!");
        }
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "profile";
    }

    /**
     * Updates status of an order received for seller's listed instrument.
     */
    @PostMapping("/profile/orders/update-status")
    public String updateReceivedOrderStatus(@RequestParam("orderId") String orderId,
                                            @RequestParam("status") OrderStatus status,
                                            HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        Order order = orderService.findById(orderId).orElse(null);
        if (order == null) {
            return "redirect:/profile?error=Order+not+found";
        }

        // Check if current user is owner of any product in this order or admin
        boolean isOwner = order.getItems().stream().anyMatch(item -> {
            var p = productService.findById(item.getProductId());
            return p.isPresent() && currentUser.getId().equals(p.get().getOwnerId());
        });

        if (!isOwner && currentUser.getRole() != com.melodymart.model.Role.ADMIN) {
            return "redirect:/profile?error=Access+denied.+You+are+not+the+seller+of+this+order.";
        }

        orderService.updateOrderStatus(orderId, status);
        return "redirect:/profile?statusUpdated=true";
    }

    /**
     * Handles updates to the user profile details.
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "profilePicture", required = false) String profilePicture,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        User updatedUser = new User();
        updatedUser.setName(name);
        updatedUser.setEmail(email);
        updatedUser.setPhoneNumber(phoneNumber);
        updatedUser.setAddress(address);

        String savedPictureUrl = currentUser.getProfilePicture();

        // Process file upload if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                String ext = (originalFilename != null && originalFilename.contains(".")) 
                        ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                        : ".jpg";
                String filename = "profile-" + currentUser.getId() + "-" + System.currentTimeMillis() + ext;
                
                Path srcDir = Paths.get("src/main/resources/static/images/profiles");
                Path targetDir = Paths.get("target/classes/static/images/profiles");

                Files.createDirectories(srcDir);
                Files.createDirectories(targetDir);

                Path srcPath = srcDir.resolve(filename);
                Path targetPath = targetDir.resolve(filename);

                Files.copy(imageFile.getInputStream(), srcPath, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(imageFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                savedPictureUrl = "/images/profiles/" + filename;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (profilePicture != null && !profilePicture.trim().isEmpty()) {
            savedPictureUrl = profilePicture.trim();
        }

        updatedUser.setProfilePicture(savedPictureUrl);

        try {
            User savedUser = userService.updateProfile(currentUser.getId(), updatedUser, newPassword);
            session.setAttribute("currentUser", savedUser);
            return "redirect:/profile?profileUpdated=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", currentUser);
            model.addAttribute("orders", orderService.getOrdersByUserId(currentUser.getId()));
            model.addAttribute("rentals", rentalService.getRentalsByUserId(currentUser.getId()));
            model.addAttribute("myListings", productService.getProductsByOwnerId(currentUser.getId()));
            return "profile";
        }
    }

    // --- Peer-to-Peer Listings Handlers ---

    /**
     * Shows form for customers to add a listing.
     */
    @GetMapping("/profile/listings/add")
    public String showAddListingForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", Category.values());
        model.addAttribute("isEdit", false);
        return "user-product-form";
    }

    /**
     * Shows form for customers to edit their listing.
     */
    @GetMapping("/profile/listings/edit/{id}")
    public String showEditListingForm(@PathVariable("id") String id, HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        Product product = productService.findById(id).orElse(null);

        if (product == null) {
            return "redirect:/profile?error=Listing+not+found";
        }

        // Security check: Only owner can edit
        if (!product.getOwnerId().equals(currentUser.getId())) {
            return "redirect:/error?message=Access+denied.+You+are+not+the+owner+of+this+listing.";
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", Category.values());
        model.addAttribute("isEdit", true);
        return "user-product-form";
    }

    /**
     * Saves user marketplace listings.
     */
    @PostMapping("/profile/listings/save")
    public String saveUserListing(
            @ModelAttribute Product product,
            @RequestParam(value = "specKeys", required = false) List<String> specKeys,
            @RequestParam(value = "specValues", required = false) List<String> specValues,
            @RequestParam(value = "forSaleChecked", required = false) Boolean forSaleChecked,
            @RequestParam(value = "forRentChecked", required = false) Boolean forRentChecked,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("currentUser");

        // Validate listing types selection (must choose at least one option)
        boolean forSale = forSaleChecked != null && forSaleChecked;
        boolean forRent = forRentChecked != null && forRentChecked;
        
        if (!forSale && !forRent) {
            return "redirect:/profile/listings/add?error=You+must+list+the+instrument+for+Sale,+Rent,+or+both.";
        }

        // Check ownership if editing
        if (product.getId() != null && !product.getId().trim().isEmpty()) {
            Product existing = productService.findById(product.getId()).orElse(null);
            if (existing != null && !existing.getOwnerId().equals(currentUser.getId())) {
                return "redirect:/error?message=Access+denied.";
            }
        }

        // Set owner attributes
        product.setOwnerId(currentUser.getId());
        product.setOwnerName(currentUser.getName());
        product.setForSale(forSale);
        product.setForRent(forRent);

        // Adjust prices to 0 if not listed for that category
        if (!forSale) product.setPrice(0.0);
        if (!forRent) product.setRentalPricePerDay(0.0);

        // Parse specs
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

        return "redirect:/profile?listingsSaved=true";
    }

    /**
     * Deletes user listed product.
     */
    @PostMapping("/profile/listings/delete/{id}")
    public String deleteUserListing(@PathVariable("id") String id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        Product product = productService.findById(id).orElse(null);

        if (product == null) {
            return "redirect:/profile?error=Listing+not+found";
        }

        // Security check: Only owner can delete
        if (!product.getOwnerId().equals(currentUser.getId())) {
            return "redirect:/error?message=Access+denied.";
        }

        productService.delete(id);
        return "redirect:/profile?listingsDeleted=true";
    }
}
