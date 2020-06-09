package com.iew.fun2order.order

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.listen
import kotlinx.android.synthetic.main.row_orderproduct.view.*

class AdapterRC_OrderProduct(var context: Context, var lstItemOrderProduct: List<ItemsLV_OrderProduct>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<AdapterRC_OrderProduct.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_orderproduct,  null)
        return ViewHolder(view).listen()
        { pos, type ->
            IAdapterOnClick.onClick("OrderProduct", pos,type)
        }
    }
    override fun getItemCount(): Int {
        return lstItemOrderProduct.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindModel( lstItemOrderProduct[position] )
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindModel(ItemsLV_orderProduct: ItemsLV_OrderProduct){
            itemView.orderproductItem.text = ItemsLV_orderProduct.itemName.toString()
            itemView.orderproductPrice.text = ItemsLV_orderProduct.itemPrice.toString()
        }
    }
}







