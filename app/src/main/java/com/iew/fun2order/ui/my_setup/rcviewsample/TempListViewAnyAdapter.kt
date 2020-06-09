package com.iew.fun2order.ui.my_setup.rcviewsample

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.iew.fun2order.R
import com.iew.fun2order.ui.my_setup.ItemsLV_MemberProfile


class TempListViewAnyAdapter(var context: Context, var listListViewItems: ArrayList<Any>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var viewHolder =
            ViewHolder()
        val type = getItemViewType(position)

        if (view == null){
            when (type) {
                TYPE_Items ->  {  view = LayoutInflater.from(context).inflate(R.layout.row_setup_memberinfobody, null)
                                  viewHolder.imageViewProfilePic = view!!.findViewById(R.id.UserView)
                                  viewHolder.textViewName = view.findViewById(R.id.UserName)
                                  viewHolder.textViewDescription = view.findViewById(R.id.UserDescription)
                                  view.tag = viewHolder
                               }

                TYPE_Header -> {  view = LayoutInflater.from(context).inflate(R.layout.row_setup_memberinfoheader, null)
                                  viewHolder.textViewName = view.findViewById(R.id.UserName)
                                  view.tag = viewHolder
                }
            }

        }else{

            viewHolder = view.tag as ViewHolder

        }

        when (type) {
            TYPE_Items -> {
                val person: ItemsLV_MemberProfile = getItem(position) as ItemsLV_MemberProfile
                viewHolder.textViewName.text = person.Name
                viewHolder.textViewDescription.text = person.description
                viewHolder.imageViewProfilePic.setImageDrawable(getImageDrawable(person.imageName))

            }
            TYPE_Header -> {
                val titleString = getItem(position) as String
                viewHolder.textViewName.text = titleString
            }
        }


        return view!!
    }

    private fun getImageDrawable(imageName: String): Drawable {
        val id = context.resources.getIdentifier(imageName, "drawable",
                context.packageName)
        return context.resources.getDrawable(id)
    }


    override fun getItem(position: Int): Any {
        return listListViewItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return listListViewItems.size
    }

    override fun getViewTypeCount(): Int { // TYPE_PERSON and TYPE_DIVIDER
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is ItemsLV_MemberProfile) {
            TYPE_Items
        } else TYPE_Header
    }

    override fun isEnabled(position: Int): Boolean {
        return getItemViewType(position) == TYPE_Items
    }

    companion object {
        private const val TYPE_Items = 0
        private const val TYPE_Header = 1
    }


    class ViewHolder{
        lateinit var imageViewProfilePic: ImageView
        lateinit var textViewName: TextView
        lateinit var textViewDescription: TextView
    }
}