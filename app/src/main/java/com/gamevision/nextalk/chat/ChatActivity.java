package com.gamevision.nextalk.chat;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.uploadwidget.model.Media;
import com.gamevision.nextalk.R;
import com.gamevision.nextalk.chat.MessageAdapter;
import com.gamevision.nextalk.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private ChatViewModel chatViewModel;
    private String chatId, otherUserId, currentUserId;
    private TextView tvName;
    private ImageView ivAvatar;
    private RecyclerView rvMessages;
    private EditText edtMessage;
    private Button btnSendMessage;
    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();
    private View mediaPreview;
    private ImageView ivMediaPreview;
    private TextView tvMediaName;
    private TextView tvMediaSize;
    private ImageButton btnRemoveMedia;
    private Uri selectedMediaUri;
    private String selectedMediaType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_chat);

        // Handle system bars insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Handle IME (keyboard) insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.inputLayout), (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            
            // Apply bottom padding to handle keyboard and navigation bar
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), 
                        v.getPaddingRight(), Math.max(imeInsets.bottom, navInsets.bottom));
            
            return insets;
        });

        otherUserId = getIntent().getStringExtra("otherUserId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        Log.e("ChatActivity", "otherUserId: " + otherUserId+" currentUserId: "+currentUserId);
        chatId = chatViewModel.getChatId(currentUserId, otherUserId);

        initViews();
        setListeners();

        
        chatViewModel.checkOrCreateChat(chatId, currentUserId, otherUserId);
        chatViewModel.loadMessages(chatId, currentUserId, otherUserId);

        chatViewModel.getMessagesLiveData().observe(this, messages -> {
            messageList.clear();
            messageList.addAll(messages);
            messageAdapter.notifyDataSetChanged();
            rvMessages.scrollToPosition(messageList.size() - 1);
        });
        loadUserInfo();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        edtMessage = findViewById(R.id.etMessage);
        btnSendMessage = findViewById(R.id.btnSend);
        tvName = findViewById(R.id.tvName);
        ivAvatar = findViewById(R.id.ivAvatar);
        
        // Initialize RecyclerView
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        rvMessages.setAdapter(messageAdapter);
        
        // Initialize media preview views
        mediaPreview = findViewById(R.id.mediaPreview);
        ivMediaPreview = findViewById(R.id.ivMediaPreview);
        tvMediaName = findViewById(R.id.tvMediaName);
        tvMediaSize = findViewById(R.id.tvMediaSize);
        btnRemoveMedia = findViewById(R.id.btnRemoveMedia);
        
        // Set up attachment button click listener
        findViewById(R.id.btnAttachment).setOnClickListener(v -> showMediaPicker());
        
        // Set up remove media button click listener
        btnRemoveMedia.setOnClickListener(v -> removeSelectedMedia());
    }

    private void setListeners() {
        btnSendMessage.setOnClickListener(v -> {
            String messageText = edtMessage.getText().toString().trim();
            if (selectedMediaUri != null) {
                // If media is selected, upload it first
                uploadMediaToCloudinary(selectedMediaUri, selectedMediaType, messageText);
            } else if (!messageText.isEmpty()) {
                // If only text is present, send text message
                chatViewModel.sendMessage(chatId, currentUserId, otherUserId, messageText);
                edtMessage.setText("");
            }
        });
    }

    private void loadUserInfo(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(otherUserId).get().addOnSuccessListener(documentSnapshot -> {{
            String name = documentSnapshot.getString("name");
            String profileUrl = documentSnapshot.getString("imageUrl");
            tvName.setText(name);
            Glide.with(this)
                    .load(profileUrl)
                    .placeholder(R.drawable.icon_user)
                    .error(R.drawable.icon_user) // optional fallback in case of failure
                    .centerCrop()
                    .into(ivAvatar);

            Log.e("ChatActivity", "name: " + name+" profileUrl: "+profileUrl);
        }});
    }

    private void showMediaPicker() {
        MediaPickerDialog dialog = new MediaPickerDialog(this, (uri, type) -> {
            selectedMediaUri = uri;
            selectedMediaType = type;
            showMediaPreview();
        });
        dialog.show();
    }

    private void showMediaPreview() {
        if (selectedMediaUri != null) {
            mediaPreview.setVisibility(View.VISIBLE);
            
            // Set media preview image based on type
            switch (selectedMediaType) {
                case "image":
                    ivMediaPreview.setImageURI(selectedMediaUri);
                    break;
                case "video":
                    ivMediaPreview.setImageResource(R.drawable.ic_video);
                    break;
                case "audio":
                    ivMediaPreview.setImageResource(R.drawable.ic_audio);
                    break;
                case "document":
                    ivMediaPreview.setImageResource(R.drawable.ic_document);
                    break;
            }

            // Get file name and size
            String fileName = getFileName(selectedMediaUri);
            String fileSize = getFileSize(selectedMediaUri);
            
            tvMediaName.setText(fileName);
            tvMediaSize.setText(fileSize);
        }
    }

    private void removeSelectedMedia() {
        selectedMediaUri = null;
        selectedMediaType = null;
        mediaPreview.setVisibility(View.GONE);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf(File.separator);
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getFileSize(Uri uri) {
        try {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    long size = cursor.getLong(sizeIndex);
                    return formatFileSize(size);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown size";
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#")
                .format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                selectedMediaUri = uri;
                selectedMediaType = MediaPickerDialog.getMediaType(requestCode);
                showMediaPreview();
            }
        }
    }

    private void uploadMediaToCloudinary(Uri uri, String type, String caption) {
        String requestId = UUID.randomUUID().toString();
        MediaManager.get().upload(uri)
            .option("resource_type", type)
            .callback(new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    // Show loading indicator
                    btnSendMessage.setEnabled(false);
                    btnSendMessage.setText("Uploading...");
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                    // Update progress if needed
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    // Create media object
                    com.gamevision.nextalk.models.Media media = new com.gamevision.nextalk.models.Media();
                    media.setMediaType(type);
                    media.setMediaUrl((String) resultData.get("url"));
                    media.setFileName(getFileName(uri));
                    media.setFileSize(getFileSize(uri));

                    if (type.equals("video")) {
                        String videoUrl = (String) resultData.get("secure_url");
                        String publicId = (String) resultData.get("public_id");

                        // Optional: extract cloud name from your config
                        String cloudName = "du3kpasqd"; // or load from your config
                        float startTime = 2.0f; // timestamp in seconds for the thumbnail

                        String thumbnailUrl = "https://res.cloudinary.com/" + cloudName +
                                "/video/upload/so_" + startTime + "/" + publicId + ".jpg";

                        media.setThumbnailUrl(thumbnailUrl);
                    }


                    // Send media message
                    chatViewModel.sendMediaMessage(chatId, currentUserId, otherUserId, media, caption);
                    
                    // Reset UI
                    edtMessage.setText("");
                    removeSelectedMedia();
                    btnSendMessage.setEnabled(true);
                    btnSendMessage.setText("Send");
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    // Handle error
                    Toast.makeText(ChatActivity.this, "Failed to upload media: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    btnSendMessage.setEnabled(true);
                    btnSendMessage.setText("Send");
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    // Handle reschedule
                    Toast.makeText(ChatActivity.this, "Upload rescheduled: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                }
            })
            .dispatch();
    }
}
