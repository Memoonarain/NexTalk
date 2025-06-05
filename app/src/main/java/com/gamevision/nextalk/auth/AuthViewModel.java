package com.gamevision.nextalk.auth;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository = new AuthRepository();

    public LiveData<FirebaseUser> login(String email, String password) {
        return authRepository.login(email, password);
    }

    public LiveData<FirebaseUser> signup(String email, String password, String name, String birthDate, String about, Uri imageUri) {
        return authRepository.signup(email, password,name,birthDate,about,imageUri);
    }

    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
    }
}
