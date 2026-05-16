package PACKAGE_PLACEHOLDER

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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

    private val bannerAdId =
        "BANNER_AD_ID_PLACEHOLDER"

    private val interstitialAdId =
        "INTERSTITIAL_AD_ID_PLACEHOLDER"

    @SuppressLint("SetJavaScriptEnabled")

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        val root =
            LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

        root.fitsSystemWindows = true

        ViewCompat.setOnApplyWindowInsetsListener(
            root
        ) { view, insets ->

            val systemBars: Insets =

                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(

                systemBars.left,

                systemBars.top,

                systemBars.right,

                systemBars.bottom
            )

            insets
        }

        webView = WebView(this)

        webView.layoutParams =

            LinearLayout.LayoutParams(

                ViewGroup.LayoutParams.MATCH_PARENT,

                0,

                1f
            )

        webView.settings.javaScriptEnabled =
            true

        webView.settings.domStorageEnabled =
            true

        webView.settings.loadsImagesAutomatically =
            true

        webView.settings.allowFileAccess =
            true

        webView.settings.allowContentAccess =
            true

        webView.settings.mixedContentMode =
            WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.webViewClient =

            object : WebViewClient() {

                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {

                    super.onPageFinished(
                        view,
                        url
                    )

                    clickCount++

                    if (
                        clickCount >= 4
                    ) {

                        showInterstitial()

                        clickCount = 0
                    }
                }
            }

        root.addView(webView)

        if (
            bannerAdId != "BANNER_DISABLED"
        ) {

            MobileAds.initialize(this)

            val adView =
                AdView(this)

            adView.setAdSize(
                AdSize.BANNER
            )

            adView.adUnitId =
                bannerAdId

            adView.layoutParams =

                LinearLayout.LayoutParams(

                    ViewGroup.LayoutParams.MATCH_PARENT,

                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

            adView.loadAd(
                AdRequest.Builder().build()
            )

            root.addView(adView)
        }

        if (
            interstitialAdId !=
            "INTERSTITIAL_DISABLED"
        ) {

            MobileAds.initialize(this)

            loadInterstitial()
        }

        setContentView(root)

        val contentType =
            "CONTENT_TYPE_PLACEHOLDER"

        if (
            contentType == "WEBSITE"
        ) {

            val contentType =
                "CONTENT_TYPE_PLACEHOLDER"

            if (
                contentType == "WEBSITE"
            ) {

                webView.loadUrl(
                    "URL_PLACEHOLDER"
                )

            } else if (
                contentType == "HTML_CODE"
            ) {

                webView.loadDataWithBaseURL(

                    null,

                    """HTML_CODE_PLACEHOLDER""",

                    "text/html",

                    "UTF-8",

                    null
                )

            } else {

                webView.loadUrl(
                    "HTML_CODE_PLACEHOLDER"
                )
            }

        } else if (
            contentType == "HTML_CODE"
        ) {

            webView.loadDataWithBaseURL(

                null,

                """HTML_CODE_PLACEHOLDER""",

                "text/html",

                "UTF-8",

                null
            )

        } else {

            webView.loadUrl(
                "file:///android_asset/index.html"
            )
        }
    }

    private fun loadInterstitial() {

        val request =
            AdRequest.Builder().build()

        InterstitialAd.load(

            this,

            interstitialAdId,

            request,

            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(
                    ad: InterstitialAd
                ) {

                    interstitialAd = ad
                }
            }
        )
    }

    private fun showInterstitial() {

        if (
            interstitialAd != null
        ) {

            interstitialAd?.show(this)

            interstitialAd = null

            loadInterstitial()
        }
    }
}