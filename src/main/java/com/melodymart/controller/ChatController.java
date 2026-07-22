package com.melodymart.controller;

import com.melodymart.config.LoginRequired;
import com.melodymart.config.AdminRequired;
import com.melodymart.model.ChatMessage;
import com.melodymart.model.Role;
import com.melodymart.model.User;
import com.melodymart.service.ChatService;
import com.melodymart.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    // ─────────────────────────────────────────────────────────────────────
    // CUSTOMER ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Get messages for the currently logged-in user.
     */
    @GetMapping("/messages")
    @LoginRequired
    public ResponseEntity<List<ChatMessage>> getMyMessages(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return ResponseEntity.status(401).build();
        List<ChatMessage> messages = chatService.getMessages(user.getId());
        return ResponseEntity.ok(messages);
    }

    /**
     * Send a message as the currently logged-in user.
     */
    @PostMapping("/send")
    @LoginRequired
    public ResponseEntity<ChatMessage> sendMessage(
            @RequestParam("content") String content,
            HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        if (user == null) return ResponseEntity.status(401).build();
        if (content == null || content.trim().isEmpty()) return ResponseEntity.badRequest().build();

        ChatMessage msg = chatService.sendMessage(
                user.getId(), user.getName(), content.trim(), false);
        return ResponseEntity.ok(msg);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ADMIN ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Get all user conversations summary for admin panel.
     */
    @GetMapping("/admin/conversations")
    @AdminRequired
    public ResponseEntity<List<Map<String, Object>>> getConversations() {
        List<User> customers = userService.getAllUsers().stream()
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .collect(Collectors.toList());

        List<Map<String, Object>> conversations = new ArrayList<>();
        for (User customer : customers) {
            List<ChatMessage> msgs = chatService.getMessages(customer.getId());
            if (msgs.isEmpty()) continue;

            long unread = msgs.stream().filter(m -> !m.isFromAdmin() && !m.isReadByAdmin()).count();
            ChatMessage last = msgs.get(msgs.size() - 1);

            Map<String, Object> conv = new LinkedHashMap<>();
            conv.put("userId", customer.getId());
            conv.put("userName", customer.getName());
            conv.put("unreadCount", unread);
            conv.put("lastMessage", last.getContent());
            conv.put("lastTime", last.getTimestamp().toString());
            conv.put("messageCount", msgs.size());
            conversations.add(conv);
        }

        // Sort by most recent message
        conversations.sort((a, b) ->
                ((String) b.get("lastTime")).compareTo((String) a.get("lastTime")));

        return ResponseEntity.ok(conversations);
    }

    /**
     * Get all messages for a specific user (admin view).
     */
    @GetMapping("/admin/messages/{userId}")
    @AdminRequired
    public ResponseEntity<List<ChatMessage>> getUserMessages(@PathVariable String userId) {
        // Mark all as read by admin
        chatService.markAllReadByAdmin(userId);
        List<ChatMessage> messages = chatService.getMessages(userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Admin reply to a specific user.
     */
    @PostMapping("/admin/reply")
    @AdminRequired
    public ResponseEntity<ChatMessage> adminReply(
            @RequestParam("userId") String userId,
            @RequestParam("content") String content) {
        if (content == null || content.trim().isEmpty()) return ResponseEntity.badRequest().build();

        // Get user's name for context
        User user = userService.getAllUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst().orElse(null);
        String userName = user != null ? user.getName() : "Unknown User";

        ChatMessage msg = chatService.sendMessage(userId, userName, content.trim(), true);
        return ResponseEntity.ok(msg);
    }

    /**
     * Get total unread message count for admin badge.
     */
    @GetMapping("/admin/unread-count")
    @AdminRequired
    public ResponseEntity<Map<String, Long>> getTotalUnread() {
        List<String> customerIds = userService.getAllUsers().stream()
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .map(User::getId)
                .collect(Collectors.toList());
        long total = chatService.getTotalUnreadCount(customerIds);
        return ResponseEntity.ok(Map.of("count", total));
    }
}
