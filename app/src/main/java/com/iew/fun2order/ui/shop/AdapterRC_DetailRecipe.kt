package com.iew.fun2order.ui.shop

import android.content.ClipData
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_MENU_INFO_RECIPES
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import kotlinx.android.synthetic.main.row_recipe_item_detail.view.*


class AdapterRC_DetailRecipe(var context: Context, var lstItemDetailRecipe: List<ItemsLV_DetailRecipe>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<AdapterRC_DetailRecipe.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.row_recipe_item_detail,   parent, false)
        return ViewHolder(view)


    }


    override fun getItemCount(): Int {
        return lstItemDetailRecipe.count()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel( lstItemDetailRecipe[position] )
    }


    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindModel(Items: ItemsLV_DetailRecipe){

            var comment = ""
            itemView.gridLayoutRecipeBtnList.removeAllViews()

            if(Items.recipetemplate.mandatoryFlag && Items.recipetemplate.allowMultiSelectionFlag)
            {
                comment = "(必選/可複選)"
            }
            else if(Items.recipetemplate.mandatoryFlag)
            {
                comment = "(必選)"
            }
            else if(Items.recipetemplate.allowMultiSelectionFlag)
            {
                comment = "(可複選)"
            }
            else
            {
                comment = ""
            }

            val recipeType : String = Items.recipetemplate.templateName + comment

            //---- 填寫title ----
            itemView.textViewRecipeType.text = recipeType

            //---- 填寫items 計算填寫位置----
            // 將 TextView 加入到 LinearLayout 中

            val columnCount = 3
            val columnDivider = 12
            val displayMetrics = itemView.context!!.resources.displayMetrics
            val pxWidth = displayMetrics.widthPixels
            val width = (pxWidth/columnCount) - (columnDivider * (columnCount+1))

            //---- 設定Btn 的Layout
            itemView.gridLayoutRecipeBtnList.columnCount = columnCount

            val lp2: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp2.setMargins(20, 20, 0, 0);

            val displayRecipeItemList = Items.recipetemplate.recipeList?.filter { it->it.itemDisplayFlag == true }
            val sortedRecipeItemList  = displayRecipeItemList?.sortedBy { it-> it.itemSequence }
            sortedRecipeItemList?.forEachIndexed { index, detailMenuInfoRecipes ->

                detailMenuInfoRecipes.itemName
                val b1 = Button(itemView.context)
                b1.tag = index
                b1.text = detailMenuInfoRecipes.itemName.toString()
                b1.textSize = 16F
                b1.minWidth = width
                b1.setPadding(0,0,0,0)

                if(detailMenuInfoRecipes.itemCheckedFlag){
                    b1.setBackgroundResource(R.drawable.shape_rectangle_select)
                }else{
                    b1.setBackgroundResource(R.drawable.shape_rectangle_unselect)
                }

                b1.setOnClickListener {
                    if(!Items.recipetemplate.allowMultiSelectionFlag)
                    {
                        sortedRecipeItemList.forEach{
                            it.itemCheckedFlag = false
                        }
                        detailMenuInfoRecipes.itemCheckedFlag = true
                    }
                    else {
                        detailMenuInfoRecipes.itemCheckedFlag = detailMenuInfoRecipes.itemCheckedFlag.xor(true)
                    }

                    IAdapterOnClick.onClick("SelectRecipe",0,0)
                    notifyItemChanged(adapterPosition)
                }
                itemView.gridLayoutRecipeBtnList.addView(b1, lp2)
            }
        }
    }
}