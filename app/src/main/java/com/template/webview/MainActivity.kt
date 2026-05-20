// FILE: app/src/main/java/com/template/webview/MainActivity.kt

package com.template.webview

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
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
    private lateinit var drawerLayout: DrawerLayout
    private var interstitialAd: InterstitialAd? = null
    private var clickCount = 0
    private var adsInitialized = false

    // --- Replaced by Build Pipeline ---
    private val bannerAdId = "BANNER_AD_ID_PLACEHOLDER"
    private val interstitialAdId = "INTERSTITIAL_AD_ID_PLACEHOLDER"
    private val contentType = "CONTENT_TYPE_PLACEHOLDER"
    private val dataPayload = "HTML_CODE_PLACEHOLDER"
    private val urlPayload = "URL_PLACEHOLDER"

    // --- Feature Flags (replaced by pipeline) ---
    private val useDrawer = false
    private val useRating = false
    private val useTitleBar = false
    private val titleText = "My App"

    // --- Drawer Navigation Links (replaced by pipeline) ---
    private val drawerTitles = listOf(DRAWER_TITLES_PLACEHOLDER)
    private val drawerUrls = listOf(DRAWER_URLS_PLACEHOLDER)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Root drawer layout
        drawerLayout = DrawerLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Main vertical container
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = DrawerLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Apply system insets to main container
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            mainContainer.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // --- Optional Title Bar ---
        if (useTitleBar) {
            buildTitleBar(mainContainer)
        }

        // --- WebView Setup ---
        webView = buildWebView()

        // --- SwipeRefreshLayout ---
        swipeRefreshLayout = SwipeRefreshLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            setColorSchemeColors(0xFF007AFF.toInt())
            setOnRefreshListener { webView.reload() }
            addView(webView)
        }
        mainContainer.addView(swipeRefreshLayout)

        // --- AdMob Banner ---
        if (bannerAdId != "BANNER_DISABLED" && bannerAdId != "BANNER_AD_ID_PLACEHOLDER") {
            initializeAds()
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

        // --- AdMob Interstitial ---
        if (interstitialAdId != "INTERSTITIAL_DISABLED" && interstitialAdId != "INTERSTITIAL_AD_ID_PLACEHOLDER") {
            initializeAds()
            loadInterstitial()
        }

        drawerLayout.addView(mainContainer)

        // --- Optional Side Drawer ---
        if (useDrawer) {
            buildNavigationDrawer()
        }

        setContentView(drawerLayout)

        // --- Load Initial Content ---
        loadContent()

        // --- Back Press Handler ---
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    drawerLayout.isDrawerOpen(Gravity.START) -> drawerLayout.closeDrawer(Gravity.START)
                    webView.canGoBack() -> webView.goBack()
                    else -> finish()
                }
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun buildWebView(): WebView {
        return WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
                allowFileAccess = true
                allowContentAccess = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
            }

            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(this@apply, true)
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val urlStr = request?.url?.toString() ?: return false

                    clickCount++
                    if (clickCount >= 4) {
                        showInterstitial()
                        clickCount = 0
                    }

                    return if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                        false // Load in WebView
                    } else {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(urlStr)))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        true
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun buildTitleBar(container: LinearLayout) {
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
        container.addView(titleBar)
    }

    private fun buildNavigationDrawer() {
        val navigationView = NavigationView(this).apply {
            layoutParams = DrawerLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply { gravity = Gravity.START }
        }

        val menu = navigationView.menu

        // Home entry always present
        menu.add("Home").setOnMenuItemClickListener {
            drawerLayout.closeDrawers()
            loadContent()
            true
        }

        // Inject custom drawer links safely
        val hasDynamicLinks = drawerTitles.isNotEmpty() &&
                drawerTitles[0].isNotBlank() &&
                drawerTitles[0] != "DRAWER_TITLES_PLACEHOLDER"

        if (hasDynamicLinks) {
            drawerTitles.forEachIndexed { index, title ->
                if (index < drawerUrls.size && title.isNotBlank()) {
                    menu.add(title).setOnMenuItemClickListener {
                        drawerLayout.closeDrawers()
                        webView.loadUrl(drawerUrls[index])
                        true
                    }
                }
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

    private fun loadContent() {
        when (contentType) {
            "WEBSITE" -> webView.loadUrl(urlPayload)
            "HTML_CODE" -> webView.loadDataWithBaseURL(
                null, dataPayload, "text/html", "UTF-8", null
            )
            "HTML_FILE" -> webView.loadUrl("file:///android_asset/index.html")
            else -> webView.loadUrl("file:///android_asset/index.html")
        }
    }

    private fun initializeAds() {
        if (!adsInitialized) {
            MobileAds.initialize(this)
            adsInitialized = true
        }
    }

    private fun triggerRatingSystem() {
        AlertDialog.Builder(this)
            .setTitle("Enjoying $titleText?")
            .setMessage("Your feedback helps us improve. Would you like to leave us a rating on the Play Store?")
            .setCancelable(true)
            .setPositiveButton("Rate Now") { dialog, _ ->
                dialog.dismiss()
                try {
                    startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
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
            .setNegativeButton("Not Now") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun loadInterstitial() {
        InterstitialAd.load(
            this,
            interstitialAdId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
            }
        )
    }

    private fun showInterstitial() {
        interstitialAd?.show(this)
        interstitialAd = null
        if (interstitialAdId != "INTERSTITIAL_DISABLED") {
            loadInterstitial()
        }
    }
}