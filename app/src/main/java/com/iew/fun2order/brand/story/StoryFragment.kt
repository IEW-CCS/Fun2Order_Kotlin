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

        val abc = ActivityOfficalMenu.getBrandName()
        var abcc = 123



        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
       // setContentView (webView)
        webView.webViewClient = WebViewClient()
        webView.loadUrl("http://www.shangyulin.com.tw/about.php")

    }
}