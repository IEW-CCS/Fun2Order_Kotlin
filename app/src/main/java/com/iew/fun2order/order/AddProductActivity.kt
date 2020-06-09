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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.HorizontalNumberPicker
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.db.firebase.RECIPE
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

    private var selectProduct: ItemsLV_OrderProduct? = null
    private val lstProductList: MutableList<ItemsLV_OrderProduct> = mutableListOf()
    private val arraylistRecipes: ArrayList<RECIPE> = ArrayList<RECIPE>()
    private var lstmenuRecipes: MutableList<RECIPE> = mutableListOf()

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


        intent?.extras?.let {

            val productList = it.getParcelableArrayList<ItemsLV_OrderProduct>("productList")
            val recipeList = it.getParcelableArrayList<RECIPE>("recipeList")

            lstProductList.clear()
            arraylistRecipes.clear()




            if (productList?.count() == 0) {
                txtProductName.isEnabled = true
                txtProducList.visibility = View.INVISIBLE
                rcvAddProduct.visibility = View.INVISIBLE
                txtProductNote.clearFocus()
            } else {
                txtProductName.isEnabled = false
                txtProductName.clearFocus()
                txtProductNote.clearFocus()
                txtProducList.visibility = View.VISIBLE
                rcvAddProduct.visibility = View.VISIBLE
            }

            productList?.forEach()
            { product ->
                lstProductList.add(product)
            }

            if (recipeList != null) {
                arraylistRecipes.addAll(recipeList.filterNotNull())
            }
        }

        rcvAddProduct.layoutManager = LinearLayoutManager(this)
        rcvAddProduct.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rcvAddProduct.adapter = AdapterRC_OrderProduct(this, lstProductList, this)

        btnAdd.setOnClickListener {

            val selectItemName = txtProductName.text.toString().trim()
            if (selectItemName.isEmpty() || selectItemName == "") {
                Toast.makeText(this, "Please Select Product", Toast.LENGTH_SHORT).show()
            } else {
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
    }

    override fun onClick(sender: String, pos: Int, type: Int) {
        when (type) {
            0 -> {
                selectProduct = lstProductList[pos] as ItemsLV_OrderProduct
                txtProductName.text = selectProduct?.itemName
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
                    }
                }
            }
            else -> {
                println("no handler onActivityReenter")
            }
        }
    }
}
