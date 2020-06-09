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


class TempListViewAdapter(var context: Context, var listListViewItems: List<ItemsLV_MemberProfile>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var viewHolder =
            ViewHolder()
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.row_setup_memberinfobody, null)
            viewHolder.imageViewProfilePic = view!!.findViewById(R.id.UserView)
            viewHolder.textViewName = view.findViewById(R.id.UserName)
            viewHolder.textViewDescription = view.findViewById(R.id.UserDescription)

            view.tag = viewHolder
        }else{
            viewHolder = view.tag as ViewHolder
        }

        val person = listListViewItems[position]

        viewHolder.textViewName.text = person.Name
        viewHolder.textViewDescription.text = person.description
        viewHolder.imageViewProfilePic.setImageDrawable(getImageDrawable(person.imageName))

        return view
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
        return 0
    }

    override fun getCount(): Int {
        return listListViewItems.size
    }

    class ViewHolder{
        lateinit var imageViewProfilePic: ImageView
        lateinit var textViewName: TextView
        lateinit var textViewDescription: TextView
    }
}