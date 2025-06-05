package com.gamevision.nextalk.chat;

import androidx.lifecycle.MutableLiveData;

import com.gamevision.nextalk.models.Media;
import com.gamevision.nextalk.models.Message;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public String generateChatId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0
                ? userId1 + "_" + userId2
                : userId2 + "_" + userId1;
    }

    public void checkOrCreateChat(String chatId, String currentUserId, String otherUserId) {
        DocumentReference chatRef = db.collection("chats").document(chatId);

        chatRef.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Map<String, Object> chatData = new HashMap<>();
                chatData.put("chatId", chatId);
                chatData.put("user1Id", currentUserId);
                chatData.put("user2Id", otherUserId);
                chatData.put("lastMessage", "");
                chatData.put("lastMessageSenderId", "");
                chatData.put("seen", true);
                chatData.put("participants", new HashMap<String, Boolean>() {{
                    put(currentUserId, true);
                    put(otherUserId, true);
                }});
                chatRef.set(chatData);
            } else {
                String lastSenderId = snapshot.getString("lastMessageSenderId");
                Boolean seen = snapshot.getBoolean("seen");

                if (lastSenderId != null && lastSenderId.equals(otherUserId) && Boolean.FALSE.equals(seen)) {
                    chatRef.update("seen", true);
                }
            }
        });
    }

    public void loadMessages(String chatId, String currentUserId, String otherUserId, MutableLiveData<List<Message>> messagesLiveData) {
        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<Message> tempList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : value) {
                        Message message = document.toObject(Message.class);

                        if (message.getSenderId().equals(otherUserId) && !message.isSeen()) {
                            db.collection("chats").document(chatId)
                                    .collection("messages")
                                    .document(message.getMessageId())
                                    .update("seen", true);
                        }

                        tempList.add(message);
                    }

                    messagesLiveData.setValue(tempList);
                });
    }

    public void sendMessage(String chatId, String currentUserId, String otherUserId, String messageText) {
        DocumentReference messageRef = db.collection("chats").document(chatId)
                .collection("messages").document();

        Message message = new Message(
                messageRef.getId(), chatId, currentUserId, otherUserId, messageText
        );

        messageRef.set(message)
                .addOnSuccessListener(unused -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("lastMessage", messageText);
                    update.put("lastMessageSenderId", currentUserId);
                    update.put("lastMessageTimestamp", FieldValue.serverTimestamp());
                    update.put("seen", false);

                    db.collection("chats").document(chatId).update(update);
                });
    }

    public void sendMediaMessage(String chatId, String currentUserId, String otherUserId, Media media, String caption) {
        DocumentReference messageRef = db.collection("chats").document(chatId)
                .collection("messages").document();

        Message message = new Message(
                chatId,
                caption,
                media,
                messageRef.getId(),
                otherUserId,
                true,
                currentUserId,
                new Date(),
                "media"
        );

        messageRef.set(message)
                .addOnSuccessListener(unused -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("lastMessage", caption.isEmpty() ? "Media" : caption);
                    update.put("lastMessageSenderId", currentUserId);
                    update.put("lastMessageTimestamp", FieldValue.serverTimestamp());
                    update.put("seen", false);

                    db.collection("chats").document(chatId).update(update);
                });
    }
}
