package com.iew.fun2order.ui.shop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.annotations.SerializedName
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.*
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import kotlinx.android.synthetic.main.activity_join_order_select_items.*
import kotlinx.android.synthetic.main.numberpicker_horizontal.view.*


class ActivityJoinOrderStandSelectItems : AppCompatActivity(), IAdapterOnClick {

    private var selectRecipeInfo : MutableList<RECIPE> = mutableListOf()
    private var basicPrice : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_order_select_items)
        supportActionBar?.title = "選擇產品配方"
        selectRecipeInfo.clear()

        val selectProductItems = intent.extras?.get("SELECT_PRODUCT") as String
        val MenuInfo = intent.extras?.get("MENU_INFO") as USER_MENU
        val itemPrice = intent.extras?.get("PRODUCT_PRICE") as String
        val itemLimit = intent.extras?.get("LIMIT_COUNT") as String


        try {
            basicPrice = itemPrice.toInt()
            productPrice.text = basicPrice.toString()
        }
        catch (ex:Exception){
            basicPrice = 0
        }

        ItemName.text = selectProductItems

        rcv_ProductRecipe.setHasFixedSize(true)
        rcv_ProductRecipe.layoutManager = LinearLayoutManager(this)
        rcv_ProductRecipe.adapter = AdapterRC_StandRecipe(this, selectRecipeInfo, this)
        rcv_ProductRecipe.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rcv_ProductRecipe.animation = null
        rcv_ProductRecipe.itemAnimator = null


        addproductCount.et_number.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                upDateUI( 0, addproductCount.value)
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int){
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int)
            {

            }
        })

        btnAdd.setOnClickListener {
            // Check MandatoryFlag
            if(itemLimit != "")
            {
                val itemLimitNumber = itemLimit.toIntOrNull()
                if(itemLimitNumber != null)
                {
                    if(addproductCount.value > itemLimitNumber)
                    {
                        AlertDialog.Builder(this)
                            .setTitle("錯誤訊息")
                            .setMessage("所選擇的限量產品: $selectProductItems 庫存不足, 請重新調整購買數量")
                            .setPositiveButton("確定", null)
                            .create()
                            .show()
                        return@setOnClickListener
                    }
                }
            }



            var index = 0
            val selectItems: MENU_PRODUCT = MENU_PRODUCT()
            selectItems.itemName = selectProductItems
            selectItems.itemComments = productNote.text.toString()
            selectItems.itemQuantity = addproductCount.value
            selectItems.itemPrice = productPrice.text.toString().toInt()

            selectRecipeInfo.forEach { selectRecipe ->
                val recipeItems: RECIPE = RECIPE()
                recipeItems.recipeCategory = selectRecipe.recipeCategory
                index = 0
                selectRecipe.recipeItems?.forEach {
                    recipeItems.recipeItems?.add(
                        RECIPE_ITEM(
                            it.checkedFlag,
                            it.recipeName,
                            index
                        )
                    )
                    index++
                }
                selectItems.menuRecipes?.add(recipeItems)
            }

            val bundle = Bundle()
            bundle.putParcelable("SelectItem", selectItems)
            val intent = Intent().putExtras(bundle)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }


        if(MenuInfo != null) {
            selectRecipeInfo.clear()
            val sortedMenuRecipe = MenuInfo.menuRecipes?.sortedBy { it -> it.sequenceNumber }
            sortedMenuRecipe?.forEach { it ->
                it.recipeItems?.forEach {
                    items -> items.checkedFlag = false
                }
                selectRecipeInfo.add(it)
            }

            if(rcv_ProductRecipe.adapter != null) {
                rcv_ProductRecipe.adapter!!.notifyDataSetChanged()
            }

        }
        else
        {
            AlertDialog.Builder(this)
                .setTitle("錯誤訊息")
                .setMessage("菜單資訊不存在")
                .setPositiveButton("確定", null)
                .create()
                .show()

        }
    }

    override fun onClick(sender: String, pos: Int, type: Int) {

       if(sender == "SelectRecipe")
       {
           upDateUI( 0, addproductCount.value)
       }
    }


    private  fun upDateUI( UnitPrice:Int, Quantity:Int)
    {
        val totalPrice = (basicPrice + UnitPrice) * Quantity
        productPrice.text = totalPrice.toString()
    }

}