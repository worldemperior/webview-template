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

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Fix #1: hide status bar for true full-screen splash ───────────────
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor    = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // ── Fix #2: prevent screen from sleeping during splash ────────────────
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val image = ImageView(this).apply {
            setImageResource(R.drawable.splash_image)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        setContentView(image)

        handler.postDelayed({
            // ── Fix #3: check activity is still alive before starting ─────────
            if (!isFinishing && !isDestroyed) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 2000)
    }

    // ── Fix #4: cancel handler if user somehow leaves during splash ───────────
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}