package com.iew.fun2order.ui.history

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.listen
import kotlinx.android.synthetic.main.row_history_order.view.*


class RCAdapter_Order(var context: Context, var ItemsLV_Order: List<USER_MENU_ORDER>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<RCAdapter_Order.ViewHolder>()
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

        fun bindModel(order: USER_MENU_ORDER){

            // set description
            itemView.notifyTitle.text = order.brandName
            itemView.orderStartTime.text = order.createTime
            itemView.orderNumber.text = order.orderNumber
            itemView.orderJoinCount.text = order.contentItems!!.count().toString()

            //itemView.item_image.setImageDrawable(getImageDrawable(ItemsLV_Favourite.imageName))
        }

        private fun getImageDrawable(imageName: String): Drawable {
            val id = context.resources.getIdentifier(imageName, "drawable",
                     context.packageName)
            return context.resources.getDrawable(id)
        }

    }

}








