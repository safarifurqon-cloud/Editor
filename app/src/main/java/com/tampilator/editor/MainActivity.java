package com.tampilator.editor

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        webView = WebView(this)
        setContentView(webView)
        
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        
        // Jembatan native
        webView.addJavascriptInterface(WifiBridge(this), "AndroidBridge")
        
        webView.webViewClient = WebViewClient()
        
        // Load editor.html dari assets
        webView.loadUrl("file:///android_asset/editor.html")
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}

class WifiBridge(private val context: Context) {
    
    @JavascriptInterface
    fun getTvBoxIp(): String {
        try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            
            val dhcp = wifiManager.dhcpInfo ?: return ""
            val gateway = dhcp.gateway
            
            if (gateway == 0) return ""
            
            return String.format(
                "%d.%d.%d.%d",
                gateway and 0xff,
                gateway shr 8 and 0xff,
                gateway shr 16 and 0xff,
                gateway shr 24 and 0xff
            )
        } catch (e: Exception) {
            return ""
        }
    }
}
