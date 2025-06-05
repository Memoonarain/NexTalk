package com.gamevision.nextalk.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {
    private String messageId;
    private String chatId;
    private String senderId;
    private String receiverId;
    private Media media;
    private String type;

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public Message(String chatId, String content, Media media, String messageId, String receiverId, boolean seen, String senderId, Date timestamp, String type) {
        this.chatId = chatId;
        this.content = content;
        this.media = media;
        this.messageId = messageId;
        this.receiverId = receiverId;
        this.seen = seen;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String content;
    private @ServerTimestamp Date timestamp;
    private boolean seen;

    // Empty constructor for Firestore
    public Message() {
    }

    public Message(String messageId, String chatId, String senderId, String receiverId, String content) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.seen = false;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
} 