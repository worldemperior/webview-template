package com.template.webview

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
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
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mainContainer: LinearLayout

    // Keep drawerLayout as field so back handler and lifecycle can access it
    private lateinit var drawerLayout: DrawerLayout

    // Track AdView for proper lifecycle management
    private var adView: AdView? = null

    private var interstitialAd: InterstitialAd? = null
    private var clickCount = 0
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)

    // Guard against stacking interstitial load calls
    private var isInterstitialLoading = false

    // GitHub Build Process Replacements
    private val bannerAdId       = "BANNER_AD_ID_PLACEHOLDER"
    private val interstitialAdId = "INTERSTITIAL_AD_ID_PLACEHOLDER"

    // Dynamic Anti-Rejection Policy Configurations
    private val useDrawer         = false
    private val useRating         = false
    private val useTitleBar       = false
    private val titleText         = "My App"
    private val showSplashEnabled = "SHOW_SPLASH_PLACEHOLDER"

    // Custom Webview Navigation Drawer Keys
    private val dItem0Title = "DRAWER_TITLE_0_CUSTOM"
    private val dItem0Url   = "DRAWER_LINK_0_CUSTOM"
    private val dItem1Title = "DRAWER_TITLE_1_CUSTOM"
    private val dItem1Url   = "DRAWER_LINK_1_CUSTOM"
    private val dItem2Title = "DRAWER_TITLE_2_CUSTOM"
    private val dItem2Url   = "DRAWER_LINK_2_CUSTOM"
    private val dItem3Title = "DRAWER_TITLE_3_CUSTOM"
    private val dItem3Url   = "DRAWER_LINK_3_CUSTOM"

    @SuppressLint("SetJavaScriptEnabled", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contentType = "CONTENT_TYPE_PLACEHOLDER"

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Prevent white flash on launch
        window.decorView.setBackgroundColor(Color.WHITE)

        drawerLayout = DrawerLayout(this)
        drawerLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        mainContainer = LinearLayout(this)
        mainContainer.orientation = LinearLayout.VERTICAL
        // Use DrawerLayout.LayoutParams so the drawer knows this is the main content view
        mainContainer.layoutParams = DrawerLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            mainContainer.setPadding(
                systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom
            )
            insets
        }

        // ── Title bar ─────────────────────────────────────────────────────────
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
                    layoutParams = LinearLayout.LayoutParams(
                        120,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
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

        // ── Loading progress bar ──────────────────────────────────────────────
        val progressBar = ProgressBar(
            this, null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                8
            )
            max = 100
            visibility = android.view.View.GONE
        }
        mainContainer.addView(progressBar)

        // ── WebView ───────────────────────────────────────────────────────────
        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.javaScriptEnabled       = true
            settings.domStorageEnabled        = true
            settings.loadsImagesAutomatically = true
            settings.allowFileAccess          = true
            settings.allowContentAccess       = true
            settings.mixedContentMode         = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            // Enable caching for faster repeat loads
            settings.cacheMode = WebSettings.LOAD_DEFAULT

            // Zoom support — useful for website content
            settings.setSupportZoom(true)
            settings.builtInZoomControls  = true
            settings.displayZoomControls  = false

            // Allow media autoplay — needed for HTML games and videos
            settings.mediaPlaybackRequiresUserGesture = false

            val chromeUserAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
            settings.userAgentString = chromeUserAgent

            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(this, true)

            // WebChromeClient — drives progress bar
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressBar.progress   = newProgress
                    progressBar.visibility = if (newProgress < 100) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val urlStr = request?.url?.toString() ?: ""

                    // Only count genuine user-initiated navigations for interstitial trigger
                    if (request?.hasGesture() == true && request.isRedirect == false) {
                        clickCount++
                        if (clickCount >= 4) {
                            showInterstitial()
                            clickCount = 0
                        }
                    }

                    // Load http/https inside WebView
                    if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                        return false
                    }

                    // Handle mailto:, tel:, intent:// etc via system
                    return try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlStr))
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                        true
                    } catch (e: Exception) {
                        true
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    swipeRefreshLayout.isRefreshing = false
                }

                // Show a friendly error page instead of a blank screen
                @Deprecated("Needed for API < 23 compatibility")
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    @Suppress("DEPRECATION")
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    if (contentType == "WEBSITE") {
                        view?.loadData(
                            """
                            <html>
                            <body style="font-family:sans-serif;text-align:center;padding:40px;margin-top:80px;">
                            <h2>⚠️ No Connection</h2>
                            <p style="color:#666;">Please check your internet connection<br>and pull down to refresh.</p>
                            </body>
                            </html>
                            """.trimIndent(),
                            "text/html",
                            "UTF-8"
                        )
                    }
                }
            }
        }

        swipeRefreshLayout = SwipeRefreshLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setColorSchemeColors(0xFF007AFF.toInt())
            setOnRefreshListener { webView.reload() }
            // Disable pull-to-refresh for local HTML — it has no effect and confuses users
            if (contentType == "HTML_FILE") {
                isEnabled = false
            }
            addView(webView)
        }
        mainContainer.addView(swipeRefreshLayout)

        drawerLayout.addView(mainContainer)

        // ── Navigation drawer ─────────────────────────────────────────────────
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
                if (contentType == "WEBSITE") {
                    loadSecureUrl("URL_BASE_MAIN")
                } else {
                    loadSecureUrl("file:///android_asset/index.html")
                }
                true
            }

            if (dItem0Title.isNotBlank() && !dItem0Title.contains("CUSTOM") && dItem0Url.isNotBlank()) {
                menu.add(dItem0Title).setOnMenuItemClickListener {
                    drawerLayout.closeDrawers()
                    loadSecureUrl(dItem0Url)
                    true
                }
            }
            if (dItem1Title.isNotBlank() && !dItem1Title.contains("CUSTOM") && dItem1Url.isNotBlank()) {
                menu.add(dItem1Title).setOnMenuItemClickListener {
                    drawerLayout.closeDrawers()
                    loadSecureUrl(dItem1Url)
                    true
                }
            }
            if (dItem2Title.isNotBlank() && !dItem2Title.contains("CUSTOM") && dItem2Url.isNotBlank()) {
                menu.add(dItem2Title).setOnMenuItemClickListener {
                    drawerLayout.closeDrawers()
                    loadSecureUrl(dItem2Url)
                    true
                }
            }
            if (dItem3Title.isNotBlank() && !dItem3Title.contains("CUSTOM") && dItem3Url.isNotBlank()) {
                menu.add(dItem3Title).setOnMenuItemClickListener {
                    drawerLayout.closeDrawers()
                    loadSecureUrl(dItem3Url)
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

        val dataPayload = "HTML_CODE_PLACEHOLDER"
        val urlPayload  = "URL_BASE_MAIN"

        // Restore WebView state on recreation (e.g. screen rotation)
        // otherwise load fresh content
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            when (contentType) {
                "WEBSITE"   -> loadSecureUrl(urlPayload)
                "HTML_CODE" -> webView.loadDataWithBaseURL(
                    null, dataPayload, "text/html", "UTF-8", null
                )
                "HTML_FILE" -> loadSecureUrl("file:///android_asset/index.html")
                else        -> loadSecureUrl("file:///android_asset/index.html")
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    drawerLayout.isDrawerOpen(Gravity.START) ->
                        drawerLayout.closeDrawer(Gravity.START)
                    webView.canGoBack() ->
                        webView.goBack()
                    else ->
                        finish()
                }
            }
        })

        ConsentManager.requestConsent(this) {
            val consentInformation = UserMessagingPlatform.getConsentInformation(this)
            if (consentInformation.canRequestAds()) {
                initializeMobileAdsSdk()
            }
        }
    }

    // ── Lifecycle — forward to WebView and AdView ─────────────────────────────
    override fun onResume() {
        super.onResume()
        webView.onResume()
        adView?.resume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
        adView?.pause()
    }

    override fun onDestroy() {
        adView?.destroy()
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }

    // Save WebView navigation history so back/forward survives rotation
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    // ── Ads ───────────────────────────────────────────────────────────────────
    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) return
        MobileAds.initialize(this) {
            runOnUiThread { setupMonetizationFeatures() }
        }
    }

    private fun setupMonetizationFeatures() {
        if (bannerAdId != "BANNER_DISABLED" && !bannerAdId.contains("PLACEHOLDER")) {
            adView = AdView(this).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = bannerAdId
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            mainContainer.addView(adView)
            adView?.loadAd(AdRequest.Builder().build())
        }

        if (interstitialAdId != "INTERSTITIAL_DISABLED" &&
            !interstitialAdId.contains("PLACEHOLDER")) {
            loadInterstitial()
        }
    }

    private fun loadInterstitial() {
        // Guard against stacking multiple simultaneous load requests
        if (isInterstitialLoading || interstitialAd != null) return
        isInterstitialLoading = true
        InterstitialAd.load(
            this,
            interstitialAdId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd        = ad
                    isInterstitialLoading = false
                }
                override fun onAdFailedToLoad(
                    error: com.google.android.gms.ads.LoadAdError
                ) {
                    isInterstitialLoading = false
                }
            }
        )
    }

    private fun showInterstitial() {
        if (interstitialAd != null) {
            interstitialAd?.show(this)
            interstitialAd = null
            loadInterstitial()
        }
    }

    // ── Rating dialog ─────────────────────────────────────────────────────────
    private fun triggerRatingSystem() {
        AlertDialog.Builder(this)
            .setTitle("Support Our Community!")
            .setMessage(
                "If you enjoy using $titleText, please share your thoughts on the Google Play Store."
            )
            .setCancelable(true)
            .setPositiveButton("Rate Us 5 Stars") { dialog, _ ->
                dialog.dismiss()
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$packageName")
                        )
                    )
                } catch (e: Exception) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                        )
                    )
                }
            }
            .setNegativeButton("Maybe Later") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    // ── URL loading ───────────────────────────────────────────────────────────
    private fun loadSecureUrl(rawUrl: String) {
        var cleanUrl = rawUrl.trim()
            .replace("\"", "")
            .replace("'", "")

        if (cleanUrl.isBlank() ||
            cleanUrl.contains("CUSTOM") ||
            cleanUrl.contains("PLACEHOLDER")) {
            return
        }

        if (cleanUrl.contains("http")) {
            cleanUrl = cleanUrl.substring(cleanUrl.indexOf("http"))
        }

        val targetUrl = if (cleanUrl.startsWith("http://") ||
            cleanUrl.startsWith("https://") ||
            cleanUrl.startsWith("file:///")) {
            cleanUrl
        } else {
            "https://$cleanUrl"
        }

        runOnUiThread {
            webView.stopLoading()
            webView.loadUrl(targetUrl)
        }
    }
}