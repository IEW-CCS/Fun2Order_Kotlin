package com.iew.fun2order.ui.shop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.DETAIL_MENU_INFORMATION
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.db.firebase.RECIPE
import com.iew.fun2order.db.firebase.RECIPE_ITEM
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import kotlinx.android.synthetic.main.activity_join_order_select_items.*
import kotlinx.android.synthetic.main.numberpicker_horizontal.view.*


class ActivityJoinOrderDetailSelectItems : AppCompatActivity(), IAdapterOnClick {

    private var selectRecipeInfo : MutableList<ItemsLV_DetailRecipe> = mutableListOf()
    private var basicPrice : Int = 0
    private var estimatedUnitPrice : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_order_select_items)
        supportActionBar?.title = "選擇產品配方"
        selectRecipeInfo.clear()

        val selectProductCategory= intent.extras?.get("SELECT_CATEGORY") as String
        val selectProductItems = intent.extras?.get("SELECT_PRODUCT") as String
        val detailMenuInfo = intent.extras?.get("DETAIL_MENU_INFO") as DETAIL_MENU_INFORMATION

        ItemName.text = selectProductItems

        rcv_ProductRecipe.setHasFixedSize(true)
        rcv_ProductRecipe.layoutManager = LinearLayoutManager(this)
        rcv_ProductRecipe.adapter = AdapterRC_DetailRecipe(this, selectRecipeInfo, this)
        rcv_ProductRecipe.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rcv_ProductRecipe.animation = null
        rcv_ProductRecipe.itemAnimator = null


        addproductCount.et_number.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                upDateUI( estimatedUnitPrice, addproductCount.value)
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int){
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int)
            {

            }
        })

        btnAdd.setOnClickListener {

            // Check MandatoryFlag
            var index = 0
            if (!checkMandatory()) {
                var alertDialog = AlertDialog.Builder(this)
                    .setTitle("錯誤")
                    .setMessage("請確定所有必選的項目, 均已經選擇完畢.")
                    .setPositiveButton("確定",null)
                    .show()
            } else {

                val selectItems: MENU_PRODUCT = MENU_PRODUCT()
                selectItems.itemName = selectProductItems
                selectItems.itemComments = productNote.text.toString()
                selectItems.itemQuantity = addproductCount.value
                selectItems.itemPrice = productPrice.text.toString().toInt()

                selectRecipeInfo.forEach {
                    selectRecipe ->

                    val recipeItems : RECIPE = RECIPE()
                    recipeItems.recipeCategory = selectRecipe.recipetemplate.templateName
                    index = 0
                    selectRecipe.recipetemplate.recipeList?.forEach {
                        recipeItems.recipeItems?.add(RECIPE_ITEM(it.itemCheckedFlag,it.itemName,index))
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

        }


        if(detailMenuInfo != null) { 
            selectRecipeInfo.clear()
            val productCategory = detailMenuInfo.productCategory?.firstOrNull { it -> it.categoryName == selectProductCategory }
            if (productCategory != null) {

                val productItems = productCategory.productItems?.firstOrNull { item -> item.productName == selectProductItems }
                val productPriceTemplate = productCategory.priceTemplate

                if (productItems != null) {
                    productPrice.text = productItems.productBasicPrice.toString()

                    val recipeTemplate = detailMenuInfo.recipeTemplates
                    val recipeList = productItems.recipeRelation?.sortedBy { it -> it.templateSequence }
                    val tempPriceSequence = productPriceTemplate.templateSequence

                    val priceItem = productItems.priceList.firstOrNull{it -> it.recipeItemName == "價格"}
                    if( priceItem != null) {
                        basicPrice = priceItem.price
                        productPrice.text = basicPrice.toString()
                    }

                    recipeList?.forEach { recipe ->

                        //----- 整理RecipeItems 內容 -----
                        val recipeItems = recipeTemplate?.firstOrNull { it -> it.templateSequence == recipe.templateSequence }
                        recipe.itemRelation.forEachIndexed { index, Item ->
                          recipeItems?.recipeList?.get(index)?.itemDisplayFlag = Item
                        }

                        if(recipe.templateSequence == tempPriceSequence)
                        {
                            productItems.priceList.forEach {
                                val tmpRecipe =  recipeItems?.recipeList?.firstOrNull { tmp -> tmp.itemName == it.recipeItemName }
                                if(tmpRecipe != null)
                                {
                                    tmpRecipe.optionalPrice = it.price
                                }
                            }
                            basicPrice = 0
                        }

                        if(recipeItems!= null)
                        {
                            selectRecipeInfo.add(ItemsLV_DetailRecipe(recipeItems!!))
                        }
                    }

                    if(rcv_ProductRecipe.adapter != null) {
                        rcv_ProductRecipe.adapter!!.notifyDataSetChanged()
                    }

                } else {
                    AlertDialog.Builder(this)
                        .setTitle("錯誤訊息")
                        .setMessage("所選擇的產品:${selectProductItems} 資訊錯誤")
                        .setPositiveButton("確定", null)
                        .create()
                        .show()

                }

            } else {
                AlertDialog.Builder(this)
                    .setTitle("錯誤訊息")
                    .setMessage("所選擇的分類:${selectProductCategory} 資訊錯誤")
                    .setPositiveButton("確定", null)
                    .create()
                    .show()

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
           EstimatedUnitPrice()
           upDateUI( estimatedUnitPrice, addproductCount.value)
       }
    }

    private fun EstimatedUnitPrice()
    {
        var price = 0
        estimatedUnitPrice = 0
        selectRecipeInfo.forEach{
            val recipeCheckItems = it.recipetemplate.recipeList?.filter { item->item.itemCheckedFlag == true }
            val tmpPrice = recipeCheckItems?.sumBy { price -> price.optionalPrice }
            price += tmpPrice ?: 0
        }
        estimatedUnitPrice = price
    }

    private  fun upDateUI( UnitPrice:Int, Quantity:Int)
    {
        val totalPrice = (basicPrice + UnitPrice) * Quantity
        productPrice.text = totalPrice.toString()
    }


    private  fun checkMandatory( ):Boolean
    {
        var result : Boolean = true
        selectRecipeInfo.forEach {
            if (it.recipetemplate.mandatoryFlag)
            {
                val checkedCount = it.recipetemplate.recipeList?.count { item -> item.itemCheckedFlag } ?: 0
                if(checkedCount == 0)
                {
                    result = false
                }
            }
        }
        return result
    }
}