package com.iew.fun2order.order

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.listen
import kotlinx.android.synthetic.main.row_selectedproduct.view.*

class AdapterRC_SelectedProductNoClick(var context: Context, var lstItemsSelectedMenuProduct: List<MENU_PRODUCT>) : RecyclerView.Adapter<AdapterRC_SelectedProductNoClick.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_selectedproduct,  null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lstItemsSelectedMenuProduct.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindModel( lstItemsSelectedMenuProduct[position] )
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        @SuppressLint("SetTextI18n")
        fun bindModel(itemSelectedMenuProduct: MENU_PRODUCT){

            var recipeItems = ""
            itemSelectedMenuProduct.menuRecipes?.forEach {
                it.recipeItems!!.forEach {recipeItem->
                    if (recipeItem.checkedFlag == true) {
                        recipeItems = recipeItems + recipeItem.recipeName + " "
                    }
                }
            }

            var comments = ""
            comments = if(itemSelectedMenuProduct.itemComments != "") {
                "(${itemSelectedMenuProduct.itemComments})"
            } else {
                itemSelectedMenuProduct.itemComments!!
            }

            itemView.selectedproductItem.text = itemSelectedMenuProduct.itemName
            itemView.selectedproductNote.text = "$recipeItems $comments"
            itemView.selectedproductCount.text = itemSelectedMenuProduct.itemQuantity.toString()
        }
    }
}







