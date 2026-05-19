package com.template.webview

import android.R.attr.fontWeight
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private var interstitialAd: InterstitialAd? = null
    private var clickCount = 0

    // GitHub Build Process Replacements
    private val bannerAdId = "BANNER_AD_ID_PLACEHOLDER"
    private val interstitialAdId = "INTERSTITIAL_AD_ID_PLACEHOLDER"

    // Dynamic Anti-Rejection Policy Configurations
    private val useDrawer = false     // Changed from CONFIG_USE_DRAWER
    private val useRating = false     // Changed from CONFIG_USE_RATING
    private val useTitleBar = false   // Changed from CONFIG_USE_TITLEBAR
    private val titleText = "My App"  // Changed from "CONFIG_TITLE_TEXT"


    @SuppressLint("SetJavaScriptEnabled", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force edge-to-edge execution so nothing clips behind punch-holes, notches, or system navigators
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Main Base Frame Root (DrawerLayout handles the slide menu if enabled)
        val drawerLayout = DrawerLayout(this)
        drawerLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Inner Vertical Layout Containment Module (Title + WebView + Ad)
        val mainContainer = LinearLayout(this)
        mainContainer.orientation = LinearLayout.VERTICAL
        mainContainer.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Handle System Insets padding to make sure app content shifts dynamically away from system bars
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            mainContainer.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- OPTION: Anti-Rejection Top Header Bar ---
        if (useTitleBar) {
            val titleBar = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(0xFFF2F2F7.toInt()) // Fixed background invocation
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(32, 24, 32, 24)
                gravity = Gravity.CENTER_VERTICAL
            }

            if (useDrawer) {
                val menuButton = Button(this).apply {
                    text = "☰"
                    textSize = 18f
                    layoutParams = LinearLayout.LayoutParams(120, ViewGroup.LayoutParams.WRAP_CONTENT)
                    setOnClickListener { drawerLayout.openDrawer(Gravity.START) }
                }
                titleBar.addView(menuButton)
            }

            val titleView = TextView(this).apply {
                text = titleText
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD) // Fixed bold typeface invocation
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { leftMargin = 24 }
            }
            titleBar.addView(titleView)
            mainContainer.addView(titleBar)
        }

        // --- Core Engine Setup: WebView Instance ---
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val urlStr = request?.url?.toString() ?: ""
                    if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                        return false
                    }
                    return try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlStr)))
                        true
                    } catch (e: Exception) { true }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    clickCount++
                    if (clickCount >= 4) {
                        showInterstitial()
                        clickCount = 0
                    }
                }
            }
        }
        mainContainer.addView(webView)

        // --- AdMob Integration: Banner Framework ---
        if (bannerAdId != "BANNER_DISABLED") {
            MobileAds.initialize(this)
            val adView = AdView(this).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = bannerAdId
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                loadAd(AdRequest.Builder().build())
            }
            mainContainer.addView(adView)
        }

        if (interstitialAdId != "INTERSTITIAL_DISABLED") {
            MobileAds.initialize(this)
            loadInterstitial()
        }

        // Add core UI view container to the drawer layer
        drawerLayout.addView(mainContainer)

        // --- OPTION: Anti-Rejection Navigation Side Menu ---
        if (useDrawer) {
            val navigationView = NavigationView(this)
            val params = DrawerLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply { gravity = Gravity.START }
            navigationView.layoutParams = params

            val menu = navigationView.menu
            menu.add("Home Portal").setOnMenuItemClickListener {
                drawerLayout.closeDrawers()
                true
            }

            if (useRating) {
                menu.add("Rate Our App").setOnMenuItemClickListener {
                    drawerLayout.closeDrawers()
                    triggerRatingSystem()
                    true
                }
            }
            drawerLayout.addView(navigationView)
        }

        setContentView(drawerLayout)

        // --- Target Compilation Deployment Router ---
        val contentType = "CONTENT_TYPE_PLACEHOLDER"
        val dataPayload = "HTML_CODE_PLACEHOLDER"
        val urlPayload = "URL_PLACEHOLDER"

        when (contentType) {
            "WEBSITE" -> webView.loadUrl(urlPayload)
            "HTML_CODE" -> webView.loadDataWithBaseURL(null, dataPayload, "text/html", "UTF-8", null)
            "HTML_FILE" -> webView.loadUrl(dataPayload) // Dynamic file URL string from builder destination
            else -> webView.loadUrl("file:///android_asset/index.html")
        }

        // Clean System Back-Press Lifecycle Handler
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(Gravity.START)) {
                    drawerLayout.closeDrawer(Gravity.START)
                } else if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }

    private fun triggerRatingSystem() {
        AlertDialog.Builder(this)
            .setTitle("Enjoying our platform?")
            .setMessage("If you love our experience, please take a moment to drop a rating on the Google Play Store!")
            .setPositiveButton("Rate Portfolio Now") { dialog, _ ->
                dialog.dismiss()
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                }
            }
            .setNegativeButton("Remind Me Later") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun loadInterstitial() {
        val request = AdRequest.Builder().build()
        InterstitialAd.load(this, interstitialAdId, request, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
        })
    }

    private fun showInterstitial() {
        if (interstitialAd != null) {
            interstitialAd?.show(this)
            interstitialAd = null
            loadInterstitial()
        }
    }
}