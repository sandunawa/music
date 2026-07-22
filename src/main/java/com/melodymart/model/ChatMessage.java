package com.melodymart.model;

import java.time.LocalDateTime;

public class ChatMessage {
    private String id;
    private String userId;
    private String userName;
    private String content;
    private LocalDateTime timestamp;
    private boolean fromAdmin;
    private boolean readByAdmin;

    public ChatMessage() {}

    public ChatMessage(String id, String userId, String userName, String content,
                       LocalDateTime timestamp, boolean fromAdmin) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.timestamp = timestamp;
        this.fromAdmin = fromAdmin;
        this.readByAdmin = fromAdmin; // admin messages are auto-read
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isFromAdmin() { return fromAdmin; }
    public void setFromAdmin(boolean fromAdmin) { this.fromAdmin = fromAdmin; }

    public boolean isReadByAdmin() { return readByAdmin; }
    public void setReadByAdmin(boolean readByAdmin) { this.readByAdmin = readByAdmin; }
}
