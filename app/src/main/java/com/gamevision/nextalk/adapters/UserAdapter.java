package com.gamevision.nextalk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gamevision.nextalk.R;
import com.gamevision.nextalk.models.ChatSummaryModel;
import com.gamevision.nextalk.models.UserModel;
import com.gamevision.nextalk.models.UserWithLastMessage;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserWithLastMessage> userList = new ArrayList<>();
    private final Context context;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClicked(UserModel user);
    }

    public UserAdapter(Context context, OnUserClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateData(List<UserWithLastMessage> newData) {
        this.userList = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserWithLastMessage item = userList.get(position);

        UserModel user = item.getUser();
        ChatSummaryModel summary = item.getSummary();

        holder.tvName.setText(user.getName());

        // Load avata
        Glide.with(context)
                .load(user.getProfileUrl())
                .placeholder(R.drawable.icon_user)
                .into(holder.ivAvatar);

        // Set last message
        holder.tvLastMessage.setText(summary != null && summary.getLastMessage() != null ?
                summary.getLastMessage() : "Tap to chat");

        // Set time
        holder.tvTime.setText(summary != null ? formatTime(summary.getTimestamp()) : "");

        // Optional: Show unread count (you can enhance this logic)
        holder.tvUnreadCount.setVisibility(View.GONE); // Initially hidden

        // Online status (you can bind this with Firestore presence logic later)
        holder.onlineIndicator.setVisibility(View.GONE);

        // Click listener
        holder.itemView.setOnClickListener(v -> listener.onUserClicked(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        ShapeableImageView ivAvatar;
        TextView tvName, tvLastMessage, tvTime;
        View onlineIndicator;
        Chip tvUnreadCount;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
            onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        }
    }

    private String formatTime(String timestamp) {
        // Assuming timestamp is ISO8601 string or millis string
        try {
            long timeMillis = Long.parseLong(timestamp);
            return android.text.format.DateFormat.format("hh:mm a", new Date(timeMillis)).toString();
        } catch (Exception e) {
            return "";
        }
    }
}
