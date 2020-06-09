package com.iew.fun2order.ui.history

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.ui.my_setup.ItemsLV_MemberProfile
import kotlinx.android.synthetic.main.row_selectedproduct.view.*


class AdapterLV_SelectedProductInvite(var context: Context,  var lstItemsSelectedMenuProduct: List<MENU_PRODUCT>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var viewHolder =
            ViewHolder()
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.row_selectedproduct,  null)
            viewHolder.selectedproductItem = view!!.findViewById(R.id.selectedproductItem)
            viewHolder.selectedproductNote = view.findViewById(R.id.selectedproductNote)
            viewHolder.selectedproductCount = view.findViewById(R.id.selectedproductCount)
            view.tag = viewHolder

        }else{
            viewHolder = view.tag as ViewHolder
        }

        val itemSelectedMenuProduct = lstItemsSelectedMenuProduct[position]
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

        viewHolder.selectedproductItem.text = itemSelectedMenuProduct.itemName
        viewHolder.selectedproductNote.text = "$recipeItems $comments"
        viewHolder.selectedproductCount.text = itemSelectedMenuProduct.itemQuantity.toString()

        return view
    }

    private fun getImageDrawable(imageName: String): Drawable {
        val id = context.resources.getIdentifier(imageName, "drawable",
                context.packageName)
        return context.resources.getDrawable(id)
    }


    override fun getItem(position: Int): Any {
        return lstItemsSelectedMenuProduct[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return lstItemsSelectedMenuProduct.size
    }

    class ViewHolder{
        lateinit var selectedproductItem : TextView
        lateinit var selectedproductNote : TextView
        lateinit var selectedproductCount: TextView
    }
}