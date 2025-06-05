package com.gamevision.nextalk.chat;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.gamevision.nextalk.R;

public class AudioPlayerActivity extends AppCompatActivity {
    private ExoPlayer player;
    private ImageButton btnPlayPause;
    private SeekBar seekBar;
    private TextView tvDuration;
    private TextView tvCurrentPosition;
    private ProgressBar progressBar;
    private Handler handler;
    private Runnable updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        String audioUrl = getIntent().getStringExtra("audioUrl");
        if (audioUrl == null) {
            finish();
            return;
        }

        initializeViews();
        setupPlayer(audioUrl);
    }

    private void initializeViews() {
        btnPlayPause = findViewById(R.id.btnPlayPause);
        seekBar = findViewById(R.id.seekBar);
        tvDuration = findViewById(R.id.tvDuration);
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition);
        progressBar = findViewById(R.id.progressBar);

        btnPlayPause.setOnClickListener(v -> togglePlayPause());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    long position = (progress * duration) / 100;
                    player.seekTo(position);
                    tvCurrentPosition.setText(formatTime(position));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupPlayer(String audioUrl) {
        player = new ExoPlayer.Builder(this).build();
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(audioUrl));
        player.setMediaItem(mediaItem);
        player.prepare();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                    updatePlayPauseButton();
                    updateDuration();
                    startSeekBarUpdates();
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
        tvDuration.setText(formatTime(duration));
    }

    private void startSeekBarUpdates() {
        handler = new Handler();
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (player != null) {
                    long duration = player.getDuration();
                    long position = player.getCurrentPosition();
                    int progress = (int) ((position * 100) / duration);
                    seekBar.setProgress(progress);
                    tvCurrentPosition.setText(formatTime(position));
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateSeekBar);
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
        if (handler != null) {
            handler.removeCallbacks(updateSeekBar);
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }
} 