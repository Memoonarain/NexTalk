package com.gamevision.nextalk.chat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.gamevision.nextalk.R;
import com.google.android.material.button.MaterialButton;

public class MediaPickerDialog extends Dialog {
    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final int PICK_AUDIO = 3;
    private static final int PICK_DOCUMENT = 4;
    private Context context;
    private final OnMediaSelectedListener listener;

    public interface OnMediaSelectedListener {
        void onMediaSelected(Uri uri, String type);
    }

    public MediaPickerDialog(@NonNull Context context, OnMediaSelectedListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_media_picker);

        MaterialButton btnImage = findViewById(R.id.btnImage);
        MaterialButton btnVideo = findViewById(R.id.btnVideo);
        MaterialButton btnAudio = findViewById(R.id.btnAudio);
        MaterialButton btnDocument = findViewById(R.id.btnDocument);

        btnImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            ((ChatActivity) context).startActivityForResult(intent, PICK_IMAGE);
            dismiss();
        });

        btnVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");
            ((ChatActivity) context).startActivityForResult(intent, PICK_VIDEO);
            dismiss();
        });

        btnAudio.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            intent.setType("audio/*");
            ((ChatActivity) context).startActivityForResult(intent, PICK_AUDIO);
            dismiss();
        });

        btnDocument.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                ((ChatActivity) context).startActivityForResult(
                    Intent.createChooser(intent, "Select Document"),
                    PICK_DOCUMENT
                );
                dismiss();
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getContext(), "Please install a File Manager.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String getMediaType(int requestCode) {
        switch (requestCode) {
            case PICK_IMAGE:
                return "image";
            case PICK_VIDEO:
                return "video";
            case PICK_AUDIO:
                return "audio";
            case PICK_DOCUMENT:
                return "raw";
            default:
                return "unknown";
        }
    }
} 