package com.gamevision.nextalk.auth;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.gamevision.nextalk.chat.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public LiveData<FirebaseUser> login(String email, String password) {
        MutableLiveData<FirebaseUser> userData = new MutableLiveData<>();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> userData.setValue(authResult.getUser()))
                .addOnFailureListener(e -> userData.setValue(null));
        return userData;
    }

    public LiveData<FirebaseUser> signup(String email, String password, String name, String birthDate, String about, Uri imageUri) {
        MutableLiveData<FirebaseUser> userData = new MutableLiveData<>();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    userData.setValue(authResult.getUser());
                    uploadImage(authResult.getUser().getUid(),email, password, name, birthDate, about, imageUri);
                })
                .addOnFailureListener(e -> userData.setValue(null));
        return userData;
    }
    public void saveUserData(String userId,String name,String birthDate,String about,String imageUrl,String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("name", name);
        userData.put("birthDate", birthDate);
        userData.put("about", about);
        userData.put("email", email);
        userData.put("imageUrl", imageUrl);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(unused -> {
                    Log.d("Firestore", "User data saved successfully");
                    userData.put("profileCompleted", true); // Only added if profile is saved successfully

                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error saving user data", e);
                });
    }

    private void uploadImage(String userId, String email, String password, String name, String birthDate, String about, Uri imageUri) {
        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                    .option("folder", "zyntra_profiles")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = resultData.get("secure_url").toString();
                            Log.d("Cloudinary", "Image URL: " + imageUrl);
                            saveUserData(userId,name,birthDate,about,imageUrl,email);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e("Cloudinary", "Upload Error: " + error.getDescription());
                            saveUserData(userId,name,birthDate,about,null,email);
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    }).dispatch();
        } else {
            // No image selected
            saveUserData(userId,name,birthDate,about,null,email);
        }
    }
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
}
