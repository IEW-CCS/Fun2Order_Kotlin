package com.iew.fun2order.ui.shop

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.RECIPE
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import kotlinx.android.synthetic.main.row_recipe_item_detail.view.*


class AdapterRC_StandRecipe(var context: Context, var lstItemStandRecipe: List<RECIPE>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<AdapterRC_StandRecipe.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_recipe_item_detail,   parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return lstItemStandRecipe.count()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel( lstItemStandRecipe[position] )
    }


    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindModel(Items: RECIPE){

            var comment = ""
            if(Items.allowedMultiFlag!!)
            {
                comment = "(可複選)"
            }
            else
            {
                comment = ""
            }

            itemView.gridLayoutRecipeBtnList.removeAllViews()
            val recipeType : String = Items.recipeCategory + comment

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


            val sortedRecipeItemList  =  Items.recipeItems?.sortedBy { it-> it.sequenceNumber }
            sortedRecipeItemList?.forEachIndexed { index, Recipe ->

                val b1 = Button(itemView.context)
                b1.tag = index
                b1.text = Recipe.recipeName.toString()
                b1.textSize = 16F
                b1.minWidth = width
                b1.setPadding(0,0,0,0)

                if(Recipe.checkedFlag!!){
                    b1.setBackgroundResource(R.drawable.shape_rectangle_select)
                }else{
                    b1.setBackgroundResource(R.drawable.shape_rectangle_unselect)
                }

                b1.setOnClickListener {
                    if(!Items.allowedMultiFlag!!)
                    {
                        sortedRecipeItemList.forEach{
                            it.checkedFlag = false
                        }
                        Recipe.checkedFlag = true
                    }
                    else {
                        Recipe.checkedFlag = Recipe.checkedFlag!!.xor(true)
                    }

                    IAdapterOnClick.onClick("SelectRecipe",0,0)
                    notifyItemChanged(adapterPosition)
                }
                itemView.gridLayoutRecipeBtnList.addView(b1, lp2)
            }
        }
    }
}