package com.gamevision.nextalk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.gamevision.nextalk.models.Media;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MediaHandler {
    private static final String TAG = "MediaHandler";
    private static final int MAX_IMAGE_SIZE = 1024; // Max dimension for images
    private static final int MAX_VIDEO_BITRATE = 2000000; // 2Mbps
    private static final int MAX_AUDIO_BITRATE = 128000; // 128kbps

    public interface MediaCallback {
        void onSuccess(Media media);
        void onProgress(int progress);
        void onError(String error);
    }

    public static void handleImage(Context context, Uri imageUri, MediaCallback callback) {
        try {
            // Create compressed image file
            File compressedFile = createImageFile(context);
            Bitmap bitmap = ImageUtils.getBitmapFromUri(context, imageUri);
            Bitmap compressedBitmap = ImageUtils.compressBitmap(bitmap, MAX_IMAGE_SIZE);
            
            FileOutputStream out = new FileOutputStream(compressedFile);
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.close();

            // Upload to Cloudinary
            uploadToCloudinary(context, compressedFile, "image", callback);

        } catch (IOException e) {
            Log.e(TAG, "Error handling image: " + e.getMessage());
            callback.onError("Failed to process image");
        }
    }

    public static void handleVideo(Context context, Uri videoUri, MediaCallback callback) {
        try {
            // Create compressed video file
            File compressedFile = createVideoFile(context);
            
            // Generate thumbnail
            Bitmap thumbnail = generateVideoThumbnail(context, videoUri);
            File thumbnailFile = createImageFile(context);
            FileOutputStream out = new FileOutputStream(thumbnailFile);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.close();

            // Upload video and thumbnail to Cloudinary
            uploadToCloudinary(context, compressedFile, "video", new MediaCallback() {
                @Override
                public void onSuccess(Media media) {
                    // Upload thumbnail
                    uploadToCloudinary(context, thumbnailFile, "image", new MediaCallback() {
                        @Override
                        public void onSuccess(Media thumbnailMedia) {
                            media.setThumbnailUrl(thumbnailMedia.getMediaUrl());
                            callback.onSuccess(media);
                        }

                        @Override
                        public void onProgress(int progress) {}

                        @Override
                        public void onError(String error) {
                            callback.onError("Failed to upload thumbnail");
                        }
                    });
                }

                @Override
                public void onProgress(int progress) {
                    callback.onProgress(progress);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Error handling video: " + e.getMessage());
            callback.onError("Failed to process video");
        }
    }

    public static void handleAudio(Context context, Uri audioUri, MediaCallback callback) {
        try {
            // Create compressed audio file
            File compressedFile = createAudioFile(context);
            
            // Upload to Cloudinary
            uploadToCloudinary(context, compressedFile, "audio", callback);

        } catch (IOException e) {
            Log.e(TAG, "Error handling audio: " + e.getMessage());
            callback.onError("Failed to process audio");
        }
    }

    public static void handleDocument(Context context, Uri documentUri, MediaCallback callback) {
        try {
            // Create document file
            File documentFile = createDocumentFile(context, documentUri);
            
            // Upload to Cloudinary
            uploadToCloudinary(context, documentFile, "document", callback);

        } catch (IOException e) {
            Log.e(TAG, "Error handling document: " + e.getMessage());
            callback.onError("Failed to process document");
        }
    }

    private static void uploadToCloudinary(Context context, File file, String type, MediaCallback callback) {
        MediaManager.get()
            .upload(file.getAbsolutePath())
            .option("resource_type", type)
            .callback(new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    callback.onProgress(0);
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {
                    int progress = (int) ((bytes * 100) / totalBytes);
                    callback.onProgress(progress);
                }

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    Media media = new Media();
                    media.setMediaType(type);
                    media.setMediaUrl((String) resultData.get("url"));
                    media.setFileName(file.getName());
                    media.setFileSize(String.valueOf(file.length()));
                    callback.onSuccess(media);
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    callback.onError(error.getDescription());
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    callback.onError("Upload rescheduled: " + error.getDescription());
                }
            })
            .dispatch();
    }

    private static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private static File createVideoFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String videoFileName = "VID_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        return File.createTempFile(videoFileName, ".mp4", storageDir);
    }

    private static File createAudioFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String audioFileName = "AUD_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        return File.createTempFile(audioFileName, ".mp3", storageDir);
    }

    private static File createDocumentFile(Context context, Uri documentUri) throws IOException {
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(
            context.getContentResolver().getType(documentUri));
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String documentFileName = "DOC_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        return File.createTempFile(documentFileName, "." + extension, storageDir);
    }

    private static Bitmap generateVideoThumbnail(Context context, Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, videoUri);
        return retriever.getFrameAtTime();
    }
} 