package com.example.nextalk

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.nextalk.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        // Load animations
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)

        // Apply animations
        binding.cardLogo.startAnimation(logoAnimation)
        binding.tvAppName.startAnimation(slideUp)
        binding.tvTagline.startAnimation(slideUp)

        // Show progress indicator after logo animation
        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressIndicator.alpha = 0f
            binding.progressIndicator.animate()
                .alpha(1f)
                .setDuration(500)
                .start()
        }, 1000)

        // Navigate to next screen after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2500) // 2.5 seconds delay
    }
} 