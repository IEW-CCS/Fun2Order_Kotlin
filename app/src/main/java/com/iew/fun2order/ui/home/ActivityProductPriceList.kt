package com.iew.fun2order.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.ProductDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.entity.Product
import com.iew.fun2order.db.firebase.PRODUCT
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.ui.home.adapter.ProductPriceItemAdapter
import com.iew.fun2order.ui.home.data.ProductPriceListData
import com.iew.fun2order.utility.RecyclerItemClickListenr
import com.iew.fun2order.utility.SpacesItemDecoration
import kotlinx.android.synthetic.main.row_product_item.view.*


class ActivityProductPriceList: AppCompatActivity() {

    var mRecyclerViewProductList: RecyclerView? = null

    lateinit var titleroductList: LinearLayout
    var mItemList: MutableList<ProductPriceListData> = mutableListOf()
    private var mFirebaseUserMenu: USER_MENU = USER_MENU()
    //private lateinit var mProductDB: ProductDAO
    //private lateinit var mDBContext: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.iew.fun2order.R.layout.activity_product_item_list)
        supportActionBar?.hide()

        val context: Context = this@ActivityProductPriceList


        val itemView = LayoutInflater.from(this).inflate(R.layout.row_product_item, null)
        itemView.textViewName.text = "產品名稱"
        itemView.textViewName.setTextColor(Color.rgb(79,195,247))

        itemView.textViewPrice.text = "價格"
        itemView.textViewPrice.setTextColor(Color.rgb(79,195,247))

        itemView.textViewLimit.text = "限量"
        itemView.textViewLimit.setTextColor(Color.rgb(255,0,0))

        titleroductList = findViewById<LinearLayout>(R.id.titleProductList)
        titleroductList.addView(itemView)

        //mDBContext = AppDatabase(context!!)
        //mProductDB = mDBContext.productdao()

        val sMenuID = intent.extras.getString("MENU_ID")
        mFirebaseUserMenu = intent.extras.get("USER_MENU") as USER_MENU

        mRecyclerViewProductList = findViewById(com.iew.fun2order.R.id.recyclerViewProductList) as RecyclerView
        //activity!!.findViewById<View>(R.id.recyclerViewMenuItems) as RecyclerView
        val adapter = ProductPriceItemAdapter(mItemList)

        mRecyclerViewProductList!!.setHasFixedSize(true)
        mRecyclerViewProductList!!.layoutManager = LinearLayoutManager(context)
        mRecyclerViewProductList!!.adapter = adapter
        mRecyclerViewProductList!!.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val space = 30
        mRecyclerViewProductList!!.addItemDecoration(SpacesItemDecoration(space))
        //var list : List<Product> = mProductDB.getProductByMenuID(sMenuID)


        mItemList.clear()
        mFirebaseUserMenu!!.menuItems!!.forEach() {

            mItemList.add(ProductPriceListData(it.itemName, it.itemPrice.toString()))
        }
            //---------------------------------------

            mRecyclerViewProductList!!.adapter!!.notifyDataSetChanged()

        mRecyclerViewProductList!!.addOnItemTouchListener(RecyclerItemClickListenr(this,
            mRecyclerViewProductList!!, object : RecyclerItemClickListenr.OnItemClickListener {

            override fun onItemClick(view: View, position: Int) {
                //do your work here..
            }
            override fun onItemLongClick(view: View?, position: Int) {
                Toast.makeText(
                    view!!.context,
                    "click on item: " + position.toString(),
                    Toast.LENGTH_LONG
                ).show()

                val str = mItemList.get(position).getItemName()
                val item = LayoutInflater.from(view.context).inflate(R.layout.activity_product_item_list, null)

                var alertDialog = AlertDialog.Builder(view.context)
                    .setTitle("確認刪除此產品")
                    .setMessage(str)
                    .setView(item)

                    .setPositiveButton("確定", null)
                    .setNegativeButton("取消", null)
                    .show()
                alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener {
                        //var i = 0
                        for (i in 0 until mFirebaseUserMenu!!.menuItems!!.size) {
                            var item : PRODUCT = mFirebaseUserMenu!!.menuItems!!.get(i)
                            if(item.itemName.equals(str)){
                                mFirebaseUserMenu!!.menuItems!!.remove(item)
                                break
                            }
                        }

                        mItemList.clear()
                        mFirebaseUserMenu!!.menuItems!!.forEach() {

                            mItemList.add(ProductPriceListData(it.itemName, it.itemPrice.toString()))
                        }
                        //---------------------------------------

                        mRecyclerViewProductList!!.adapter!!.notifyDataSetChanged()

                        alertDialog.dismiss()
                    }
            }
        }))

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