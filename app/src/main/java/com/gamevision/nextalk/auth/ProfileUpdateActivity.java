package com.gamevision.nextalk.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.gamevision.nextalk.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileUpdateActivity extends AppCompatActivity {

    private TextInputEditText etName, etBirthDate, etAbout;
    private TextView tvAddProfilePhoto;
    private ImageView ivProfilePhoto;
    private MaterialButton btnUpdate;
    private ImageButton btnBack;
    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_update);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        initializeViews();
        
        // Load current user data
        loadUserData();

        // Set up click listeners
        setupClickListeners();

        // Initialize Cloudinary if not already initialized
        initializeCloudinary();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etFullName);
        etBirthDate = findViewById(R.id.etBirthDate);
        etAbout = findViewById(R.id.etAbout);
        tvAddProfilePhoto = findViewById(R.id.tvAddPhoto);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadUserData() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etName.setText(documentSnapshot.getString("name"));
                        etBirthDate.setText(documentSnapshot.getString("birthDate"));
                        etAbout.setText(documentSnapshot.getString("about"));
                        // Load profile image if exists
                        String profileImageUrl = documentSnapshot.getString("imageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this).load(profileImageUrl).placeholder(R.drawable.icon_user).into(ivProfilePhoto);
                        }
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(ProfileUpdateActivity.this, "Error loading profile data", Toast.LENGTH_SHORT).show()
                );
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvAddProfilePhoto.setOnClickListener(v -> selectImage());

        etBirthDate.setOnClickListener(v -> showDatePicker());

        btnUpdate.setOnClickListener(v -> {
            if (validateInputs()) {
                updateProfile();
            }
        });
    }

    private void initializeCloudinary() {
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "du3kpasqd");
            config.put("api_key", "978589238943688");
            config.put("api_secret", "UBV5lxDjn_56OlOQM1CfU8-D2uU");
            MediaManager.init(this, config);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    etBirthDate.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private boolean validateInputs() {
        String name = etName.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String about = etAbout.getText().toString().trim();

        if (name.isEmpty() || birthDate.isEmpty() || about.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateProfile() {
        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etName.getText().toString().trim());
        updates.put("birthDate", etBirthDate.getText().toString().trim());
        updates.put("about", etAbout.getText().toString().trim());

        if (imageUri != null) {
            // Upload image to Cloudinary
            MediaManager.get().upload(imageUri)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            // Upload started
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            // Upload progress
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = (String) resultData.get("url");
                            updates.put("imageUrl", imageUrl);
                            updateFirestore(updates);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Toast.makeText(ProfileUpdateActivity.this, 
                                "Error uploading image", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            // Upload rescheduled
                        }
                    })
                    .dispatch();
        } else {
            updateFirestore(updates);
        }
    }

    private void updateFirestore(Map<String, Object> updates) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileUpdateActivity.this, 
                        "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(ProfileUpdateActivity.this, 
                        "Error updating profile", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ivProfilePhoto.setImageBitmap(bitmap);
                tvAddProfilePhoto.setText("Change Profile Photo");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}