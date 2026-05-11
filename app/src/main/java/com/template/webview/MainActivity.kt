package com.template.webview

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            MaterialTheme {

                Surface(modifier = Modifier.fillMaxSize()) {

                    var loading by remember {
                        mutableStateOf(true)
                    }

                    AndroidView(factory = {

                        webView = WebView(it)

                        webView.settings.javaScriptEnabled = true
                        webView.settings.domStorageEnabled = true
                        webView.settings.allowFileAccess = true

                        webView.webChromeClient = WebChromeClient()

                        webView.webViewClient =
                            object : WebViewClient() {

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {

                                    val url =
                                        request?.url.toString()

                                    if (
                                        url.startsWith("mailto:") ||
                                        url.startsWith("tel:")
                                    ) {

                                        startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(url)
                                            )
                                        )

                                        return true
                                    }

                                    return false
                                }

                                override fun onPageFinished(
                                    view: WebView?,
                                    url: String?
                                ) {
                                    loading = false
                                }
                            }

                        webView.setDownloadListener {
                                url, _, _, _, _ ->

                            val request =
                                DownloadManager.Request(
                                    Uri.parse(url)
                                )

                            request.setNotificationVisibility(
                                DownloadManager.Request
                                    .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                            )

                            val manager =
                                getSystemService(
                                    Context.DOWNLOAD_SERVICE
                                ) as DownloadManager

                            manager.enqueue(request)
                        }

                        webView.loadUrl(
                            "URL_PLACEHOLDER"
                        )

                        webView
                    })

                    if (loading) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {

        if (
            ::webView.isInitialized &&
            webView.canGoBack()
        ) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}