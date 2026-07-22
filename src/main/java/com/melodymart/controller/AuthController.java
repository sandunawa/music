package com.melodymart.controller;

import com.melodymart.model.Role;
import com.melodymart.model.User;
import com.melodymart.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLDecoder;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "redirect", required = false) String redirect, Model model, HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/"; // Already logged in
        }
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "redirect", required = false) String redirect,
            HttpSession session,
            Model model) {

        try {
            User user = userService.login(email, password);
            if (user != null) {
                session.setAttribute("currentUser", user);
                
                // Keep checkout/cart states or count in session
                if (session.getAttribute("cart") == null) {
                    session.setAttribute("cart", new java.util.HashMap<String, Integer>());
                }

                if (redirect != null && !redirect.trim().isEmpty()) {
                    return "redirect:" + URLDecoder.decode(redirect, "UTF-8");
                }
                
                if (user.getRole() == Role.ADMIN) {
                    return "redirect:/admin/dashboard";
                }
                return "redirect:/profile";
            }
            model.addAttribute("error", "Invalid email or password.");
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during authentication.");
        }

        model.addAttribute("email", email);
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("role") Role role,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            Model model) {

        User user = new User(null, name, email, password, role, phoneNumber, address);
        try {
            userService.register(user);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please check inputs.");
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}
