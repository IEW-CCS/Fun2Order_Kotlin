package com.iew.fun2order.ui.history

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.listen
import com.iew.fun2order.ui.notifications.AdapterRC_Notification_WithBannerAds
import com.iew.fun2order.ui.notifications.ItemsLV_Ads
import com.iew.fun2order.ui.notifications.ItemsLV_Notify
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_EXPIRE
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_WAIT
import kotlinx.android.synthetic.main.activity_tap_message.*
import kotlinx.android.synthetic.main.row_history_order.view.*
import java.text.SimpleDateFormat
import java.util.*


class RCAdapter_Order_WithBannerAds(var context: Context, var ItemsLV_Order: List<Any>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<RCAdapter_Order_WithBannerAds.BaseViewHolder<*>>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when(viewType)
        {
            RCAdapter_Order_WithBannerAds.TYPE_Items -> {
                val view = LayoutInflater.from(context).inflate(R.layout.row_history_order,  null)
                return ViewHolder(view).listen()
                { pos, type ->
                    IAdapterOnClick.onClick("Order", pos,type)
                }
            }

            RCAdapter_Order_WithBannerAds.TYPE_ADS -> {
                val view = LayoutInflater.from(context).inflate(R.layout.row_bannerads,  null)
                val adView = view.findViewById(R.id.adView) as AdView
                val request: AdRequest = AdRequest.Builder().build()
                adView.loadAd(request)
                adView.adListener = object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        view.visibility = View.GONE
                        adView.visibility = View.GONE
                    }
                }
                return ADSViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return ItemsLV_Order.size
    }


    override fun onBindViewHolder(holder: RCAdapter_Order_WithBannerAds.BaseViewHolder<*>, position: Int) {
        val element = ItemsLV_Order[position]
        when (holder) {
            is RCAdapter_Order_WithBannerAds.ViewHolder -> holder.bindModel(element as ItemsLV_Order)
            is RCAdapter_Order_WithBannerAds.ADSViewHolder -> {}
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (ItemsLV_Order[position]) {
            is ItemsLV_Order -> RCAdapter_Order_WithBannerAds.TYPE_Items
            is ItemsLV_Ads -> RCAdapter_Order_WithBannerAds.TYPE_ADS
            else -> throw IllegalArgumentException()
        }
    }

    companion object {
        private const val TYPE_Items = 0
        private const val TYPE_ADS = 1
    }


    abstract inner class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindModel(item: T)
    }



    inner class ViewHolder(itemView: View) : BaseViewHolder<ItemsLV_Order>(itemView){
        override fun bindModel(order: ItemsLV_Order){
            itemView.notifyTitle.text = order.brandName
            itemView.orderStartTime.text = order.startTime
            itemView.orderDueTime.text = order.dueTime
            itemView.orderNumber.text = order.orderNumber
            itemView.orderJoinCount.text = order.joinCount
            if(order.expired)
            {
                itemView.orderDueTime.setTextColor(Color.RED)
            }
            else
            {
                itemView.orderDueTime.setTextColor(Color.BLACK)
            }
        }
    }



    inner class ADSViewHolder(itemView: View) : BaseViewHolder<ItemsLV_Notify>(itemView){
        override fun bindModel(item: ItemsLV_Notify) {

        }
    }
}








