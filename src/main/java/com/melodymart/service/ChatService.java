package com.melodymart.service;

import com.melodymart.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private FileStorageService fileStorageService;

    private String chatFile(String userId) {
        return "chat_" + userId.replaceAll("[^a-zA-Z0-9_-]", "_") + ".json";
    }

    /**
     * Send a message (from user or from admin to a specific user).
     */
    public ChatMessage sendMessage(String userId, String userName, String content, boolean fromAdmin) {
        List<ChatMessage> messages = getMessages(userId);
        ChatMessage msg = new ChatMessage(
                UUID.randomUUID().toString(),
                userId,
                userName,
                content,
                LocalDateTime.now(),
                fromAdmin
        );
        messages.add(msg);
        fileStorageService.writeList(chatFile(userId), messages);
        return msg;
    }

    /**
     * Get all messages for a specific user conversation.
     */
    public List<ChatMessage> getMessages(String userId) {
        return new ArrayList<>(fileStorageService.readList(chatFile(userId), ChatMessage.class));
    }

    /**
     * Mark all messages from a user as read by admin.
     */
    public void markAllReadByAdmin(String userId) {
        List<ChatMessage> messages = getMessages(userId);
        messages.forEach(m -> {
            if (!m.isFromAdmin()) m.setReadByAdmin(true);
        });
        fileStorageService.writeList(chatFile(userId), messages);
    }

    /**
     * Get count of unread messages from a specific user.
     */
    public long getUnreadCount(String userId) {
        return getMessages(userId).stream()
                .filter(m -> !m.isFromAdmin() && !m.isReadByAdmin())
                .count();
    }

    /**
     * Get total unread messages across all users (for admin badge).
     */
    public long getTotalUnreadCount(List<String> userIds) {
        return userIds.stream().mapToLong(this::getUnreadCount).sum();
    }

    /**
     * Get list of user IDs that have sent at least one message.
     */
    public List<String> getActiveUserIds(List<com.melodymart.model.User> allUsers) {
        return allUsers.stream()
                .filter(u -> u.getRole() == com.melodymart.model.Role.CUSTOMER)
                .filter(u -> {
                    List<ChatMessage> msgs = getMessages(u.getId());
                    return !msgs.isEmpty();
                })
                .map(com.melodymart.model.User::getId)
                .collect(Collectors.toList());
    }

    /**
     * Get last message timestamp for sorting conversations.
     */
    public Optional<ChatMessage> getLastMessage(String userId) {
        List<ChatMessage> msgs = getMessages(userId);
        if (msgs.isEmpty()) return Optional.empty();
        return Optional.of(msgs.get(msgs.size() - 1));
    }
}
