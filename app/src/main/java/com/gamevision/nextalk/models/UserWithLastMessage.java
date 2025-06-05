package com.gamevision.nextalk.models;
public class UserWithLastMessage {
    private UserModel user;

    public ChatSummaryModel getSummary() {
        return summary;
    }

    public void setSummary(ChatSummaryModel summary) {
        this.summary = summary;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    private ChatSummaryModel summary;

    public UserWithLastMessage(UserModel user, ChatSummaryModel summary) {
        this.user = user;
        this.summary = summary;
    }

    // Getters
}
