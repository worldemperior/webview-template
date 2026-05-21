// FILE: app/src/main/java/com/template/webview/MainActivity.kt

package com.template.webview

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var interstitialAd: InterstitialAd? = null
    private var clickCount = 0

    // GitHub Build Process Replacements
    private val bannerAdId = "BANNER_AD_ID_PLACEHOLDER"
    private val interstitialAdId = "INTERSTITIAL_AD_ID_PLACEHOLDER"

    // Dynamic Anti-Rejection Policy Configurations
    private val useDrawer = false
    private val useRating = false
    private val useTitleBar = false
    private val titleText = "My App"

    // Custom Webview Navigation Drawer Items
    private val dItem0Title = "DRAWER_ITEM_0_TITLE_PLACEHOLDER"
    private val dItem0Url = "DRAWER_ITEM_0_URL_PLACEHOLDER"

    private val dItem1Title = "DRAWER_ITEM_1_TITLE_PLACEHOLDER"
    private val dItem1Url = "DRAWER_ITEM_1_URL_PLACEHOLDER"

    private val dItem2Title = "DRAWER_ITEM_2_TITLE_PLACEHOLDER"
    private val dItem2Url = "DRAWER_ITEM_2_URL_PLACEHOLDER"

    private val dItem3Title = "DRAWER_ITEM_3_TITLE_PLACEHOLDER"
    private val dItem3Url = "DRAWER_ITEM_3_URL_PLACEHOLDER"


    @SuppressLint("SetJavaScriptEnabled", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Lock screen runtime configuration strictly to Vertical Portrait Orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Force edge-to-edge execution so nothing clips behind punch-holes, notches, or system navigators
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Main Base Frame Root (DrawerLayout handles the slide menu if enabled)
        val drawerLayout = DrawerLayout(this)
        drawerLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Inner Vertical Layout Containment Module (Title + SwipeRefresh/WebView + Ad)
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
                setBackgroundColor(0xFFF2F2F7.toInt())
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
                setTypeface(null, android.graphics.Typeface.BOLD)
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
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Core DOM Authentication Optimizations
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            // Bypasses "Error 403: disallowed_useragent" by mimicking full mobile stable Chrome build patterns
            val chromeUserAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
            settings.userAgentString = chromeUserAgent

            // Configure cookie container rules for multi-domain OAuth redirections
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val urlStr = request?.url?.toString() ?: ""

                    // 1. Increment click count ONLY when a link request navigation event is actively processed
                    clickCount++
                    if (clickCount >= 4) {
                        showInterstitial()
                        clickCount = 0
                    }

                    if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                        return false // Let the WebView load the clicked URL link natively
                    }
                    return try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlStr)))
                        true
                    } catch (e: Exception) { true }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // 2. Remove the ad logic tracking completely from here to prevent background reload popups!
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        // --- Pull to Refresh Container Framework Insertion ---
        swipeRefreshLayout = SwipeRefreshLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setColorSchemeColors(0xFF007AFF.toInt()) // Standard native layout accents
            setOnRefreshListener {
                webView.reload()
            }
            // Bind our fully optimized web view directly inside the pull-to-refresh canvas wrapper
            addView(webView)
        }
        mainContainer.addView(swipeRefreshLayout)

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

            // Standard Default Base Route
            menu.add("Home Portal").setOnMenuItemClickListener {
                drawerLayout.closeDrawers()
                // Safely route back to standard homepage context configuration tracking
                val defaultUrl = "URL_PLACEHOLDER"
                val defaultType = "CONTENT_TYPE_PLACEHOLDER"
                if (defaultType == "WEBSITE") {
                    webView.loadUrl(defaultUrl)
                } else {
                    webView.loadUrl("file:///android_asset/index.html")
                }
                true
            }

            // Dynamic Custom Link Injection Engine Mapping
            if (dItem0Title.isNotBlank() && !dItem0Title.contains("PLACEHOLDER") && dItem0Url.isNotBlank()) {
                menu.add(dItem0Title).setOnMenuItemClickListener {
                    drawerLayout.closeDrawers()
                    webView.loadUrl(dItem0Url)
                    true
                }
            }
            if (dItem1Title.isNotBlank() && !dItem1Title.contains("PLACEHOLDER") && dItem1Url.isNotBlank()) {
                menu.add(dItem1Title).setOnMenuItemClickListener {
                    drawerLayout.closeDrawers()
                    webView.loadUrl(dItem1Url)
                    true
                }
            }
            if (dItem2Title.isNotBlank() && !dItem2Title.contains("PLACEHOLDER") && dItem2Url.isNotBlank()) {
                menu.add(dItem2Title).setOnMenuItemClickListener {
                    drawerLayout.closeDrawers()
                    webView.loadUrl(dItem2Url)
                    true
                }
            }
            if (dItem3Title.isNotBlank() && !dItem3Title.contains("PLACEHOLDER") && dItem3Url.isNotBlank()) {
                menu.add(dItem3Title).setOnMenuItemClickListener {
                    drawerLayout.closeDrawers()
                    webView.loadUrl(dItem3Url)
                    true
                }
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
            "HTML_FILE" -> webView.loadUrl(dataPayload)
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

    // Completely reformatted, friendly UX modal dialog sequence mapping
    private fun triggerRatingSystem() {
        AlertDialog.Builder(this)
            .setTitle("Support Our Community!")
            .setMessage("If you enjoy using $titleText, please share your thoughts on the Google Play Store. It helps us keep improving your experience!")
            .setCancelable(true)
            .setPositiveButton("Rate Us 5 Stars") { dialog, _ ->
                dialog.dismiss()
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                }
            }
            .setNegativeButton("Maybe Later") { dialog, _ -> dialog.dismiss() }
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