package com.iew.fun2order.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.USER_MENU


class ActivityItemList : AppCompatActivity() {

    var listView: ListView? = null
    private var mFirebaseUserMenu: USER_MENU = USER_MENU()
    private var mContext : Context ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.iew.fun2order.R.layout.activity_item_list)
        supportActionBar?.hide()

        mContext = this@ActivityItemList

        val projects: Array<String> = intent.extras.getStringArray("ItemListData")
        mFirebaseUserMenu = intent.extras.get("USER_MENU") as USER_MENU

        val array = arrayListOf<String>()
        mFirebaseUserMenu.locations!!.forEach {
            array.add(it)
        }



        var arr_aAdapter: ArrayAdapter<String>? = null

        arr_aAdapter = ArrayAdapter(this, android.R.layout.simple_selectable_list_item, array)

        listView = findViewById<View>(com.iew.fun2order.R.id.listViewItemList) as ListView

        listView!!.setAdapter(arr_aAdapter)

        listView!!.setOnItemClickListener { parent, view, position, id ->
            //Toast.makeText(this, "Position Clicked:"+" "+position,Toast.LENGTH_SHORT).show()
            val str = listView!!.getItemAtPosition(position).toString()
            val item = LayoutInflater.from(this).inflate(R.layout.activity_item_list, null)

            var alertDialog = AlertDialog.Builder(this)
                .setTitle("確認刪除地點")
                .setMessage(str)
                .setView(item)

                .setPositiveButton("確定", null)
                .setNegativeButton("取消", null)
                .show()
            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    mFirebaseUserMenu.locations!!.remove(str)
                    arr_aAdapter.remove(str)
                    arr_aAdapter.notifyDataSetChanged()

                    //Toast.makeText(this, "Remove:"+" "+position,Toast.LENGTH_SHORT).show()

                    alertDialog.dismiss()
                }
        }

    }

    override fun onBackPressed() {


        val bundle = Bundle()
        bundle.putString("Result", "OK")
        bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
        val intent = Intent().putExtras(bundle)
        setResult(Activity.RESULT_OK, intent)
        //finish()

        super.onBackPressed()
    }
}