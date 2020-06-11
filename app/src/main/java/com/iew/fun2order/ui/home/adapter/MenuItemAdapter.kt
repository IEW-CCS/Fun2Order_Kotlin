package com.iew.fun2order.ui.home.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.db.firebase.USER_PROFILE
import com.iew.fun2order.ui.home.ActivityAddMenu
import com.iew.fun2order.ui.home.ActivitySetupOrder
import com.iew.fun2order.ui.home.data.MenuItemListData
import java.io.File


class MenuItemAdapter(listdata: MutableList<MenuItemListData>) :
    RecyclerView.Adapter<MenuItemAdapter.ViewHolder>() {
    private val listdata: MutableList<MenuItemListData>
    private val ACTION_CHU_GROUP_REQUEST_CODE = 100
    private val ACTION_EDIT_MENU_REQUEST_CODE = 102
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
        val myListData: MenuItemListData = listdata[position]
        holder.txtItemName.setText(listdata[position].getItemName())
        var sDesc =listdata[position].getItemValue()
        if(listdata[position].getItemValue().toString().length>60){
            sDesc = listdata[position].getItemValue().toString().substring(0,59)+ ".."
        }
        holder.txtItemValue.setText(sDesc)
        holder.imageViewMenu.setImageBitmap(listdata[position].getItemImage())
        holder.imagePath = listdata[position].getItemImagePath()
        holder.mediaStorageDir =listdata[position].getMediaStorageDir()
        holder.mediaStorageReadDir =listdata[position].getMediaStorageReadDir()
        if(holder.imagePath != ""){
            val file = File(holder.mediaStorageReadDir.toString() + "/" + holder.imagePath.toString())
            if (file.exists()) {
                val bm: Bitmap = BitmapFactory.decodeFile(file.absolutePath)
                holder.imageViewMenu.setImageBitmap(bm)
            }else{
                var islandRef = Firebase.storage.reference.child(holder.imagePath!!)
                val ONE_MEGABYTE = 1024 * 1024.toLong()
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytesPrm: ByteArray ->
                    val bmp = BitmapFactory.decodeByteArray(bytesPrm, 0, bytesPrm.size)
                    holder.imageViewMenu.setImageBitmap(bmp)
                }
            }

        }
        holder.user_menu = listdata[position].getUserMenu()
        holder.user_profile = listdata[position].getUserProfile()

        holder.linearLayout.setOnClickListener { view ->
            /*
            Toast.makeText(
                view.context,
                "click on item: " + myListData.getItemName(),
                Toast.LENGTH_LONG
            ).show()
             */
            val bundle = Bundle()
            bundle.putString("EDIT", "Y")
            bundle.putString("MENU_ID", myListData.getItemName())
            bundle.putParcelable("USER_MENU", holder.user_menu)
            bundle.putParcelable("USER_PROFILE", holder.user_profile)
            var I = Intent(view.context, ActivityAddMenu::class.java)
            I.putExtras(bundle)
            //view.context.startActivity(I)
            (view.context as Activity).startActivityForResult(I, ACTION_EDIT_MENU_REQUEST_CODE)
            //startActivityForResult(I, ACTION_EDIT_MENU_REQUEST_CODE)
        }

        holder.linearLayout.setOnLongClickListener() { view ->
            val alert = AlertDialog.Builder(view.context)
            with(alert) {
                setTitle("確認刪除菜單")
                setMessage(myListData.getItemName())
                setPositiveButton("確定") { dialog, _ ->
                    try {
                        var menuPath = "USER_MENU_INFORMATION/${holder.user_profile!!.userID.toString()}/${holder.user_menu!!.menuNumber}"
                        val database = Firebase.database
                        database.getReference(menuPath).removeValue()
                        listdata.removeAt(position)
                        notifyDataSetChanged()
                    }
                    catch (e: Exception)
                    {
                    }
                    dialog.dismiss()
                }
                setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
            }
            val dialog = alert.create()
            dialog.show()

            true
        }

        holder.btnChuGroup.setOnClickListener { view ->

            val bundle = Bundle()
            bundle.putString("MENU_ID", myListData.getItemName())
            bundle.putParcelable("USER_MENU", holder.user_menu)
            bundle.putParcelable("USER_PROFILE", holder.user_profile)
            var I = Intent(view.context, ActivitySetupOrder::class.java)
            I.putExtras(bundle)
            view.context.startActivity(I)
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
        var imagePath: String? = ""
        var user_menu: USER_MENU? = null
        var user_profile: USER_PROFILE? = null
        var btnChuGroup : Button
        var linearLayout: LinearLayout
        var mediaStorageDir: File? = null
        var mediaStorageReadDir: File? = null

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

            user_menu = USER_MENU()
            user_profile = USER_PROFILE()

            mediaStorageDir = null

            mediaStorageReadDir = null
            // 點擊項目中的Button時
            /*
            btnChuGroup.setOnClickListener(View.OnClickListener {
                // 按下Button要做的事
                var context : Context
                context = itemView.getContext();
                val bound = Bundle();
                bound.putString("MENU_ID", txtItemName.getText().toString())
                bound.putParcelable("USER_MENU", user_menu)
                val intent = Intent(context, ActivitySetupOrder::class.java)
                intent.putExtras(bound);
                context.startActivity(intent)
            })

             */
        }
    }

    // RecyclerView recyclerView;
    init {
        this.listdata = listdata
    }
}