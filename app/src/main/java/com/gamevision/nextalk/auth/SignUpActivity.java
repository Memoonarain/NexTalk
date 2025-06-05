package com.gamevision.nextalk.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.gamevision.nextalk.R;
import com.gamevision.nextalk.chat.MainActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    TextInputEditText etEmail,etPassword,etName,etBirthDate,etAbout;
    TextView tvAddProfilePhoto, tvLogin;
    Button btnSignUp;
    ImageView ivProfilePhoto;
    Uri imageUri;
    AuthViewModel viewModel;
    private static final int PICK_IMAGE_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewModel = new AuthViewModel();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etFullName);
        etBirthDate = findViewById(R.id.etBirthDate);
        etAbout = findViewById(R.id.etAbout);
        tvLogin = findViewById(R.id.tvLogin);
        tvAddProfilePhoto = findViewById(R.id.tvAddPhoto);
        btnSignUp = findViewById(R.id.btnSignUp);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        try {
            MediaManager.get(); // Try to get instance
        }
        catch (IllegalStateException e) {
            // Not initialized yet, so initialize now
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "du3kpasqd");
            config.put("api_key", "978589238943688");
            config.put("api_secret", "UBV5lxDjn_56OlOQM1CfU8-D2uU");
            MediaManager.init(this, config);
        }
        tvLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        tvAddProfilePhoto.setOnClickListener(v -> selectImage());
        etBirthDate.setOnClickListener(v -> showDatePicker());

        btnSignUp.setOnClickListener(v -> {
            if (validateInputs()) {

                // Implement image upload logic here
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String name = etName.getText().toString();
                String birthDate = etBirthDate.getText().toString();
                String about = etAbout.getText().toString();
                viewModel.signup(email, password,name,birthDate,about,imageUri).observe(this, firebaseUser -> {
                    if (firebaseUser != null) {
                        Toast.makeText(SignUpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this, "Account Creation Failed", Toast.LENGTH_SHORT).show();
                    }
                });

            }else Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();

        });

    }
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format: dd/MM/yyyy
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
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etPassword.getText().toString();
        String Bio = etAbout.getText().toString();
        if (name.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()||Bio.isEmpty()|| etBirthDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            return false;
        }



        if (password.length() < 6) {
            etPassword.setError("Password should be at least 6 characters");
            return false;
        }

        return true;
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