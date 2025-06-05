package com.gamevision.nextalk.chat;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.gamevision.nextalk.R;
import com.gamevision.nextalk.models.Media;
import com.gamevision.nextalk.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType == 0 ? R.layout.item_message_received : R.layout.item_message_sent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        
        if (message.getType() != null && message.getType().equals("media")) {
            // Handle media message
            if (holder.messageText != null) holder.messageText.setVisibility(View.GONE);
            if (holder.mediaContainer != null) holder.mediaContainer.setVisibility(View.VISIBLE);
            
            Media media = message.getMedia();
            if (media != null) {
                // Set media preview based on type
                switch (media.getMediaType()) {
                    case "image":
                        if (holder.ivPlayButton != null) holder.ivPlayButton.setVisibility(View.GONE);
                        if (holder.audioPlayerControls != null) holder.audioPlayerControls.setVisibility(View.GONE);
                        if (holder.btnDownload != null) holder.btnDownload.setVisibility(View.GONE);
                        if (holder.ivMediaPreview != null) {
                            Log.e("MessageAdapter", "Media URL: " + media.getMediaUrl());
                            Glide.with(holder.itemView.getContext())
                                .load(media.getMediaUrl())
                                .into(holder.ivMediaPreview);
                        }
                        break;
                    case "video":
                        if (holder.ivPlayButton != null) holder.ivPlayButton.setVisibility(View.VISIBLE);
                        if (holder.audioPlayerControls != null) holder.audioPlayerControls.setVisibility(View.GONE);
                        if (holder.btnDownload != null) holder.btnDownload.setVisibility(View.VISIBLE);
                        if (holder.ivMediaPreview != null) {
                            Log.e("MessageAdapter", "Thumbnail URL: " + media.getThumbnailUrl());
                            Glide.with(holder.itemView.getContext())
                                .load(media.getThumbnailUrl())
                                .into(holder.ivMediaPreview);
                        }
                        break;
                    case "audio":
                        if (holder.ivPlayButton != null) holder.ivPlayButton.setVisibility(View.GONE);
                        if (holder.audioPlayerControls != null) holder.audioPlayerControls.setVisibility(View.VISIBLE);
                        if (holder.btnDownload != null) holder.btnDownload.setVisibility(View.VISIBLE);
                        if (holder.ivMediaPreview != null) {
                            holder.ivMediaPreview.setImageResource(R.drawable.ic_audio);
                        }
                        break;
                    case "document":
                        if (holder.ivPlayButton != null) holder.ivPlayButton.setVisibility(View.GONE);
                        if (holder.audioPlayerControls != null) holder.audioPlayerControls.setVisibility(View.GONE);
                        if (holder.btnDownload != null) holder.btnDownload.setVisibility(View.VISIBLE);
                        if (holder.ivMediaPreview != null) {
                            holder.ivMediaPreview.setImageResource(R.drawable.ic_document);
                        }
                        break;
                }

                if (holder.tvMediaName != null) holder.tvMediaName.setText(media.getFileName());
                if (holder.tvMediaSize != null) holder.tvMediaSize.setText(media.getFileSize());

                // Set click listeners
                if (holder.mediaContainer != null) {
                    holder.mediaContainer.setOnClickListener(v -> {
                        if (media.getMediaType().equals("image")) {
                            // Open image in full screen
                            Intent intent = new Intent(v.getContext(), ImageViewerActivity.class);
                            intent.putExtra("imageUrl", media.getMediaUrl());
                            v.getContext().startActivity(intent);
                        }
                        else if (media.getMediaType().equals("video")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(media.getMediaUrl()), "video/mp4");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            // Prefer VLC or other media players
                            Intent chooser = Intent.createChooser(intent, "Open with");

                            try {
                                v.getContext().startActivity(chooser);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(v.getContext(), "No suitable video player found", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                }

                if (holder.btnDownload != null) {
                    holder.btnDownload.setOnClickListener(v -> {
                        // Download media file
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(media.getMediaUrl()));
                        v.getContext().startActivity(intent);
                    });
                }

                if (holder.btnPlayPause != null) {
                    holder.btnPlayPause.setOnClickListener(v -> {
                        // Toggle audio playback
                        if (media.getMediaType().equals("audio")) {
                            // Implement audio playback logic
                            Toast.makeText(v.getContext(), "Audio playback not implemented yet", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        } else {
            // Handle text message
            if (holder.messageText != null) {
                holder.messageText.setVisibility(View.VISIBLE);
                holder.messageText.setText(message.getContent());
            }
            if (holder.mediaContainer != null) holder.mediaContainer.setVisibility(View.GONE);
        }

        // Set message alignment based on sender
        if (holder.messageContainer != null) {
            if (message.getSenderId().equals(currentUserId)) {
                holder.messageContainer.setGravity(Gravity.END);
                if (holder.messageText != null) {
                    holder.messageText.setBackgroundResource(R.drawable.bg_message_sent);
                }
            } else {
                holder.messageContainer.setGravity(Gravity.START);
                if (holder.messageText != null) {
                    holder.messageText.setBackgroundResource(R.drawable.bg_message_received);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return message.getSenderId().equals(currentUserId) ? 1 : 0;
    }

    private String getMimeType(String mediaType) {
        switch (mediaType) {
            case "image":
                return "image/*";
            case "video":
                return "video/*";
            case "audio":
                return "audio/*";
            case "document":
                return "application/*";
            default:
                return "*/*";
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        LinearLayout messageContainer;
        View mediaContainer;
        ImageView ivMediaPreview;
        ImageView ivPlayButton;
        View audioPlayerControls;
        ImageButton btnPlayPause;
        TextView tvAudioDuration;
        TextView tvMediaName;
        TextView tvMediaSize;
        ImageButton btnDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            mediaContainer = itemView.findViewById(R.id.mediaContainer);
            ivMediaPreview = itemView.findViewById(R.id.ivMediaPreview);
            ivPlayButton = itemView.findViewById(R.id.ivPlayButton);
            audioPlayerControls = itemView.findViewById(R.id.audioPlayerControls);
            btnPlayPause = itemView.findViewById(R.id.btnPlayPause);
            tvAudioDuration = itemView.findViewById(R.id.tvAudioDuration);
            tvMediaName = itemView.findViewById(R.id.tvMediaName);
            tvMediaSize = itemView.findViewById(R.id.tvMediaSize);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
} 