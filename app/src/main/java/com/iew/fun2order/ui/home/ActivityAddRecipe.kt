package com.iew.fun2order.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.R
import com.iew.fun2order.db.entity.MenuType
import com.iew.fun2order.db.firebase.*
import com.iew.fun2order.ui.home.adapter.RecipeItemAdapter
import com.iew.fun2order.ui.home.data.MenuItemListData
import com.iew.fun2order.ui.home.data.RecipeItemListData
import info.hoang8f.android.segmented.SegmentedGroup
import java.text.SimpleDateFormat
import java.util.*


class ActivityAddRecipe: AppCompatActivity() {
    var mBtnList :MutableList<Button> = mutableListOf()
    var mRecyclerViewRecipeItems: RecyclerView? = null
    var mItemList: MutableList<RecipeItemListData> = mutableListOf()
    var menuRecipes: MutableList<RECIPE> = mutableListOf()
    var mFdUserMenu: USER_MENU = USER_MENU()
    private lateinit var mDatabase: DatabaseReference
    var mInflater: LayoutInflater? = null
    private lateinit var  mDialog : AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.iew.fun2order.R.layout.activity_add_recipe)
        supportActionBar?.hide()

        //get RECIPE
        //val bundle = Bundle()
        mFdUserMenu = intent?.extras?.get("USER_MENU") as USER_MENU

        val context: Context = this@ActivityAddRecipe
        mDatabase = Firebase.database.reference

        val button2 = findViewById(com.iew.fun2order.R.id.button2) as Button

        // 將 TextView 加入到 LinearLayout 中
        //var metrics = DisplayMetrics();
        //getWindowManager().getDefaultDisplay().getMetrics(metrics); //抓取螢幕大小的資料
        //var width = metrics.widthPixels/3 ;        // 螢幕的寬度/4放近with
        //button2.setMinWidth(width)
        // set on-click listener for textViewMakeMenu
        button2.setOnClickListener {
            AddRecipeItem()
        }


        //add recipe type
        mFdUserMenu.menuRecipes!!.forEach {
            var item_data_list :MutableList<String> = mutableListOf()
            var item_data_select_list :MutableList<Boolean> = mutableListOf()
            it.recipeItems!!.forEach {
                item_data_list.add(it.recipeName.toString())
                var checkedFlag:Boolean = it.checkedFlag!!
                item_data_select_list.add(checkedFlag)
            }
            var recipeItemListData = RecipeItemListData(it.recipeCategory,item_data_list,item_data_select_list, false)
            mItemList.add(recipeItemListData)
        }
        //var recipeItemListData = RecipeItemListData("ICE",item_data_list, false)

        //mItemList.add(recipeItemListData)
        mRecyclerViewRecipeItems = findViewById(com.iew.fun2order.R.id.recyclerViewRecipeItems) as RecyclerView
        val adapter = RecipeItemAdapter(mItemList,false)

        mRecyclerViewRecipeItems!!.setHasFixedSize(true)
        mRecyclerViewRecipeItems!!.layoutManager = LinearLayoutManager(context)
        mRecyclerViewRecipeItems!!.adapter = adapter

        val textViewAddRecipeType = findViewById(com.iew.fun2order.R.id.textViewAddRecipeType) as TextView
        // set on-click listener for ImageView
        textViewAddRecipeType.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(com.iew.fun2order.R.layout.alert_input_recipe_type, null)

            var alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setPositiveButton("確定", null)
                .setNegativeButton("取消", null)
                .show()

            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    var editTextInputRecipeType = item.findViewById(com.iew.fun2order.R.id.editTextInputRecipeType) as EditText
                    var checkBoxAllowMultiChoose = item.findViewById(com.iew.fun2order.R.id.checkBoxAllowMultiChoose) as CheckBox

                    if (TextUtils.isEmpty(editTextInputRecipeType.text))
                    {
                        editTextInputRecipeType.requestFocus()
                        editTextInputRecipeType.error = "配方類別不能為空白!"
                    }else {

                        //Toast.makeText(applicationContext,
                        //    "加入配方類別:"+editTextInputRecipeType.getText().toString(), Toast.LENGTH_SHORT).show()
                        AddRecipeTypeItem(editTextInputRecipeType.getText().toString(), checkBoxAllowMultiChoose.isChecked)
                        alertDialog.dismiss()
                    }
                }
        }

        val textViewAddMenuRecipe = findViewById(com.iew.fun2order.R.id.textViewAddMenuRecipe) as TextView
        // set on-click listener for ImageView
        textViewAddMenuRecipe.setOnClickListener {
            createNewMenuRecipe()
            val bundle = Bundle()
            bundle.putString("Result", "OK")
            bundle.putParcelable("USER_MENU", mFdUserMenu)
            val intent = Intent().putExtras(bundle)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        val textViewSelectTemplate = findViewById(com.iew.fun2order.R.id.textViewSelectTemplate) as TextView
        // set on-click listener for ImageView
        textViewSelectTemplate.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_recipe_template, null)


            val alertDialog =
                AlertDialog.Builder(this@ActivityAddRecipe)
            alertDialog.setView(item)

            alertDialog.setNegativeButton("取消", null)
            //alertDialog.show()
            mDialog = alertDialog.show();

            var segmentedGroupRecipeTemplate = item.findViewById(R.id.segmentedGroupRecipeTemplate) as SegmentedGroup
            //var menuTypelist = mMenuTypeDB.getMenuTypeslist()
            //getRecipeTemplateList(item)
            val default = segmentedGroupRecipeTemplate!!.getChildAt(0) as RadioButton
            default.isChecked = true

            val menutype = default.text.toString()

            if(menutype.equals("官方範本")){
                getRecipeTemplateList(item)
            }else{
                getCustRecipeTemplateList(item)
            }

            segmentedGroupRecipeTemplate!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)

                val menutype = radioButton.text.toString()
                if(menutype.equals("官方範本")){
                    getRecipeTemplateList(item)
                }else{
                    getCustRecipeTemplateList(item)
                }

            })

        }

        val textViewSaveNewTemplate = findViewById(R.id.textViewSaveNewTemplate) as TextView
        // set on-click listener for TextView
        textViewSaveNewTemplate.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_template_name, null)

            var alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setPositiveButton("確定", null)
                .setNegativeButton("取消", null)
                .show()

            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    var textViewSaveNewTemplate = item.findViewById(R.id.editTextTemplateName) as EditText
                    if (TextUtils.isEmpty(textViewSaveNewTemplate.text))
                    {
                        textViewSaveNewTemplate.requestFocus()
                        textViewSaveNewTemplate.error = "範本名稱不能為空白!"
                    }else {
                        createNewTemplate(textViewSaveNewTemplate.getText().toString(), alertDialog)
                    }

                }
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
        //mBtnList.forEach(){
        //    gridLayoutBtnList.addView(it);
       // }
        /*
        for(int i =0; i < mBtnList.size-1 ; i++) {
            button[i] = new Button (this);
            button[i].setText(String.valueOf(i + 1)); // 設定Button中的的字
            button[i].setMinWidth(with);      // 設定button寬度
            button[i].setMinHeight(with);     // 設定button高度
            button[i].setId(i);              // 設定Button的ID
            button[i].setOnClickListener(this);
            gridLayout.addView(button[i]); // 按照格子的順序依序放近GridLayout
            /*
            助解的部分世代表有另外一種設定模式直接指定在第幾行第幾列放入資料
            Spec rowSpec = GridLayout.spec(i /3 );
            Spec columnSpec = GridLayout.spec(i % 3);
            LayoutParams layoutParams = new LayoutParams( rowSpec, columnSpec );
            layoutParams.setGravity(Gravity.FILL);
            gridLayout.addView(button[i], layoutParams);
            */
        }
         */
    }

    private fun AddRecipeTypeItem(recipeType : String, allowmulti : Boolean) {
        var item_data_list :MutableList<String> = mutableListOf()
        var item_data_select_list :MutableList<Boolean> = mutableListOf()
        var recipeItemListData = RecipeItemListData(recipeType,item_data_list,item_data_select_list,allowmulti)

        mItemList.add(recipeItemListData)

        val adapter = RecipeItemAdapter(mItemList,false)

        //mRecyclerViewRecipeItems!!.setHasFixedSize(true)
        //mRecyclerViewRecipeItems!!.layoutManager = LinearLayoutManager(context)
        mRecyclerViewRecipeItems!!.adapter = adapter

        mRecyclerViewRecipeItems!!.adapter!!.notifyDataSetChanged()

    }

    private fun createNewMenuRecipe() {
        mFdUserMenu.menuRecipes!!.clear()
        var iIdx:Int =0

        mItemList.forEach(){

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
            if(recipe.recipeItems!!.size>0) {
                recipe.sequenceNumber = iIdx++
                mFdUserMenu.menuRecipes!!.add(recipe)
            }
        }
        var abc ="123"
        //userMenu.menuRecipes

        //Create

        /*
        mDatabase.child("USER_MENU_INFORMATION").child(mAuth.currentUser!!.uid).child(userMenuID).setValue(userMenu)
            .addOnSuccessListener {
                Toast.makeText(this, "建立菜單成功!", Toast.LENGTH_SHORT).show()
                // Write was successful!
                val bundle = Bundle()
                bundle.putString("Result", "OK")
                val intent = Intent().putExtras(bundle)
                setResult(Activity.RESULT_OK, intent)
                finish()

            }
            .addOnFailureListener {
                // Write failed
                Toast.makeText(this, "採購建立菜單失敗!", Toast.LENGTH_SHORT).show()
            }

         */
    }

    private fun createNewTemplate(templateName : String, dialog : AlertDialog) {
        var userRecipeTemplate : USER_CUSTOM_RECIPE_TEMPLATE = USER_CUSTOM_RECIPE_TEMPLATE()
        userRecipeTemplate.menuRecipes!!.clear()
        userRecipeTemplate.templateName = templateName
        var iIdx:Int =0

        mItemList.forEach(){

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
            if(recipe.recipeItems!!.size>0) {
                recipe.sequenceNumber = iIdx++
                userRecipeTemplate.menuRecipes!!.add(recipe)
            }
        }

        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

        }

        mDatabase.child("USER_CUSTOM_RECIPE_TEMPLATE").child(mAuth.currentUser!!.uid).child(templateName).setValue(userRecipeTemplate)
            .addOnSuccessListener {
                Toast.makeText(this, "存儲範本成功!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()

            }
            .addOnFailureListener {
                // Write failed
                Toast.makeText(this, "存儲範本失敗!", Toast.LENGTH_SHORT).show()
            }


    }
    private fun getRecipeTemplateList(item: View) {
        var userMenu: USER_MENU = com.iew.fun2order.db.firebase.USER_MENU()

        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

        }
        var menuPath = "MENU_RECIPE_TEMPLATE"
        val database = Firebase.database
        val myRef = database.getReference(menuPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val array = arrayListOf<String>()
                dataSnapshot.children.forEach()
                {

                    var test =  it.getValue(MENU_RECIPE_TEMPLATE::class.java)
                    array.add(test!!.templateName.toString())
                }

                var arr_aAdapter: ArrayAdapter<String>? = null
                arr_aAdapter = ArrayAdapter(item.getContext() , android.R.layout.simple_selectable_list_item, array)

                var listView = item.findViewById(R.id.listViewRecipeTemplateListItems) as ListView

                listView!!.setAdapter(arr_aAdapter)
                for (i in 0 until listView.getChildCount()) {
                    (listView.getChildAt(i) as TextView).setTextColor(Color.GREEN)
                }

                listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                    // Get the selected item text from ListView
                    val selectedItem = parent.getItemAtPosition(position) as String

                    getRecipeTemplate(view, selectedItem)

                    mDialog.dismiss()
                }
                //listView.setCacheColorHint(Color.rgb(36, 33, 32));
                /*
                val alertDialog =
                    AlertDialog.Builder(this@ActivityAddRecipe)
                alertDialog.setView(item)

                alertDialog.setNegativeButton("取消", null)
                //alertDialog.show()
                mDialog = alertDialog.show();

                 */
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

    }

    private fun getCustRecipeTemplateList(item: View) {
        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

        }
        var menuPath = "USER_CUSTOM_RECIPE_TEMPLATE/${mAuth.currentUser!!.uid.toString()}/"
        val database = Firebase.database
        val myRef = database.getReference(menuPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val array = arrayListOf<String>()
                dataSnapshot.children.forEach()
                {

                    var test =  it.getValue(USER_CUSTOM_RECIPE_TEMPLATE::class.java)
                    array.add(test!!.templateName.toString())
                }

                var arr_aAdapter: ArrayAdapter<String>? = null
                arr_aAdapter = ArrayAdapter(item.getContext() , android.R.layout.simple_selectable_list_item, array)

                var listView = item.findViewById(R.id.listViewRecipeTemplateListItems) as ListView

                listView!!.setAdapter(arr_aAdapter)
                for (i in 0 until listView.getChildCount()) {
                    (listView.getChildAt(i) as TextView).setTextColor(Color.GREEN)
                }

                listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                    // Get the selected item text from ListView
                    val selectedItem = parent.getItemAtPosition(position) as String

                    //getRecipeTemplate(view, selectedItem)
                    getCustRecipeTemplate(view, selectedItem)
                    mDialog.dismiss()
                }
                //listView.setCacheColorHint(Color.rgb(36, 33, 32));
                /*
                val alertDialog =
                    AlertDialog.Builder(this@ActivityAddRecipe)
                alertDialog.setView(item)

                alertDialog.setNegativeButton("取消", null)
                //alertDialog.show()
                mDialog = alertDialog.show();

                 */
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

    }

    private fun getRecipeTemplate(item: View, template_name: String) {

        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

        }
        var menuPath = "MENU_RECIPE_TEMPLATE/${template_name}"
        val database = Firebase.database
        val myRef = database.getReference(menuPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(MENU_RECIPE_TEMPLATE::class.java)
                //var abc = value
                //add recipe type
                mItemList.clear()
                mRecyclerViewRecipeItems!!.adapter!!.notifyDataSetChanged()

                value!!.menuRecipes!!.forEach {
                    var item_data_list :MutableList<String> = mutableListOf()
                    var item_data_select_list :MutableList<Boolean> = mutableListOf()
                    it.recipeItems!!.forEach {
                        item_data_list.add(it.recipeName.toString())
                        item_data_select_list.add(true)
                    }
                    var recipeItemListData = RecipeItemListData(it.recipeCategory,item_data_list, item_data_select_list,it.allowedMultiFlag)
                    mItemList.add(recipeItemListData)

                    mRecyclerViewRecipeItems!!.adapter!!.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

    }

    private fun getCustRecipeTemplate(item: View, template_name: String) {

        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

        }
        var menuPath = "USER_CUSTOM_RECIPE_TEMPLATE/${mAuth.currentUser!!.uid.toString()}/${template_name}"
        val database = Firebase.database
        val myRef = database.getReference(menuPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(USER_CUSTOM_RECIPE_TEMPLATE::class.java)
                //var abc = value
                //add recipe type
                mItemList.clear()
                mRecyclerViewRecipeItems!!.adapter!!.notifyDataSetChanged()

                value!!.menuRecipes!!.forEach {
                    var item_data_list :MutableList<String> = mutableListOf()
                    var item_data_select_list :MutableList<Boolean> = mutableListOf()
                    it.recipeItems!!.forEach {
                        item_data_list.add(it.recipeName.toString())
                        item_data_select_list.add(true)
                    }
                    var recipeItemListData = RecipeItemListData(it.recipeCategory,item_data_list, item_data_select_list,it.allowedMultiFlag)
                    mItemList.add(recipeItemListData)

                    mRecyclerViewRecipeItems!!.adapter!!.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

    }
}