package com.melodymart.service;

import com.melodymart.model.Role;
import com.melodymart.model.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final String USERS_FILE = "users.json";

    @Autowired
    private FileStorageService fileStorageService;

    public List<User> getAllUsers() {
        return fileStorageService.readList(USERS_FILE, User.class);
    }

    public Optional<User> findById(String id) {
        return getAllUsers().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return getAllUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    /**
     * Registers a new user. Checks for email duplication, hashes password, assigns ID.
     */
    public synchronized User register(User user) {
        if (findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email address is already in use.");
        }

        List<User> users = getAllUsers();
        
        // Setup default fields
        user.setId("u-" + UUID.randomUUID().toString().substring(0, 8));
        user.setPasswordHash(BCrypt.hashpw(user.getPasswordHash(), BCrypt.gensalt()));
        
        if (user.getRole() == null) {
            user.setRole(Role.CUSTOMER);
        }

        users.add(user);
        fileStorageService.writeList(USERS_FILE, users);
        return user;
    }

    /**
     * Authenticates a user based on email and raw password.
     */
    public User login(String email, String password) {
        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                return user; // Success
            }
        }
        return null; // Invalid credentials
    }

    /**
     * Updates an existing user's profile details.
     */
    public synchronized User updateProfile(String id, User updatedDetails, String newPassword) {
        List<User> users = getAllUsers();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user.getId().equals(id)) {
                // Validate email uniqueness if it's changing
                if (!user.getEmail().equalsIgnoreCase(updatedDetails.getEmail()) && 
                    findByEmail(updatedDetails.getEmail()).isPresent()) {
                    throw new IllegalArgumentException("New email address is already in use.");
                }

                user.setName(updatedDetails.getName());
                user.setEmail(updatedDetails.getEmail());
                user.setPhoneNumber(updatedDetails.getPhoneNumber());
                user.setAddress(updatedDetails.getAddress());
                user.setProfilePicture(updatedDetails.getProfilePicture());

                // Update password if a new one is provided
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    user.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
                }

                fileStorageService.writeList(USERS_FILE, users);
                return user;
            }
        }
        throw new NoSuchElementException("User profile not found.");
    }
}
