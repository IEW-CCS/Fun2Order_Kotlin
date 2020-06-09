package com.iew.fun2order.ui.home.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.ui.home.ActivitySetupOrder
import com.iew.fun2order.ui.home.data.ProductItemListData

class ProductItemAdapter (listdata: MutableList<ProductItemListData>) :
    RecyclerView.Adapter<ProductItemAdapter.ViewHolder>() {
    private val listdata: MutableList<ProductItemListData>
    private val ACTION_CHU_GROUP_REQUEST_CODE = 100
    private val context: Context? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View =
            layoutInflater.inflate(R.layout.row_menu_item, parent, false)

        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val myListData: ProductItemListData = listdata[position]
        holder.txtItemName.setText(listdata[position].getItemProductName())
        holder.txtItemValue.setText(listdata[position].getItemProductPrice())


        holder.linearLayout.setOnClickListener { view ->
            /*
            Toast.makeText(
                view.context,
                "click on item: " + myListData.getItemProductName(),
                Toast.LENGTH_LONG
            ).show()

             */

        }
    }

    override fun getItemCount(): Int {
        return listdata.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //public ImageView imageView;
        //public TextView textView;
        var txtItemName: TextView
        var txtItemValue: TextView
        var imageViewMenu: ImageView

        var btnChuGroup : Button
        var linearLayout: LinearLayout

        init {

            //this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
            //this.textView = (TextView) itemView.findViewById(R.id.textView);
            txtItemName =
                itemView.findViewById<View>(R.id.textViewMenuName) as TextView
            txtItemValue =
                itemView.findViewById<View>(R.id.textViewMenuDescription) as TextView

            imageViewMenu =
                itemView.findViewById<View>(R.id.imageViewMenuItem) as ImageView

            linearLayout =
                itemView.findViewById<View>(R.id.layoutMenuItem) as LinearLayout

            btnChuGroup =
                itemView.findViewById<View>(R.id.btnChuGroup) as Button
            // 點擊項目中的Button時

            // 點擊項目中的Button時
            btnChuGroup.setOnClickListener(View.OnClickListener {
                // 按下Button要做的事
                var context : Context
                context = itemView.getContext();
                val intent = Intent(context, ActivitySetupOrder::class.java)
                context.startActivity(intent)
            })
        }
    }

    // RecyclerView recyclerView;
    init {
        this.listdata = listdata
    }
}