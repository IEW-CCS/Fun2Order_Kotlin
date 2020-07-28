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
import kotlinx.android.synthetic.main.row_detail_productitems.view.*


class AdapterRC_Items(var context: Context, var lstProductItems : List<ItemsLV_Products>, private val lstPriceSequenct: List<String>) : RecyclerView.Adapter<AdapterRC_Items.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_detail_productitems,  null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lstProductItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel( lstProductItems[position], lstPriceSequenct)
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindModel(Items: ItemsLV_Products, priceSequence : List<String> ) {
            itemView.itemName.text = Items.Name
            itemView.itemName.textSize = 16F

            if(Items.ItemDesc == "")
            {
                itemView.itemDesc.text = ""
                itemView.itemDesc.visibility = View.GONE
            }
            else
            {
                itemView.itemDesc.text = Items.ItemDesc
                itemView.itemDesc.visibility = View.VISIBLE
            }


            itemView.itemAttribute.removeAllViews()

            val lp2: TableRow.LayoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT
            );
            lp2.setMargins(20, 0, 0, 0);

            val tbrow = TableRow(context)
            priceSequence.forEach { recipeitemName ->

                val priceItem = Items.ItemPrices.firstOrNull { it -> it.recipeItemName == recipeitemName }
                var price = ""
                if(priceItem != null)
                {
                    if(priceItem?.price != 0) {
                        price = priceItem?.price.toString()
                    }
                }

                val t1v = TextView(context)
                t1v.text = price
                t1v.setTextColor(Color.BLACK)
                t1v.textSize = 16F
                t1v.width = 20
                t1v.gravity = Gravity.CENTER
                tbrow.addView(t1v, lp2)
            }
            itemView.itemAttribute.addView(tbrow)
        }
    }
}








