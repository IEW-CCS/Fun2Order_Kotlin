package com.iew.fun2order.ui.shop

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.order.ItemsLV_OrderProduct
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import kotlinx.android.synthetic.main.row_detail_productitems_with_carts.view.*


class AdapterRC_StandItems_with_carts(var context: Context, var lstProductItems: List<ItemsLV_OrderProduct>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<AdapterRC_StandItems_with_carts.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_detail_productitems_with_carts,  null)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return lstProductItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel( lstProductItems[position])
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindModel(Items: ItemsLV_OrderProduct ) {
            itemView.itemName.text = Items.itemName
            itemView.itemName.textSize = 16F
            itemView.itemAttribute.removeAllViews()

            val lp2: TableRow.LayoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT
            );
            lp2.setMargins(20, 0, 0, 0);

            val tbrow = TableRow(context)

            val t1v = TextView(context)
            t1v.text = Items.itemPrice
            t1v.setTextColor(Color.BLUE)
            t1v.textSize = 16F
            t1v.width = 20
            t1v.gravity = Gravity.CENTER
            tbrow.addView(t1v, lp2)

            val t2v = TextView(context)
            t2v.text = Items.itemLimit
            t2v.setTextColor(Color.RED)
            t2v.textSize = 16F
            t2v.width = 20
            t2v.gravity = Gravity.CENTER
            tbrow.addView(t2v, lp2)
            itemView.itemAttribute.addView(tbrow)

            if(Items.itemLimit != null) {
                val intItemLimit = Items.itemLimit?.toInt() ?: 0
                if (intItemLimit > 0) {
                    itemView.itemShoppingCarts.isEnabled = true
                    itemView.itemShoppingCarts.visibility = View.VISIBLE
                } else {

                    itemView.itemShoppingCarts.isEnabled = false
                    itemView.itemShoppingCarts.visibility = View.INVISIBLE
                }
            }
            else
            {
                itemView.itemShoppingCarts.isEnabled = true
                itemView.itemShoppingCarts.visibility = View.VISIBLE
            }

            itemView.itemShoppingCarts.setOnClickListener {
                IAdapterOnClick.onClick("ShoppingCarts", adapterPosition, 0)
            }
        }
    }
}








