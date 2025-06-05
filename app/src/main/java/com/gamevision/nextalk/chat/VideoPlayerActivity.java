package com.gamevision.nextalk.chat;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.gamevision.nextalk.R;

public class VideoPlayerActivity extends AppCompatActivity {
    private ExoPlayer player;
    private PlayerView playerView;
    private ImageButton btnPlayPause;
    private ProgressBar progressBar;
    private TextView tvDuration;
    private TextView tvCurrentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        String videoUrl = getIntent().getStringExtra("videoUrl");
        if (videoUrl == null) {
            finish();
            return;
        }

        initializeViews();
        setupPlayer(videoUrl);
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        progressBar = findViewById(R.id.progressBar);
        tvDuration = findViewById(R.id.tvDuration);
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition);

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
    }

    private void setupPlayer(String videoUrl) {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                    updatePlayPauseButton();
                    updateDuration();
                } else if (state == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                updatePlayPauseButton();
            }
        });

        // Start playback
        player.play();
    }

    private void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.play();
        }
    }

    private void updatePlayPauseButton() {
        btnPlayPause.setImageResource(
            player.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play
        );
    }

    private void updateDuration() {
        long duration = player.getDuration();
        long currentPosition = player.getCurrentPosition();
        
        tvDuration.setText(formatTime(duration));
        tvCurrentPosition.setText(formatTime(currentPosition));
    }

    private String formatTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
} 