package com.iew.fun2order.ui.home

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.ProductDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.entity.Product
import com.iew.fun2order.db.firebase.PRODUCT
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.ui.home.adapter.ProductPriceItemAdapter
import com.iew.fun2order.ui.home.adapter.SwipeAndDragHelper
import com.iew.fun2order.ui.home.data.ProductPriceListData
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.RecyclerItemClickListenr
import com.iew.fun2order.utility.SpacesItemDecoration
import kotlinx.android.synthetic.main.row_product_item.view.*


class ActivityProductPriceList: AppCompatActivity(), IAdapterOnClick {

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

        itemView.imageview_reorder.visibility = View.GONE

        titleroductList = findViewById<LinearLayout>(R.id.titleProductList)
        titleroductList.addView(itemView)

        //mDBContext = AppDatabase(context!!)
        //mProductDB = mDBContext.productdao()

        val sMenuID = intent.extras.getString("MENU_ID")
        mFirebaseUserMenu = intent.extras.get("USER_MENU") as USER_MENU

        mRecyclerViewProductList = findViewById(com.iew.fun2order.R.id.recyclerViewProductList) as RecyclerView
        //activity!!.findViewById<View>(R.id.recyclerViewMenuItems) as RecyclerView
        val adapter = ProductPriceItemAdapter(mItemList, this)


        val swipeAndDragHelper = SwipeAndDragHelper(adapter)
        val touchHelper = ItemTouchHelper(swipeAndDragHelper)
        adapter.setTouchHelper(touchHelper)

        mRecyclerViewProductList!!.setHasFixedSize(true)
        mRecyclerViewProductList!!.layoutManager = LinearLayoutManager(context)
        mRecyclerViewProductList!!.adapter = adapter

        touchHelper.attachToRecyclerView(mRecyclerViewProductList)

        mRecyclerViewProductList!!.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        val space = 30
        mRecyclerViewProductList!!.addItemDecoration(SpacesItemDecoration(space))
        //var list : List<Product> = mProductDB.getProductByMenuID(sMenuID)


        mItemList.clear()
        mFirebaseUserMenu!!.menuItems!!.forEach() {

            mItemList.add(ProductPriceListData(it.itemName, it.itemPrice.toString(), it.quantityLimitation?.toString()))
        }
            //---------------------------------------

        mRecyclerViewProductList!!.adapter!!.notifyDataSetChanged()



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




    override fun onClick(sender: String, pos: Int, type: Int) {
        val buttonActions = arrayOf("刪除項目!!", "修改項目!!")
        AlertDialog.Builder(this)
            .setTitle("請選擇操作項目")
            .setItems(buttonActions,  DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 -> { checkRemoveItems(pos) }
                    1 -> { CheckModifyItems(pos)}
                }
            })
            .setNegativeButton("關閉", null)
            .create()
            .show()

    }

    private fun CheckModifyItems(position: Int) {

        val str = mItemList.get(position).getItemName()

        var getItems =  mFirebaseUserMenu.menuItems!!.filter { it.itemName == str }.firstOrNull()
        if(getItems != null) {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_product_price, null)
            val radioGroup = item.findViewById(R.id.radioGroup) as RadioGroup
            val Radio1 = item.findViewById(R.id.radioLimit) as RadioButton
            val Radio2 = item.findViewById(R.id.radioNoLimit) as RadioButton

            var editLimitCount = item.findViewById(R.id.editLimitCount) as EditText
            var editTextProduct = item.findViewById(R.id.editTextProduct) as EditText
            var editTextProductPrice = item.findViewById(R.id.editTextProductPrice) as EditText

            //---- 填入正確的數值 -----------
            editTextProduct.text =  Editable.Factory.getInstance().newEditable(getItems.itemName)
            editTextProduct.isEnabled = false
            editTextProduct.isClickable = false
            editTextProductPrice.text =  Editable.Factory.getInstance().newEditable(getItems.itemPrice.toString())

            if(getItems.quantityLimitation != null)
            {
                radioGroup.check(Radio1.id)
                editLimitCount.visibility = View.VISIBLE
                editLimitCount.isClickable = true
                editLimitCount.text =  Editable.Factory.getInstance().newEditable(getItems.quantityLimitation.toString())

            }
            else
            {
                radioGroup.check(Radio2.id)
                editLimitCount.visibility = View.INVISIBLE
                editLimitCount.isClickable = false
            }

            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
                if (radioButton.text == "限量") {
                    editLimitCount.visibility = View.VISIBLE
                } else {
                    editLimitCount.visibility = View.INVISIBLE
                    editLimitCount.isClickable = false
                }
            }



            var alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setCancelable(false)
                .setPositiveButton("確定", null)
                .setNegativeButton("取消", null)
                .show()

            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    if (Radio1.isChecked && editLimitCount.text.toString() == "") {
                        editLimitCount.requestFocus()
                        editLimitCount.error = "限量數量必須填寫!"
                    } else {

                        var fdproduct: PRODUCT = PRODUCT()
                        fdproduct.itemName = editTextProduct.getText().toString().trim()
                        try {
                            val parsedInt =
                                editTextProductPrice.getText().toString().toInt()
                            fdproduct.itemPrice = parsedInt
                            if (Radio1.isChecked && editLimitCount.text.toString() != "") {
                                val LimitCount = editLimitCount.text.toString().toInt()
                                fdproduct.quantityLimitation = LimitCount
                                fdproduct.quantityRemained = LimitCount
                            }
                            else if(Radio2.isChecked)
                            {
                                fdproduct.quantityLimitation = null
                                fdproduct.quantityRemained = null
                            }

                        } catch (nfe: NumberFormatException) {
                            fdproduct.itemPrice = 0
                        }


                        //------ Real Update
                        getItems.quantityRemained = fdproduct.quantityRemained
                        getItems.quantityLimitation = fdproduct.quantityLimitation
                        getItems.itemName = fdproduct.itemName
                        getItems.itemPrice = fdproduct.itemPrice
                        getItems.sequenceNumber = fdproduct.sequenceNumber

                        mItemList.clear()
                        mFirebaseUserMenu!!.menuItems!!.forEach() {
                            mItemList.add(
                                ProductPriceListData(
                                    it.itemName,
                                    it.itemPrice.toString(),
                                    it.quantityLimitation?.toString()
                                )
                            )
                        }

                        mRecyclerViewProductList!!.adapter!!.notifyDataSetChanged()
                        alertDialog.dismiss()
                    }

                }
        }
    }


    private fun checkRemoveItems(position: Int) {
        val str = mItemList.get(position).getItemName()
        var alertDialog = AlertDialog.Builder(this)
            .setTitle("確認刪除此產品")
            .setMessage(str)
            .setPositiveButton("確定") { dialog, _ ->
                for (i in 0 until mFirebaseUserMenu!!.menuItems!!.size) {
                    var item: PRODUCT = mFirebaseUserMenu!!.menuItems!!.get(i)
                    if (item.itemName.equals(str)) {
                        mFirebaseUserMenu!!.menuItems!!.remove(item)
                        break
                    }
                }
                mItemList.clear()
                mFirebaseUserMenu!!.menuItems!!.forEach() {
                    mItemList.add(
                        ProductPriceListData(
                            it.itemName,
                            it.itemPrice.toString(),
                            it.quantityLimitation?.toString()
                        )
                    )
                }
                mRecyclerViewProductList!!.adapter!!.notifyDataSetChanged()
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}