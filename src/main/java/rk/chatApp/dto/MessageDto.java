package rk.chatApp.dto;

import rk.chatApp.model.Message;
import java.time.LocalDateTime;

public class MessageDto {
    private Long id;
    private String content;
    private LocalDateTime timestamp;
    private String username;
    private Long groupId;

    public MessageDto() {
    }

    public MessageDto(Message message) {
        this.id = message.getId();
        this.content = message.getContent();
        this.timestamp = message.getTimestamp();
        this.username = message.getUser().getUsername();
        this.groupId = message.getGroup().getId();
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}