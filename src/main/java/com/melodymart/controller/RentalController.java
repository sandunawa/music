package com.melodymart.controller;

import com.melodymart.config.LoginRequired;
import com.melodymart.model.Product;
import com.melodymart.model.Rental;
import com.melodymart.model.Role;
import com.melodymart.model.User;
import com.melodymart.service.ProductService;
import com.melodymart.service.RentalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

@Controller
@LoginRequired
public class RentalController {

    @Autowired
    private RentalService rentalService;

    @Autowired
    private ProductService productService;

    /**
     * Renders rental booking form.
     */
    @GetMapping("/rental/book/{productId}")
    public String showRentalBookingForm(
            @PathVariable("productId") String productId,
            Model model) {

        Product product = productService.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/error?message=Instrument+not+found.";
        }

        model.addAttribute("product", product);
        model.addAttribute("minStartDate", LocalDate.now());
        model.addAttribute("minEndDate", LocalDate.now().plusDays(1));
        return "rental-checkout";
    }

    /**
     * Processes rental booking and payment.
     */
    @PostMapping("/rental/book/{productId}")
    public String processRentalBooking(
            @PathVariable("productId") String productId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("cardNumber") String cardNumber,
            @RequestParam("cardExpiry") String cardExpiry,
            @RequestParam("cardCvv") String cardCvv,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("currentUser");
        Product product = productService.findById(productId).orElse(null);

        if (product == null) {
            return "redirect:/error?message=Product+not+found.";
        }

        // Basic credit card validation
        if (cardNumber.replaceAll("\\s", "").length() < 16 || cardCvv.trim().length() < 3) {
            model.addAttribute("error", "Invalid credit card details provided.");
            model.addAttribute("product", product);
            model.addAttribute("minStartDate", LocalDate.now());
            model.addAttribute("minEndDate", LocalDate.now().plusDays(1));
            return "rental-checkout";
        }

        try {
            Rental rental = rentalService.bookRental(currentUser, productId, startDate, endDate, paymentMethod);
            return "redirect:/invoice/rental/" + rental.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("product", product);
            model.addAttribute("minStartDate", LocalDate.now());
            model.addAttribute("minEndDate", LocalDate.now().plusDays(1));
            return "rental-checkout";
        }
    }

    /**
     * Handles returning an instrument.
     */
    @PostMapping("/rental/return/{rentalId}")
    public String processReturn(@PathVariable("rentalId") String rentalId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        Rental rental = rentalService.findById(rentalId).orElse(null);

        if (rental == null) {
            return "redirect:/error?message=Rental+record+not+found.";
        }

        // Only owner or admin can trigger return
        if (currentUser.getRole() != Role.ADMIN && !rental.getUserId().equals(currentUser.getId())) {
            return "redirect:/error?message=Access+denied.";
        }

        try {
            rentalService.returnInstrument(rentalId);
            return "redirect:/profile?returned=true";
        } catch (Exception e) {
            return "redirect:/profile?error=" + e.getMessage();
        }
    }
}
