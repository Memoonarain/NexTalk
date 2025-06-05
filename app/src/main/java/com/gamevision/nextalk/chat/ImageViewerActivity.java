package com.gamevision.nextalk.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.gamevision.nextalk.R;

public class ImageViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        ImageView imageView = findViewById(R.id.imageView);

        // Load image using Glide
        Glide.with(this)
            .load(imageUrl)
            .into(imageView);

        // Close activity on image click
        imageView.setOnClickListener(v -> finish());
    }
} 