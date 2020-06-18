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
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.listen
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_EXPIRE
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_WAIT
import kotlinx.android.synthetic.main.activity_tap_message.*
import kotlinx.android.synthetic.main.row_history_order.view.*
import java.text.SimpleDateFormat
import java.util.*


class RCAdapter_Order(var context: Context, var ItemsLV_Order: List<ItemsLV_Order>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<RCAdapter_Order.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_history_order,  null)
        return ViewHolder(view).listen()
        { pos, type ->
            IAdapterOnClick.onClick("Order",pos,type)
        }
    }

    override fun getItemCount(): Int {
        return ItemsLV_Order.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel( ItemsLV_Order[position] )
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindModel(order: ItemsLV_Order){

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
}








