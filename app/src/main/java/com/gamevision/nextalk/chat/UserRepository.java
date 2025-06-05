package com.gamevision.nextalk.chat;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.gamevision.nextalk.models.ChatSummaryModel;
import com.gamevision.nextalk.models.UserModel;
import com.gamevision.nextalk.models.UserWithLastMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUid = FirebaseAuth.getInstance().getUid();
    String chatId;
    public void fetchUsersWithLastMessages(MutableLiveData<List<UserWithLastMessage>> liveData) {
        db.collection("users").get().addOnSuccessListener(userSnapshots -> {
            List<UserWithLastMessage> result = new ArrayList<>();

            for (DocumentSnapshot userDoc : userSnapshots) {
                String userId = userDoc.getId();
                if (userId.equals(currentUid)) continue;
                UserModel user = userDoc.toObject(UserModel.class);
                if (user != null) {
                    user.setUid(userId);
                    user.setProfileUrl(userDoc.getString("imageUrl"));
                }

                if (user != null) {
                    db.collection("chats")
                            .whereEqualTo("participants." + currentUid, true)
                            .whereEqualTo("participants." + userId, true)
                            .orderBy("lastMessageTimestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener(chatDocs -> {
                                ChatSummaryModel lastMessage = null;
                                if (!chatDocs.isEmpty()) {
                                    lastMessage = chatDocs.getDocuments().get(0).toObject(ChatSummaryModel.class);
                                }

                                UserWithLastMessage model = new UserWithLastMessage(user, lastMessage);
                                result.add(model);
                                liveData.setValue(new ArrayList<>(result)); // Triggers observer update
                            });
                }
            }
        });
    }
    public void searchUsers(String query, OnUsersFetchedCallback callback) {
        FirebaseFirestore.getInstance().collection("Users")
                .orderBy("name") // Ensure "name" is indexed in Firestore
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<UserModel> filteredUsers = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        UserModel user = doc.toObject(UserModel.class);
                        filteredUsers.add(user);
                    }
                    callback.onSuccess(filteredUsers);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

}
