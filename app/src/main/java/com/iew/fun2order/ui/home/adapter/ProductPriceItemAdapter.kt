package com.iew.fun2order.ui.home.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.ui.home.ActivityItemList
import com.iew.fun2order.ui.home.data.ProductPriceListData

class ProductPriceItemAdapter (listdata: MutableList<ProductPriceListData>) :
    RecyclerView.Adapter<ProductPriceItemAdapter.ViewHolder>() {
    private val listdata: MutableList<ProductPriceListData>
    private val context: Context? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View =
            layoutInflater.inflate(R.layout.row_product_item, parent, false)

        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val myListData: ProductPriceListData = listdata[position]
        holder.txtItemName.setText(listdata[position].getItemName())
        holder.txtItemValue.setText(listdata[position].getItemValue())
/*
        holder.itemView.setOnClickListener { view ->
            Toast.makeText(
                view.context,
                "click on item: " + myListData.getItemName(),
                Toast.LENGTH_LONG
            ).show()

        }

 */
        //holder.linearLayout
    }

    override fun getItemCount(): Int {
        return listdata.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var txtItemValue: TextView
        var linearLayout: LinearLayout


        init {

            txtItemName =
                itemView.findViewById<View>(R.id.textViewNameValue) as TextView
            txtItemValue =
                itemView.findViewById<View>(R.id.textViewPriceValue) as TextView
            linearLayout =
                itemView.findViewById<View>(R.id.linearLayoutProdPrice) as LinearLayout

        }
    }

    // RecyclerView recyclerView;
    init {
        this.listdata = listdata
    }
}