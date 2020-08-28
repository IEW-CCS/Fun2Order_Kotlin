package com.iew.fun2order.brand.news

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_BRAND_PROFILE
import kotlinx.android.synthetic.main.activity_news_detail.*
import kotlinx.android.synthetic.main.news_fragment.*
import kotlinx.android.synthetic.main.story_fragment.*

class ActivityNewsDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)
        val newsTitle = intent.extras?.getString("NEWS_TITLE")
        val newsImageURL = intent.extras?.getString("NEWS_IMAGE_URL")
        val newsContentURL = intent.extras?.getString("NEWS_CONTENT_URL")
        newsDetailTitle.text = newsTitle ?: ""
        if(newsContentURL != "") {
            val webSettings = newsDetailWebContent.settings
            webSettings.javaScriptEnabled = true
            newsDetailWebContent.webViewClient = WebViewClient()
            newsDetailWebContent.loadUrl(newsContentURL)
        }

        if(newsImageURL != "")
        {
            newsDetailImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(newsImageURL)
                .error(null)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        newsDetailImage.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(newsDetailImage)
        }
        else
        {
            newsDetailImage.visibility = View.GONE
        }
    }
}