package com.iew.fun2order.brand.product

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import com.iew.fun2order.R
import kotlinx.android.synthetic.main.activity_product_detail_info.*


class ActivityProductDetailInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail_info)
        val productItemDetailURL  = intent.extras?.getString("PRODUCT_ITEM_URL") ?: ""
        if(productItemDetailURL != "")
        {
            val webSettings = productItemDetailWeb.settings
            webSettings.javaScriptEnabled = true
            productItemDetailWeb.webViewClient = WebViewClient()
            productItemDetailWeb.loadUrl(productItemDetailURL)
        }
    }
}