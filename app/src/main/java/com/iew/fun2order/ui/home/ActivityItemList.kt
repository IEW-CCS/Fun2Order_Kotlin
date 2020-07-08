package com.iew.fun2order.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.iew.fun2order.R


class ActivityItemList : AppCompatActivity() {

    var listView: ListView? = null
    val array = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.iew.fun2order.R.layout.activity_item_list)
        supportActionBar?.hide()

        val locationItemList: Array<String>? = intent.extras.getStringArray("ItemListData")

        locationItemList?.forEach {
            array.add(it)
        }

        var lstAdapter: ArrayAdapter<String>? = null
        lstAdapter = ArrayAdapter(this, android.R.layout.simple_selectable_list_item, array)
        listView = findViewById<View>(com.iew.fun2order.R.id.listViewItemList) as ListView
        listView!!.adapter = lstAdapter
        listView!!.setOnItemLongClickListener { parent, view, position, id ->

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
                    lstAdapter.remove(str)
                    lstAdapter.notifyDataSetChanged()
                    alertDialog.dismiss()
                }
            true
        }
    }

    override fun onBackPressed() {
        val bundle = Bundle()
        bundle.putString("Result", "OK")
        bundle.putStringArrayList("Location",array)
        val intent = Intent().putExtras(bundle)
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }
}