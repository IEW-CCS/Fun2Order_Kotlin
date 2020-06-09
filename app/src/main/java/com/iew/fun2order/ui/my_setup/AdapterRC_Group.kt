package com.iew.fun2order.ui.my_setup

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_setup_group.view.*
import com.iew.fun2order.R


class AdapterRC_Group(var context: Context, var lstItemsGroup: List<ItemsLV_Group>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<AdapterRC_Group.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_setup_group,  null)
        return ViewHolder(view).listen()
        { pos, type ->

            IAdapterOnClick.onClick("Group",pos,type)
        }
    }

    override fun getItemCount(): Int {
        return lstItemsGroup.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindModel( lstItemsGroup[position] )
    }

    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindModel(Items: ItemsLV_Group){
            itemView.GroupName.text = Items.Name
            itemView.GroupView.setImageBitmap(Items.Photo)
        }

        private fun getImageDrawable(imageName: String): Drawable {
            val id = context.resources.getIdentifier(imageName, "drawable",
                context.packageName)
            return context.resources.getDrawable(id)
        }

    }

}








