package com.template.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.google.android.gms.ads.*
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

        MobileAds.initialize(this)

        webView = WebView(this)

        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        loadInterstitial()

        webView.webViewClient =
            object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    clickCount++

                    if (
                        clickCount >= 4 &&
                        interstitialAd != null
                    ) {

                        interstitialAd?.show(this@MainActivity)

                        clickCount = 0
                    }

                    return false
                }
            }

        webView.loadUrl("URL_PLACEHOLDER")
    }

    private fun loadInterstitial() {

        if (interstitialAdId.isBlank()) {
            return
        }

        val adRequest =
            AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            interstitialAdId,
            adRequest,

            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {

                    interstitialAd = ad
                }
            }
        )
    }
}