package com.iew.fun2order.order

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.listen
import io.opencensus.resource.Resource
import kotlinx.android.synthetic.main.row_orderreference_item.view.*


class AdapterRC_ReferenceOrder(var context: Context, var lstItemsReferenceOrder: List<ItemsLV_ReferenceOrder>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<AdapterRC_ReferenceOrder.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_orderreference_item, null)
        return ViewHolder(view).listen()
        { pos, type ->
            IAdapterOnClick.onClick("SelectProduct", pos, type)
        }
    }

    override fun getItemCount(): Int {
        return lstItemsReferenceOrder.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindModel(lstItemsReferenceOrder[position], position)
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindModel(itemsLV_ReferenceOrder: ItemsLV_ReferenceOrder, Position: Int) {
            itemView.referenceOwner.text = itemsLV_ReferenceOrder.referenceOwner
            if (itemsLV_ReferenceOrder.followone) {
                itemView.followOne.setTextColor(context.resources.getColor(R.color.white))
                itemView.followOne.setBackgroundResource(R.drawable.shape_rectangle_followone)
                itemView.followOne.setPadding(0, 0, 0, 0)
            } else {
                itemView.followOne.setTextColor(context.resources.getColor(R.color.Orange))
                itemView.followOne.setBackgroundResource(0)
                itemView.followOne.setPadding(0, 0, 0, 0)
            }
            itemView.followOne!!.tag = Position
            itemView.referenceItem!!.tag = Position

            var referenceItemData: String = "請點擊查看訂單詳細內容"
            if (itemsLV_ReferenceOrder.referenceProduct.count() < 3) {
                referenceItemData = ""
                var recipeItems = ""
                itemsLV_ReferenceOrder.referenceProduct.forEach { it ->
                    recipeItems = ""
                    it.menuRecipes!!.forEach { recipe ->
                        recipe.recipeItems!!.forEach { recipeItem ->
                            if (recipeItem.checkedFlag == true) {
                                recipeItems = recipeItems + recipeItem.recipeName + " "
                            }
                        }
                    }
                    val referenceItems = "${it.itemName}: ${recipeItems} * ${it.itemQuantity}"
                    referenceItemData += "${referenceItems}\n"
                }
            }

            itemView.referenceItem.text = referenceItemData
            itemView.followOne.setOnClickListener(View.OnClickListener {
                IAdapterOnClick.onClick("followOne", it.tag as Int, 0)
            })
        }
    }
}







