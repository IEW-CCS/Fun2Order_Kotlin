package com.iew.fun2order.order

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.db.firebase.*
import com.iew.fun2order.ui.home.adapter.RecipeItemAdapter
import com.iew.fun2order.ui.home.data.RecipeItemListData
import kotlin.collections.ArrayList


class OrderRecipe: AppCompatActivity() {
    var mBtnList :MutableList<Button> = mutableListOf()
    var mRecyclerViewRecipeItems: RecyclerView? = null
    var mItemList: MutableList<RecipeItemListData> = mutableListOf()
    var mMenuRecipes: MutableList<RECIPE> = mutableListOf()
    var checkOutMenuRecipes:ArrayList<RECIPE> = ArrayList<RECIPE>()

    //var mFdUserMenu: USER_MENU = USER_MENU()
    var mInflater: LayoutInflater? = null
    private lateinit var  mDialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.iew.fun2order.R.layout.activity_order_recipe)
        supportActionBar?.hide()

        //get RECIPE
        //val bundle = Bundle()

        val tmpMenuRecipe = intent?.extras?.getParcelableArrayList<RECIPE>("recipeList")

        tmpMenuRecipe?.forEach()
        {
            mMenuRecipes.add(it)
        }

        val context: Context = this@OrderRecipe
        val button2 = findViewById(com.iew.fun2order.R.id.button2) as Button

        button2.setOnClickListener {
            AddRecipeItem()
        }


        //add recipe type
        mMenuRecipes?.forEach {
            var item_data_list :MutableList<String> = mutableListOf()
            var item_data_select_list :MutableList<Boolean> = mutableListOf()
            it.recipeItems!!.forEach {
                item_data_list.add(it.recipeName.toString())
                var checkedFlag:Boolean = false
                item_data_select_list.add(checkedFlag)
            }
            var recipeItemListData = RecipeItemListData(it.recipeCategory,item_data_list,item_data_select_list, it.allowedMultiFlag)
            mItemList.add(recipeItemListData)
        }
        //var recipeItemListData = RecipeItemListData("ICE",item_data_list, false)

        //mItemList.add(recipeItemListData)
        mRecyclerViewRecipeItems = findViewById(com.iew.fun2order.R.id.recyclerViewRecipeItems) as RecyclerView
        val adapter = RecipeItemAdapter(mItemList,true)

        mRecyclerViewRecipeItems!!.setHasFixedSize(true)
        mRecyclerViewRecipeItems!!.layoutManager = LinearLayoutManager(context)
        mRecyclerViewRecipeItems!!.adapter = adapter



        val textViewAddMenuRecipe = findViewById(com.iew.fun2order.R.id.textViewAddMenuRecipe) as TextView
        // set on-click listener for ImageView
        textViewAddMenuRecipe.setOnClickListener {
            checkOutMenuRecipe()
            val bundle = Bundle()
            bundle.putParcelableArrayList("MENU_RECIPE", checkOutMenuRecipes)
            val intent = Intent().putExtras(bundle)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }


    }

    private fun AddRecipeItem() {
        // 取得 LinearLayout 物件
        // 取得 LinearLayout 物件
        val gridLayoutBtnList = findViewById(com.iew.fun2order.R.id.gridLayoutBtnList) as GridLayout
        val button2 = findViewById(com.iew.fun2order.R.id.button2) as Button
        // 將 TextView 加入到 LinearLayout 中
        var metrics = DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics); //抓取螢幕大小的資料
        var width = metrics.widthPixels/3 ;        // 螢幕的寬度/4放近with

        // 將 Button 1 加入到 LinearLayout 中
        val b1 = Button(this)
        b1.setText((mBtnList.size+1).toString())
        b1.setMinWidth(width)
        //this.getResources(),R.drawable.icon_add_group
        //b1.setBackgroundResource(com.iew.fun2order.R.drawable.button_bg)
        mBtnList.add(b1)
        //button = new Button[9] ;
        //GridLayout gridLayout = (GridLayout)findViewById(R.id.root) ;
        gridLayoutBtnList.setColumnCount(3);           // 設定GridLayout有幾行

        var iRow:Int = 0;
        iRow = (mBtnList.size+1)/3+1;

        gridLayoutBtnList.setRowCount(iRow);              // 設定GridLayout有幾列
        gridLayoutBtnList.removeView(button2)
        gridLayoutBtnList.addView(b1);
        gridLayoutBtnList.addView(button2)

    }

    private fun checkOutMenuRecipe() {
        checkOutMenuRecipes!!.clear()
        var iIdx:Int =0

        mItemList.forEach(){
            iIdx++
            var recipe: RECIPE = RECIPE()
            recipe.allowedMultiFlag = it.getAllowMulti()
            recipe.recipeCategory=it.getItemName()
            var iItemIdx:Int = 0
            var iItemCnt:Int = 0
            var itemDataSelectList = it.getItemDataSelectList()
            it.getItemDataList().forEach(){
                iItemIdx++
                if(itemDataSelectList[iItemIdx-1]){

                    iItemCnt++
                    var recipe_item : RECIPE_ITEM = RECIPE_ITEM()
                    recipe_item.checkedFlag = itemDataSelectList[iItemIdx-1]
                    recipe_item.recipeName = it.toString()
                    recipe_item.sequenceNumber = iItemCnt
                    recipe.recipeItems!!.add(recipe_item)
                }
            }
            recipe.sequenceNumber=iIdx
            checkOutMenuRecipes!!.add(recipe)
        }
    }
}