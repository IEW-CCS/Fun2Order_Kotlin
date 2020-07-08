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
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.PRODUCT
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.ui.home.adapter.ProductPriceItemAdapter
import com.iew.fun2order.ui.home.adapter.SwipeAndDragHelper
import com.iew.fun2order.ui.home.data.ProductPriceListData
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.SpacesItemDecoration
import kotlinx.android.synthetic.main.row_product_item.view.*


class ActivityProductPriceList: AppCompatActivity(), IAdapterOnClick {

    private lateinit var titleroductList: LinearLayout
    private lateinit var mRecyclerViewProductList: RecyclerView

    private var mFirebaseUserMenu: USER_MENU = USER_MENU()
    var mItemList: MutableList<ProductPriceListData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.iew.fun2order.R.layout.activity_product_item_list)
        supportActionBar?.hide()

        val context: Context = this@ActivityProductPriceList
        val decorationSpace = 30

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


        mFirebaseUserMenu = intent.extras?.get("USER_MENU") as USER_MENU

        mRecyclerViewProductList = findViewById<RecyclerView>(R.id.recyclerViewProductList)

        val adapter = ProductPriceItemAdapter(mItemList, this)

        val swipeAndDragHelper = SwipeAndDragHelper(adapter)
        val touchHelper = ItemTouchHelper(swipeAndDragHelper)
        adapter.setTouchHelper(touchHelper)

        mRecyclerViewProductList.setHasFixedSize(true)
        mRecyclerViewProductList.layoutManager = LinearLayoutManager(context)
        mRecyclerViewProductList.adapter = adapter

        touchHelper.attachToRecyclerView(mRecyclerViewProductList)

        mRecyclerViewProductList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        mRecyclerViewProductList.addItemDecoration(SpacesItemDecoration(decorationSpace))


        mItemList.clear()
        mFirebaseUserMenu.menuItems!!.forEach() {
            mItemList.add(ProductPriceListData(it.itemName, it.itemPrice.toString(), it.quantityLimitation?.toString()))
        }

        mRecyclerViewProductList.adapter!!.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        val bundle = Bundle()
        bundle.putString("Result", "OK")
        bundle.putParcelable("USER_MENU", mFirebaseUserMenu)
        val intent = Intent().putExtras(bundle)
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    override fun onClick(sender: String, pos: Int, type: Int) {
        val buttonActions = arrayOf("刪除項目", "編輯項目", "複製項目")
        AlertDialog.Builder(this)
            .setTitle("請選擇操作項目")
            .setItems(buttonActions,  DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    0 -> { checkRemoveItems(pos)}
                    1 -> { checkModifyItems(pos)}
                    2 -> { checkCopyItems(pos)}
                }
            })
            .setNegativeButton("關閉", null)
            .create()
            .show()
    }

    private fun checkModifyItems(position: Int) {

        val str = mItemList.get(position).getItemName()
        val getItems = mFirebaseUserMenu.menuItems!!.filter { it.itemName == str }.firstOrNull()
        if (getItems != null) {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_product_price, null)
            val radioGroup  = item.findViewById(R.id.radioGroup) as RadioGroup
            val radio1      = item.findViewById(R.id.radioLimit) as RadioButton
            val radio2      = item.findViewById(R.id.radioNoLimit) as RadioButton
            val editLimitCount = item.findViewById(R.id.editLimitCount) as EditText
            val editTextProduct = item.findViewById(R.id.editTextProduct) as EditText
            val editTextProductPrice = item.findViewById(R.id.editTextProductPrice) as EditText

            //---- 填入正確的數值 -----------
            editTextProduct.text = Editable.Factory.getInstance().newEditable(getItems.itemName)
            editTextProduct.isEnabled = false
            editTextProduct.isClickable = false
            editTextProductPrice.text =
                Editable.Factory.getInstance().newEditable(getItems.itemPrice.toString())

            if (getItems.quantityLimitation != null) {
                radioGroup.check(radio1.id)
                editLimitCount.visibility = View.VISIBLE
                editLimitCount.isClickable = true
                editLimitCount.text = Editable.Factory.getInstance()
                    .newEditable(getItems.quantityLimitation.toString())
            } else {
                radioGroup.check(radio2.id)
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

                    if (radio1.isChecked && editLimitCount.text.toString() == "") {
                        editLimitCount.requestFocus()
                        editLimitCount.error = "限量數量必須填寫!"
                    } else {
                        val fdProduct: PRODUCT = PRODUCT()
                        fdProduct.itemName = editTextProduct.getText().toString().trim()
                        try {
                            val parsedInt =
                                editTextProductPrice.getText().toString().toInt()
                            fdProduct.itemPrice = parsedInt
                            if (radio1.isChecked && editLimitCount.text.toString() != "") {
                                val LimitCount = editLimitCount.text.toString().toInt()
                                fdProduct.quantityLimitation = LimitCount
                                fdProduct.quantityRemained = LimitCount
                            } else if (radio2.isChecked) {
                                fdProduct.quantityLimitation = null
                                fdProduct.quantityRemained = null
                            }

                        } catch (nfe: NumberFormatException) {
                            fdProduct.itemPrice = 0
                        }

                        //------ Real Update
                        getItems.quantityRemained = fdProduct.quantityRemained
                        getItems.quantityLimitation = fdProduct.quantityLimitation
                        getItems.itemName = fdProduct.itemName
                        getItems.itemPrice = fdProduct.itemPrice
                        getItems.sequenceNumber = fdProduct.sequenceNumber

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

    private fun checkCopyItems(position: Int) {

        val str = mItemList.get(position).getItemName()
        val addPosition = position + 1
        val getItems = mFirebaseUserMenu.menuItems!!.filter { it.itemName == str }.firstOrNull()
        if (getItems != null) {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_product_price, null)
            val radioGroup = item.findViewById(R.id.radioGroup) as RadioGroup
            val radio1 = item.findViewById(R.id.radioLimit) as RadioButton
            val radio2 = item.findViewById(R.id.radioNoLimit) as RadioButton

            val editLimitCount = item.findViewById(R.id.editLimitCount) as EditText
            val editTextProduct = item.findViewById(R.id.editTextProduct) as EditText
            val editTextProductPrice = item.findViewById(R.id.editTextProductPrice) as EditText

            //---- 填入正確的數值 -----------
            editTextProduct.hint = getItems.itemName
            editTextProductPrice.text =
                Editable.Factory.getInstance().newEditable(getItems.itemPrice.toString())

            if (getItems.quantityLimitation != null) {
                radioGroup.check(radio1.id)
                editLimitCount.visibility = View.VISIBLE
                editLimitCount.isClickable = true
                editLimitCount.text = Editable.Factory.getInstance().newEditable(getItems.quantityLimitation.toString())

            } else {
                radioGroup.check(radio2.id)
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

                    if (TextUtils.isEmpty(editTextProduct.text.trim())) {
                        editTextProduct.requestFocus()
                        editTextProduct.error = "產品名稱不能為空白!"
                    } else {
                        var bFound = false
                        mFirebaseUserMenu.menuItems!!.forEach {
                            if (it.itemName.equals(editTextProduct.getText().toString().trim())) {
                                bFound = true
                            }
                        }
                        if (bFound) {
                            editTextProduct.requestFocus()
                            editTextProduct.error = "產品名稱不能重覆!"
                        } else {
                            if (radio1.isChecked && editLimitCount.text.toString() == "") {
                                editLimitCount.requestFocus()
                                editLimitCount.error = "限量數量必須填寫!"
                            } else {

                                val fdProduct: PRODUCT = PRODUCT()
                                fdProduct.itemName = editTextProduct.getText().toString().trim()
                                try {
                                    val parsedInt = editTextProductPrice.getText().toString().toInt()
                                    fdProduct.itemPrice = parsedInt
                                    if (radio1.isChecked && editLimitCount.text.toString() != "") {
                                        val LimitCount = editLimitCount.text.toString().toInt()
                                        fdProduct.quantityLimitation = LimitCount
                                        fdProduct.quantityRemained = LimitCount
                                    } else if (radio2.isChecked) {
                                        fdProduct.quantityLimitation = null
                                        fdProduct.quantityRemained = null
                                    }
                                } catch (nfe: NumberFormatException) {
                                    fdProduct.itemPrice = 0
                                }
                                fdProduct.sequenceNumber = mFirebaseUserMenu.menuItems!!.size + 1
                                mFirebaseUserMenu.menuItems!!.add(addPosition,fdProduct)
                                mItemList.add(addPosition,
                                    ProductPriceListData(
                                        fdProduct.itemName,
                                        fdProduct.itemPrice.toString(),
                                        fdProduct.quantityLimitation?.toString()
                                    )
                                )
                                alertDialog.dismiss()
                            }
                        }
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
            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}