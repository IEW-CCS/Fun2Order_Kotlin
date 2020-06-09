package com.iew.fun2order.ui.home.adapter

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.iew.fun2order.R
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.jude.rollviewpager.adapter.StaticPagerAdapter

class RollPagerViewAdapter (val IAdapterOnClick: IAdapterOnClick): StaticPagerAdapter() {
    private val imgs = intArrayOf(
        R.drawable.banner_1
    )
    var menuImages: MutableList<Bitmap> = mutableListOf()

    override fun getView(container: ViewGroup, position: Int): View {
        val view = ImageView(container.context)
        val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(view.resources, menuImages[position])
        view.setImageDrawable(roundedBitmapDrawable)
        view.scaleType = ImageView.ScaleType.CENTER_INSIDE
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        view.setOnLongClickListener()
        {

            IAdapterOnClick.onClick("RollPagerView",position ,1)

            true
        }
        return view
    }

    override fun getCount(): Int {
        //return imgs.size
        return menuImages.size
    }
}