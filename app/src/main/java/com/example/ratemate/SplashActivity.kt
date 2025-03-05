package com.example.ratemate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize the VideoView
        val videoView = findViewById<VideoView>(R.id.videoView)

        // Set the video path
        val videoPath = "android.resource://${packageName}/${R.raw.splash_video}"
        videoView.setVideoURI(Uri.parse(videoPath))

        // Start the video
        videoView.start()

        // Apply zoom to the VideoView
        applyZoom(videoView, 2.5f) // 1.5x zoom (adjust the scale factor as needed)

        // Set a completion listener to navigate to the next activity
        videoView.setOnCompletionListener {
            // Navigate to the MainActivity after the video ends
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Close the SplashActivity
        }

        // Optional: Add a timeout in case the video doesn't play
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 5000) // 5 seconds timeout
    }

    private fun applyZoom(videoView: VideoView, scale: Float) {
        // Apply scaling to the VideoView
        videoView.scaleX = scale
        videoView.scaleY = scale
    }
}