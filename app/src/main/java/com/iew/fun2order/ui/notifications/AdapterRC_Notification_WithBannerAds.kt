// Chris Modify

package com.iew.fun2order.ui.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.iew.fun2order.R
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.listen
import com.iew.fun2order.utility.*
import kotlinx.android.synthetic.main.row_notification.view.*

class AdapterRC_Notification_WithBannerAds(var context: Context, var lstItemsNotify:List<Any>, val IAdapterOnClick: IAdapterOnClick)  : RecyclerView.Adapter<AdapterRC_Notification_WithBannerAds.BaseViewHolder<*>>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when(viewType)
        {
            TYPE_Items -> {
                val view = LayoutInflater.from(context).inflate(R.layout.row_notification,  null)
                return ViewHolder(view).listen()
                { pos, type ->
                    IAdapterOnClick.onClick("Notify", pos,type)
                }
            }

            TYPE_ADS -> {
                val view = LayoutInflater.from(context).inflate(R.layout.row_bannerads,  null)
                val adView = view.findViewById(R.id.adView) as AdView
                val request: AdRequest = AdRequest.Builder().build()
                adView.loadAd(request)
                adView.setAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        view.visibility = View.GONE
                        adView.visibility = View.GONE
                    }
                })
                return ADSViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return lstItemsNotify.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = lstItemsNotify[position]
        when (holder) {
            is AdapterRC_Notification_WithBannerAds.ViewHolder -> holder.bindModel(element as ItemsLV_Notify)
            is AdapterRC_Notification_WithBannerAds.ADSViewHolder -> {}
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (lstItemsNotify[position]) {
            is ItemsLV_Notify -> TYPE_Items
            else  -> TYPE_ADS
        }
    }


    companion object {
        private const val TYPE_Items = 0
        private const val TYPE_ADS = 1
    }


    abstract inner class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindModel(item: T)
    }
    // view
    inner class ViewHolder(itemView: View) : BaseViewHolder<ItemsLV_Notify>(itemView){

        @SuppressLint("SimpleDateFormat")
        override fun bindModel(ItemsLV_Notify: ItemsLV_Notify){

            itemView.notifytitle.text = "來自 ${ItemsLV_Notify.notifyTitle} 的團購訊息"
            itemView.brandName.text = "[ ${ItemsLV_Notify.brandName} ]"

            when(ItemsLV_Notify.msgType)
            {
                NOTIFICATION_TYPE_ACTION_JOIN_ORDER ->
                {
                    itemView.messageTitle.text = "團購邀請"
                    itemView.messageTitle.setTextColor(Color.rgb(0, 122, 255))

                }
                NOTIFICATION_TYPE_MESSAGE_DUETIME->
                {
                    itemView.messageTitle.text = "團購催訂"
                    itemView.messageTitle.setTextColor(Color.rgb(177, 0, 28))
                }

                NOTIFICATION_TYPE_MESSAGE_INFORMATION ->
                {
                    itemView.messageTitle.text = "團購訊息"
                    itemView.messageTitle.setTextColor(Color.rgb(177, 0, 28))
                    itemView.acknowledge.visibility = View.INVISIBLE
                    itemView.joinstatus.visibility = View.INVISIBLE
                }

            }

            if(ItemsLV_Notify.msgType == NOTIFICATION_TYPE_ACTION_JOIN_ORDER || ItemsLV_Notify.msgType == NOTIFICATION_TYPE_MESSAGE_DUETIME) {
                if (ItemsLV_Notify.ackStatus == "" || ItemsLV_Notify.ackStatus == MENU_ORDER_REPLY_STATUS_WAIT) {
                    itemView.acknowledge.text = "尚未回覆"
                    itemView.acknowledge.setTextColor(Color.rgb(0, 122, 255))
                    itemView.joinstatus.text = ""
                } else {
                    itemView.acknowledge.text = "已回覆"
                    when (ItemsLV_Notify.ackStatus) {
                        MENU_ORDER_REPLY_STATUS_ACCEPT -> {
                            itemView.joinstatus.text = "參加"
                            itemView.joinstatus.setTextColor(Color.rgb(0, 122, 255))
                            itemView.acknowledge.setTextColor(Color.rgb(0, 122, 255))
                        }
                        MENU_ORDER_REPLY_STATUS_REJECT -> {
                            itemView.joinstatus.text = "不參加"
                            itemView.joinstatus.setTextColor(Color.rgb(177, 0, 28))
                            itemView.acknowledge.setTextColor(Color.rgb(177, 0, 28))
                        }

                        MENU_ORDER_REPLY_STATUS_EXPIRE -> {
                            itemView.acknowledge.text = "訂單逾期"
                            itemView.joinstatus.text = "沒有參加"
                            itemView.joinstatus.setTextColor(Color.rgb(177, 0, 28))
                            itemView.acknowledge.setTextColor(Color.rgb(177, 0, 28))
                        }
                    }
                }
            }

            itemView.notifydatetime.text = ItemsLV_Notify.notifydatetime
            itemView.notifdesc.setText ( ItemsLV_Notify.desc)
            itemView.notifdesc.setOnClickListener(View.OnClickListener {
                IAdapterOnClick.onClick("Notify", adapterPosition,0)
            })



            if(ItemsLV_Notify.read == "N")
            {
                itemView.notifylayout.setBackgroundResource(R.drawable.shape_rectangle_lightblue)
            }
            else if(ItemsLV_Notify.read == "Y")
            {
                itemView.notifylayout.setBackgroundResource(R.drawable.shape_rectangle_red)
            }
        }


        private fun getImageDrawable(imageName: String): Drawable {
            val id = context.resources.getIdentifier(imageName, "drawable",
                context.packageName)
            return context.resources.getDrawable(id)
        }
    }


    inner class ADSViewHolder(itemView: View) : BaseViewHolder<ItemsLV_Notify>(itemView){
        override fun bindModel(item: ItemsLV_Notify) {

        }
    }
}








