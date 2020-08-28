package com.iew.fun2order.brand.news

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_BRAND_EVENT
import com.iew.fun2order.db.firebase.DETAIL_BRAND_PROFILE
import com.iew.fun2order.db.firebase.PRODUCT
import com.iew.fun2order.ui.shop.ActivityDetailMenu
import com.iew.fun2order.ui.shop.ActivityOfficalMenu
import kotlinx.android.synthetic.main.activity_check_notification.*
import kotlinx.android.synthetic.main.news_fragment.*
import kotlinx.android.synthetic.main.row_brandnotify_item.view.*
import kotlinx.android.synthetic.main.row_selectedproduct.view.*
import kotlinx.android.synthetic.main.row_shop_branditem.view.*


class NewsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.news_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.view?.setBackgroundColor(ActivityOfficalMenu.getBackGroundColor())

        if(ActivityOfficalMenu.getBrandProfile()!= null) {
            eventBannerImageView.visibility = View.VISIBLE
            val bannerURL = ActivityOfficalMenu.getBrandProfile()!!.brandEventBannerURL
            Glide.with(requireContext())
                .load(bannerURL)
                .error(null)
                .into(eventBannerImageView)
        }
        else
        {
            eventBannerImageView.setImageBitmap(null)
            eventBannerImageView.visibility = View.GONE

        }

        val brandName = ActivityOfficalMenu.getBrandName()
        if(brandName != "") {
            val detailBrandEvent = "/DETAIL_BRAND_EVENT/$brandName"
            val database = Firebase.database
            val myRef = database.getReference(detailBrandEvent)
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.reversed().forEach()
                    {
                        var eventNotify =  it.getValue(DETAIL_BRAND_EVENT::class.java)
                        if(eventNotify!= null) {
                            val NotifyView = LayoutInflater.from(context)
                                .inflate(R.layout.row_brandnotify_item, null)
                            NotifyView.textNotifyTitle.text = eventNotify.eventTitle ?: ""
                            NotifyView.textNotifySubTitle.text = eventNotify.eventSubTitle ?: ""
                            NotifyView.textUpdateTime.text = eventNotify.publishDate

                            if(eventNotify.eventImageURL!= null) {
                                Glide.with(requireContext())
                                    .load(eventNotify.eventImageURL)
                                    .error(null)
                                    .into(NotifyView.imageBrandNotify)
                            }

                            NotifyView.setOnClickListener {
                                val bundle = Bundle()
                                bundle.putString("NEWS_TITLE", eventNotify.eventTitle ?: "")
                                bundle.putString("NEWS_IMAGE_URL", eventNotify.eventImageURL ?: "")
                                bundle.putString("NEWS_CONTENT_URL", eventNotify.eventContentURL ?: "")
                                val intent = Intent(context, ActivityNewsDetail::class.java)
                                intent.putExtras(bundle)
                                startActivity(intent)
                            }
                            eventListLinearLayout.addView(NotifyView)
                        }
                    }
                }
            })
        }
    }
}