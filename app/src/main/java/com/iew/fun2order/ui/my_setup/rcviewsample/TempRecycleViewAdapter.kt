package com.iew.fun2order.ui.my_setup.rcviewsample

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.ItemsLV_Favourite
import com.iew.fun2order.ui.my_setup.listen
import kotlinx.android.synthetic.main.row_setup_group.view.*


class TempRecycleViewAdapter(var context: Context, var ItemsLV_Favourite: List<ItemsLV_Favourite>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<TempRecycleViewAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 指定了 layout
        val view = LayoutInflater.from(context).inflate(R.layout.row_setup_group,  null)
        return ViewHolder(view).listen()
        { pos, type ->

            IAdapterOnClick.onClick("sender",pos,type)

        }
    }

    override fun getItemCount(): Int {
        return ItemsLV_Favourite.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindModel( ItemsLV_Favourite[position] )
    }



    // view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        fun bindModel(ItemsLV_Favourite: ItemsLV_Favourite){

            // set description
            itemView.GroupName.text = ItemsLV_Favourite.Name
            itemView.GroupView.setImageDrawable(getImageDrawable(ItemsLV_Favourite.imageName))


        }


        private fun getImageDrawable(imageName: String): Drawable {
            val id = context.resources.getIdentifier(imageName, "drawable",
                     context.packageName)
            return context.resources.getDrawable(id)
        }

    }

}








