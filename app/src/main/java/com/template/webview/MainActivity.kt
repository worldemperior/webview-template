package PACKAGE_PLACEHOLDER

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.core.view.WindowCompat
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

    private val admobAppId =
        "ADMOB_APP_ID_PLACEHOLDER"

    private val bannerAdId =
        "BANNER_AD_ID_PLACEHOLDER"

    private val interstitialAdId =
        "INTERSTITIAL_AD_ID_PLACEHOLDER"

    @SuppressLint("SetJavaScriptEnabled")

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(
            window,
            true
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

        webView.settings.mixedContentMode =
            WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.settings.allowFileAccess =
            true

        webView.settings.allowContentAccess =
            true

        webView.settings.setSupportZoom(
            false
        )

        webView.webViewClient =
            object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    clickCount++

                    if (
                        clickCount >= 4
                    ) {

                        showInterstitial()

                        clickCount = 0
                    }

                    return false
                }
            }

        root.addView(webView)

        if (
            bannerAdId.isNotBlank() &&
            bannerAdId != "BANNER_AD_ID_PLACEHOLDER"
        ) {

            MobileAds.initialize(this)

            val adView =
                AdView(this)

            adView.setAdSize(
                AdSize.BANNER
            )

            adView.adUnitId =
                bannerAdId

            adView.loadAd(
                AdRequest.Builder().build()
            )

            root.addView(adView)
        }

        if (
            interstitialAdId.isNotBlank() &&
            interstitialAdId != "INTERSTITIAL_AD_ID_PLACEHOLDER"
        ) {

            MobileAds.initialize(this)

            loadInterstitial()
        }

        setContentView(root)

        webView.loadUrl(
            "URL_PLACEHOLDER"
        )
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