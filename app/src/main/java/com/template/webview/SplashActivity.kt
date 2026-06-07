package com.template.webview

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat

class SplashActivity : ComponentActivity() {

    // Keep reference so we can cancel it safely in onDestroy
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // True full-screen splash — transparent status and nav bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor     = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Keep screen on during the 2-second delay
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val image = ImageView(this).apply {
            setImageResource(R.drawable.splash_image)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        setContentView(image)

        handler.postDelayed({
            // Only navigate if the activity is still alive
            if (!isFinishing && !isDestroyed) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 2000)
    }

    override fun onDestroy() {
        // Cancel pending callback to prevent leaks if user exits during splash
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}