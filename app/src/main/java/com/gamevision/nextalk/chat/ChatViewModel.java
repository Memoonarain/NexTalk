package com.gamevision.nextalk.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gamevision.nextalk.models.Media;
import com.gamevision.nextalk.models.Message;

import java.util.List;

public class ChatViewModel extends ViewModel {

    private final ChatRepository repository;
    private final MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();

    public ChatViewModel() {
        repository = new ChatRepository();
    }

    public LiveData<List<Message>> getMessagesLiveData() {
        return messagesLiveData;
    }

    public String getChatId(String userId1, String userId2) {
        return repository.generateChatId(userId1, userId2);
    }

    public void checkOrCreateChat(String chatId, String currentUserId, String otherUserId) {
        repository.checkOrCreateChat(chatId, currentUserId, otherUserId);
    }

    public void loadMessages(String chatId, String currentUserId, String otherUserId) {
        repository.loadMessages(chatId, currentUserId, otherUserId, messagesLiveData);
    }

    public void sendMessage(String chatId, String currentUserId, String otherUserId, String messageText) {
        repository.sendMessage(chatId, currentUserId, otherUserId, messageText);
    }

    public void sendMediaMessage(String chatId, String currentUserId, String otherUserId, Media media, String caption) {
        repository.sendMediaMessage(chatId, currentUserId, otherUserId, media, caption);
    }
}
