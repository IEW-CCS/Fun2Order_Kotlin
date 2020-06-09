package com.iew.fun2order.ui.my_setup.rcviewsample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.my_setup.ItemsLV_Favourite
import com.iew.fun2order.ui.my_setup.listen
import kotlinx.android.synthetic.main.row_setup_group.view.*


class TempRecycleViewAnyAdapter(var context: Context, var ItemsLV_Favourite: List<ItemsLV_Favourite>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<TempRecycleViewAnyAdapter.BaseViewHolder<*>>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {

        return when(viewType)
        {
            TYPE_Items -> {
                val view =  LayoutInflater.from(context).inflate(android.R.layout.list_content, parent, false)
                TypeViewHolder(view).listen()
                { pos, type ->

                    IAdapterOnClick.onClick("sender",pos,type)
                }
            }
            TYPE_Header -> {
                val view = LayoutInflater.from(context).inflate(android.R.layout.list_content, parent, false)
                HeaderViewHolder(view).listen()
                { pos, type ->

                    IAdapterOnClick.onClick("sender",pos,type)
                }
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }

    }


    override fun getItemCount(): Int {
        return ItemsLV_Favourite.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {

        val element = ItemsLV_Favourite[position]

        when (holder) {
            is TypeViewHolder -> holder.bindModel(element as ItemsLV_Favourite)
            is HeaderViewHolder -> holder.bindModel(element as ItemsLV_Favourite)

            else -> throw IllegalArgumentException()
        }


        // holderHeader?.bindModel( LVItems_Favourite[position] )
    }

    override fun getItemViewType(position: Int): Int {
        val comparable = ItemsLV_Favourite[position]
        return when (comparable) {
            is ItemsLV_Favourite -> TYPE_Items
            else -> TYPE_Header
            //throw IllegalArgumentException("Invalid type of data " + position)
        }


    }

    companion object {
        private const val TYPE_Items = 0
        private const val TYPE_Header = 1
    }

    inner abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindModel(item: T)
    }

    inner class TypeViewHolder(itemView: View) : BaseViewHolder<ItemsLV_Favourite>(itemView){

        override fun bindModel(ItemsLV_Favourite: ItemsLV_Favourite){

            // set description
            itemView.GroupName.text = ItemsLV_Favourite.Name



        }
    }

    inner class HeaderViewHolder(itemView: View) : BaseViewHolder<ItemsLV_Favourite>(itemView){

        override fun bindModel(ItemsLV_Favourite: ItemsLV_Favourite){
            // set description
            itemView.GroupName.text = ItemsLV_Favourite.Name

        }




    }
}



