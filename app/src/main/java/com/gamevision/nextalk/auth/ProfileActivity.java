package com.gamevision.nextalk.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.gamevision.nextalk.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePhoto;
    private TextView tvName, tvEmail, tvBirthDate, tvAbout;
    private Button btnEditProfile;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();
        
        // Load user data
        loadUserData();

        // Set up edit profile button
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ProfileUpdateActivity.class));
        });
    }

    private void initializeViews() {
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvBirthDate = findViewById(R.id.tvBirthDate);
        tvAbout = findViewById(R.id.tvAbout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
    }

    private void loadUserData() {
        String userId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Set user details
                        tvName.setText(document.getString("name"));
                        tvEmail.setText(document.getString("email"));
                        tvBirthDate.setText(document.getString("birthDate"));
                        tvAbout.setText(document.getString("about"));

                        // Load profile photo if exists
                        String profilePhotoUrl = document.getString("imageUrl");
                        if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
                            Glide.with(this)
                                .load(profilePhotoUrl)
                                .placeholder(R.drawable.icon_user)
                                .error(R.drawable.icon_user)
                                .into(ivProfilePhoto);
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user data when activity resumes
        loadUserData();
    }
}