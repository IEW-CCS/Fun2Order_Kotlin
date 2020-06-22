package com.iew.fun2order.order

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.HorizontalNumberPicker
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.db.firebase.PRODUCT
import com.iew.fun2order.db.firebase.RECIPE
import com.iew.fun2order.ui.home.data.ProductPriceListData
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.ACTION_ADDRECIPE_CODE


class AddProductActivity : AppCompatActivity(), IAdapterOnClick {

    private lateinit var rcvAddProduct: RecyclerView
    private lateinit var txtProductName: TextView
    private lateinit var txtProductNote: TextView
    private lateinit var btnAdd: Button
    private lateinit var btnCancel: Button
    private lateinit var btnSetRecipe: Button
    private lateinit var txtProducList: TextView
    private lateinit var npkSelectCount: HorizontalNumberPicker
    private lateinit var txtSetRecipe: TextView

    private var selectProduct: ItemsLV_OrderProduct? = null
    private val lstProductList: MutableList<ItemsLV_OrderProduct> = mutableListOf()
    private val arraylistRecipes: ArrayList<RECIPE> = ArrayList<RECIPE>()
    private var lstmenuRecipes: MutableList<RECIPE> = mutableListOf()
    private var MenuOrderInfoPath = ""

    private lateinit var menuRef:DatabaseReference
    private lateinit var childEventListener: ChildEventListener


    override fun onStart() {
        super.onStart()

        if(MenuOrderInfoPath != "") {
            val MenuItemsPath = "$MenuOrderInfoPath/limitedMenuItems"
            menuRef = Firebase.database.getReference(MenuItemsPath)
            if(menuRef!= null) {
                menuRef.addChildEventListener(childEventListener)

                menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        dataSnapshot.children.forEach()
                        {
                            var upload =  it.getValue(PRODUCT::class.java)
                            lstProductList.find { it.itemName == upload!!.itemName }?.itemLimit  = upload!!.quantityRemained.toString()
                            RefreahItemDate()
                        }
                    }
                })
            }
        }
    }


    override fun onStop() {
        super.onStop()

        if(menuRef!= null) {
            menuRef.removeEventListener(childEventListener)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        supportActionBar?.hide()

        rcvAddProduct = findViewById(R.id.rcvAddproduct)
        txtProductName = findViewById(R.id.addproductName)
        txtProductNote = findViewById(R.id.addproductNote)
        btnAdd = findViewById(R.id.addproductOK)
        btnCancel = findViewById(R.id.addproductCancel)
        npkSelectCount = findViewById(R.id.addproductCount)
        btnSetRecipe = findViewById(R.id.addproductSetRecipe)
        txtProducList = findViewById(R.id.productList)
        txtSetRecipe = findViewById(R.id.setRecipeContent)


        intent?.extras?.let {

            val productList = it.getParcelableArrayList<ItemsLV_OrderProduct>("productList")
            val recipeList = it.getParcelableArrayList<RECIPE>("recipeList")
            MenuOrderInfoPath =  it.getString("MenuOrderInfoPath")


            lstProductList.clear()
            arraylistRecipes.clear()

            if (productList?.count() == 0) {
                //--- Richard  要求aways 可以 Free Key in
                //txtProductName.isEnabled = true
                txtProductName.clearFocus()
                txtProductNote.clearFocus()
                txtProducList.visibility = View.INVISIBLE
                rcvAddProduct.visibility = View.INVISIBLE
                txtProductNote.clearFocus()
            } else {
                //--- Richard  要求aways 可以 Free Key in
                // txtProductName.isEnabled = false
                txtProductName.clearFocus()
                txtProductNote.clearFocus()
                txtProducList.visibility = View.VISIBLE
                rcvAddProduct.visibility = View.VISIBLE
            }


            lstProductList.add(ItemsLV_OrderProduct("產品名稱", "價格", "限量", ""))
            productList?.forEach()
            { product ->
                lstProductList.add(product)
            }

            if (recipeList != null) {
                arraylistRecipes.addAll(recipeList.filterNotNull())
            }
        }

        txtSetRecipe.text = ""

        rcvAddProduct.layoutManager = LinearLayoutManager(this)
        rcvAddProduct.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rcvAddProduct.adapter = AdapterRC_OrderProduct(this, lstProductList, this)

        btnAdd.setOnClickListener {

            val selectItemName = txtProductName.text.toString().trim()
            if (selectItemName.isEmpty() || selectItemName == "") {

                var alertDialog = AlertDialog.Builder(this)
                    .setTitle("錯誤")
                    .setMessage("請至少選擇一個產品!!")
                    .setPositiveButton("確定", null)
                    .show()

            }
            else {

                if(!CheckReMained(selectItemName))
                {
                    var alertDialog = AlertDialog.Builder(this)
                        .setTitle("錯誤")
                        .setMessage("已選擇的產品已經沒有可購買的數量!!\n請重新選擇")
                        .setPositiveButton("確定") { dialog, _ ->
                            dialog.dismiss()
                            txtProductName.text = ""
                            txtSetRecipe.text = ""
                            lstmenuRecipes.clear()
                        }
                        .show()
                }
                else {
                    val selectItems: MENU_PRODUCT = MENU_PRODUCT()
                    selectItems.itemName = selectItemName
                    selectItems.itemComments = txtProductNote.text.toString()
                    selectItems.itemQuantity = npkSelectCount.value.toInt()

                    if (lstmenuRecipes.count() != 0) {
                        selectItems.menuRecipes = lstmenuRecipes.toMutableList()
                    }

                    val bundle = Bundle()
                    bundle.putParcelable("SelectItem", selectItems)
                    val intent = Intent().putExtras(bundle)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }

        btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnSetRecipe.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelableArrayList("recipeList", arraylistRecipes)
            val intent = Intent(this, OrderRecipe::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, ACTION_ADDRECIPE_CODE)
        }


        childEventListener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val upload: PRODUCT = p0.getValue(PRODUCT::class.java)!!
                lstProductList.find { it.itemName == upload.itemName }?.itemLimit  = upload.quantityRemained .toString()
                RefreahItemDate()
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        }


    }


    private fun CheckReMained(itemName : String) : Boolean
    {
        var result = true
        val limit = lstProductList.find { it.itemName == itemName }?.itemLimit
        if(limit != null)
        {
            if(limit != "") {
                val count = limit.toInt()
                if(count <=0) {
                    result = false
                }
            }
        }
        return result
    }

    private fun RefreahItemDate()
    {
       if( rcvAddProduct.adapter  != null)
       {
           rcvAddProduct.adapter!!.notifyDataSetChanged()
       }
    }

    override fun onClick(sender: String, pos: Int, type: Int) {
        when (type) {
            0 -> {
                if(pos != 0) {
                    selectProduct = lstProductList[pos] as ItemsLV_OrderProduct
                    txtProductName.text = selectProduct?.itemName
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ACTION_ADDRECIPE_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val bundle = Bundle()
                    val selectMenuRecipe =
                        data?.extras?.getParcelableArrayList<RECIPE>("MENU_RECIPE")
                    if (selectMenuRecipe != null) {
                        lstmenuRecipes.clear()
                        lstmenuRecipes = selectMenuRecipe.toMutableList()

                        txtSetRecipe.text = ""
                        var setRecipe : String = ""
                        lstmenuRecipes?.forEach {recipe->
                            recipe.recipeItems?.forEach{recipeItem->
                                if(recipeItem.checkedFlag == true)
                                {
                                    setRecipe += recipeItem.recipeName + " "
                                }
                            }
                        }
                        txtSetRecipe.text = setRecipe
                    }
                }
            }
            else -> {
                println("no handler onActivityReenter")
            }
        }
    }
}
