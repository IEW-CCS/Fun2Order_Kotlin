package com.iew.fun2order.brand.story

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.iew.fun2order.R
import com.iew.fun2order.ui.shop.ActivityOfficalMenu
import kotlinx.android.synthetic.main.story_fragment.*


class StoryFragment : Fragment() {



    private lateinit var viewModel: StoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.story_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(ActivityOfficalMenu.getBrandProfile() != null) {
            val brandStoryURL = ActivityOfficalMenu.getBrandProfile()!!.brandStoryURL ?: ""
            if(brandStoryURL != "") {
                val webSettings = webView.settings
                webSettings.javaScriptEnabled = true
                webView.webViewClient = WebViewClient()
                webView.loadUrl(brandStoryURL)
            }
        }
    }
}