package com.iew.fun2order.ui.home.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.ui.home.ActivityItemList
import com.iew.fun2order.ui.home.data.ProductPriceListData
import com.iew.fun2order.ui.my_setup.IAdapterOnClick

class ProductPriceItemAdapter (listdata: MutableList<ProductPriceListData>, val IAdapterOnClick: IAdapterOnClick) : RecyclerView.Adapter<ProductPriceItemAdapter.ViewHolder>(), SwipeAndDragHelper.ActionCompletionContract {
    private val listdata: MutableList<ProductPriceListData>
    private val context: Context? = null
    private var touchHelper: ItemTouchHelper? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(R.layout.row_product_item, parent, false)

        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val myListData: ProductPriceListData = listdata[position]
        holder.txtItemName.setText(listdata[position].getItemName())
        holder.txtItemPrice.setText(listdata[position].getItemValue())
        holder.txtItemLimit.setText(listdata[position].getItemLimit())
        holder.linearLayout.setOnLongClickListener {
            IAdapterOnClick.onClick("productItemPrice", position, 1)
            true
        }


        holder.imageRander.setOnTouchListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                touchHelper!!.startDrag(holder)
            }
            false
        }

    }

    override fun getItemCount(): Int {
        return listdata.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtItemName: TextView
        var txtItemPrice: TextView
        var txtItemLimit: TextView
        var linearLayout: LinearLayout
        var imageRander : ImageView


        init {

            txtItemName = itemView.findViewById<View>(R.id.textViewName) as TextView
            txtItemPrice = itemView.findViewById<View>(R.id.textViewPrice) as TextView
            txtItemLimit = itemView.findViewById<View>(R.id.textViewLimit) as TextView
            linearLayout = itemView.findViewById<View>(R.id.linearLayoutProdPrice) as LinearLayout
            imageRander = itemView.findViewById<View>(R.id.imageview_reorder) as ImageView

        }
    }

    // RecyclerView recyclerView;
    init {
        this.listdata = listdata
    }

    override fun onViewMoved(oldPosition: Int, newPosition: Int) {
        val targetUser = listdata!![oldPosition]
        val user = ProductPriceListData(targetUser.getItemName(),targetUser.getItemValue(),targetUser.getItemLimit())
        listdata!!.removeAt(oldPosition)
        listdata!!.add(newPosition, user)
        notifyItemMoved(oldPosition, newPosition)
    }

    override fun onViewSwiped(position: Int) {
        listdata!!.removeAt(position)
        notifyItemRemoved(position)
    }

    fun setTouchHelper(touchHelper: ItemTouchHelper?) {
        this.touchHelper = touchHelper
    }
}