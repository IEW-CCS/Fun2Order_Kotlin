package com.iew.fun2order.ui.my_setup
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import kotlinx.android.synthetic.main.row_setup_favouritefriend.view.UserView
import kotlinx.android.synthetic.main.row_setup_favouritefriend.view.UserName
import kotlinx.android.synthetic.main.row_setup_memberinfobody.view.*


class AdapterRC_UserProfile(var context: Context, var lstItemsUserProfile: List<Any>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<AdapterRC_UserProfile.BaseViewHolder<*>>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*>  {
        return when(viewType)
        {
            TYPE_Items -> {
                val view =  LayoutInflater.from(context).inflate(R.layout.row_setup_memberinfobody,  null)
                ItemsViewHolder(view).listen()
                { pos, type ->
                    IAdapterOnClick.onClick("UserProfile",pos,type)
                }
            }
            TYPE_Header -> {
                val view =  LayoutInflater.from(context).inflate(R.layout.row_setup_memberinfoheader,  null)
                HeaderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun getItemCount(): Int {
        return lstItemsUserProfile.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = lstItemsUserProfile[position]
        when (holder) {
            is ItemsViewHolder -> holder.bindModel(element as ItemsLV_MemberProfile)
            is HeaderViewHolder -> holder.bindModel(element as String)
            else -> throw IllegalArgumentException()
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (lstItemsUserProfile[position]) {
            is ItemsLV_MemberProfile -> TYPE_Items
            else -> TYPE_Header
        }
    }

    companion object {
        private const val TYPE_Items = 0
        private const val TYPE_Header = 1
    }


    inner abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindModel(item: T)
    }

    inner class ItemsViewHolder(itemView: View) : BaseViewHolder<ItemsLV_MemberProfile>(itemView){

        override fun bindModel(ItemsLV_Body: ItemsLV_MemberProfile){
            itemView.UserName.text = ItemsLV_Body.Name
            itemView.UserDescription.text = ItemsLV_Body.description
            itemView.UserView.setImageDrawable(getImageDrawable(ItemsLV_Body.imageName))
            if(!ItemsLV_Body.disclosure_indicator) {
                itemView.DisclosureIndicator.visibility = View.INVISIBLE
            }
            else
            {
                itemView.DisclosureIndicator.visibility = View.VISIBLE
            }
        }

        private fun getImageDrawable(imageName: String): Drawable {
            val id = context.resources.getIdentifier(imageName, "drawable",
                context.packageName)
            return context.resources.getDrawable(id)
        }
    }

    inner class HeaderViewHolder(itemView: View) : BaseViewHolder<String>(itemView){
        override fun bindModel(Header: String){
            itemView.UserName.text = Header;
        }
    }
}



