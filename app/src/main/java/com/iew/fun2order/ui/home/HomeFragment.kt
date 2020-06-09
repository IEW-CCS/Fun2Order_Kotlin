package com.iew.fun2order.ui.home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.BuildConfig
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.MenuTypeDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.db.firebase.USER_PROFILE
import com.iew.fun2order.nativead.TemplateView
import com.iew.fun2order.ui.home.adapter.MenuItemAdapter
import com.iew.fun2order.ui.home.data.MenuItemListData
import com.iew.fun2order.utility.LOCALBROADCASE_MESSAGE
import com.tooltip.Tooltip
import info.hoang8f.android.segmented.SegmentedGroup


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var mAuth: FirebaseAuth
    private val ACTION_ADD_MENU_REQUEST_CODE = 101
    private val ACTION_EDIT_MENU_REQUEST_CODE = 102
    private lateinit var imageAddMenu : ImageView


    private lateinit var mMenuTypeDB: MenuTypeDAO
    private lateinit var mDBContext: AppDatabase

    var mMenuType: String? = ""
    var mRecyclerViewUserMenu: RecyclerView? = null
    var mSegmentedGroupMenuType: SegmentedGroup? = null
    var mItemList: MutableList<MenuItemListData> = mutableListOf()
    var mInflater: LayoutInflater? = null
    private lateinit var  mDialog : androidx.appcompat.app.AlertDialog
    //var mFdUserMenus: Map<String, USER_MENU2> = mapOf()
    var mUserProfile: USER_PROFILE? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this.context);
    }

    fun showMenuTypeDiago(userProfile: USER_PROFILE) {
        val item = LayoutInflater.from(requireContext()).inflate(R.layout.alert_edit_menu_type, null)
        /*
       * Consume the events here so the buttons cannot process them
       * if the CheckBox in the UI is checked
       */
        /*
        var menuTypelist = mMenuTypeDB.getMenuTypeslist()
        val array = arrayListOf<String>()
        if (menuTypelist.count() > 0) {

            menuTypelist.forEach()
            {
                array.add(it.toString())
            }
        }
        var values = arrayOf(
            "台灣應材",
            "默克",
            "奇美材料"
        )

         */
        val array = arrayListOf<String>()
        if(userProfile!=null){
            userProfile.brandCategoryList!!.forEach()
            {
                array.add(it.toString())
            }
        }

        var arr_aAdapter: ArrayAdapter<String>? = null

        arr_aAdapter = ArrayAdapter(context, android.R.layout.simple_selectable_list_item, array)

        var listView = item.findViewById(R.id.listViewMenuTypeListItems) as ListView

        listView!!.setAdapter(arr_aAdapter)
        for (i in 0 until listView.getChildCount()) {
            (listView.getChildAt(i) as TextView).setTextColor(Color.GREEN)
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            // Get the selected item text from ListView
            val selectedItem = parent.getItemAtPosition(position) as String
            var editTextMenuType = item.findViewById(R.id.editTextMenuType) as EditText
            editTextMenuType.setText(selectedItem)
        }

        var btnDeleteMenuType = item.findViewById(R.id.btnDeleteMenuType) as Button
        btnDeleteMenuType.setOnClickListener {
            var editTextMenuType = item.findViewById(R.id.editTextMenuType) as EditText

            if (TextUtils.isEmpty(editTextMenuType.text))
            {
                editTextMenuType.requestFocus()
                editTextMenuType.error = "類別不能為空白!"
            }else {

                // Insert DB
                //val menutype: MenuType = MenuType(null, editTextMenuType.getText().toString())

                /*
                var deleteMenuType = mMenuTypeDB.getMenuTypeByName(menutype.menu_type)
                if(deleteMenuType.size>0){
                    mMenuTypeDB.delete(deleteMenuType.get(0))
                    MenuTypeListRefresh(item)
                    editTextMenuType.setText("")
                }else{
                    editTextMenuType.requestFocus()
                    editTextMenuType.error = "找不到類別!"
                }
                 */

                if(mUserProfile!!.brandCategoryList!!.size>0){
                    deleteUserMenuFromFireBase(editTextMenuType.getText().toString(),item)

                }else{
                    editTextMenuType.requestFocus()
                    editTextMenuType.error = "找不到類別!"
                }
            }
        }
        var btnAddMenuType = item.findViewById(R.id.btnAddMenuType) as Button
        btnAddMenuType.setOnClickListener {
            var editTextMenuType = item.findViewById(R.id.editTextMenuType) as EditText


            if (TextUtils.isEmpty(editTextMenuType.text))
            {
                editTextMenuType.requestFocus()
                editTextMenuType.error = "類別不能為空白!"
            }else {
                var bFound:Boolean = false

                mUserProfile!!.brandCategoryList!!.forEach {
                    if(editTextMenuType.getText().toString().equals(it)){
                        bFound = true
                    }
                }
                if(bFound){
                    editTextMenuType.requestFocus()
                    editTextMenuType.error = "重覆類別!"

                }else{
                    addUserMenuFromFireBase(editTextMenuType.getText().toString(),item)
                    //MenuTypeListRefresh(item)
                    //editTextMenuType.setText("")
                }


                // Insert DB
                /*
                val menutype: MenuType = MenuType(null, editTextMenuType.getText().toString())

                var addMenuType = mMenuTypeDB.getMenuTypeByName(menutype.menu_type)
                if(addMenuType.size == 0){
                    mMenuTypeDB.insertRow(menutype)
                    MenuTypeListRefresh(item)
                    editTextMenuType.setText("")
                }else{
                    editTextMenuType.requestFocus()
                    editTextMenuType.error = "重覆類別!"
                }

                 */


            }
        }

        var alertDialog = androidx.appcompat.app.AlertDialog.Builder(context!!)
            .setView(item)
            //.setPositiveButton("加入菜單分類", null)
            .setNegativeButton("關閉", null)

        mDialog = alertDialog.show();

        mDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
            .setOnClickListener {
                mMenuType=""
                mDialog.dismiss()
                createMenuTypeButton(mUserProfile!!)
            }

        mDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {
                /*
                var editTextMenuType = item.findViewById(R.id.editTextMenuType) as EditText

                if (TextUtils.isEmpty(editTextMenuType.text))
                {
                    editTextMenuType.requestFocus()
                    editTextMenuType.error = "類別不能為空白!"
                }else {

                    // Insert DB
                    val menutype: MenuType = MenuType(null, editTextMenuType.getText().toString())

                    mMenuTypeDB.insertRow(menutype)
                    mDialog.dismiss()
                }

                 */
mMenuType=""
                mDialog.dismiss()
                createMenuTypeButton(mUserProfile!!)
            }

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home_main, container, false)
        mInflater = inflater
        mDBContext = AppDatabase(context!!)
        //mUserMenuDB = mDBContext.usermenudao()
        mMenuTypeDB = mDBContext.menutyoedao()


        val template: TemplateView = root.findViewById(R.id.my_template)
        val adLoader: AdLoader = AdLoader.Builder(this.context, requireContext().getString(R.string.native_ad_unit_id))
            .forUnifiedNativeAd(object : UnifiedNativeAd.OnUnifiedNativeAdLoadedListener {
                override fun onUnifiedNativeAdLoaded(unifiedNativeAd: UnifiedNativeAd?) {
                    if (unifiedNativeAd != null) {
                        template.setNativeAd(unifiedNativeAd)
                    }
                }
            })
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    template.visibility = View.GONE
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())


        //
        mSegmentedGroupMenuType =
            root.findViewById<View>(R.id.segmentedGroupMenuType) as SegmentedGroup

        mSegmentedGroupMenuType!!.removeAllViews()

        //mSegmentedGroupMenuType = SegmentedGroup(this.context);
        mSegmentedGroupMenuType!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            //var value = radioButton.text
            //val index: Int = group.indexOfChild(radioButton)

            //val radioButton = group.getChildAt(checkedId - 1) as RadioButton
            val menutype = radioButton.text.toString()
            mMenuType = menutype
            if(mMenuType.equals("編輯")){
                showMenuTypeDiago(mUserProfile!!)
            }else{
                setUserMenu(menutype)
            }


        })


        /*
        mSegmentedGroupMenuType.onTouchEvent(){

        }
        mSegmentedGroupMenuType!!.setOnLongClickListener {

            fun onLongClick(View v):booba {
                Toast.makeText(getContext(), "Long Clicked", Toast.LENGTH_SHORT).show();

                true;
            }


            true
        }

         */

        mRecyclerViewUserMenu = root.findViewById(R.id.recyclerViewMenuItems) as RecyclerView
        //activity!!.findViewById<View>(R.id.recyclerViewMenuItems) as RecyclerView
        val adapter = MenuItemAdapter(mItemList)

        mRecyclerViewUserMenu!!.setHasFixedSize(true)
        mRecyclerViewUserMenu!!.layoutManager = LinearLayoutManager(context)
        mRecyclerViewUserMenu!!.adapter = adapter


/*
        mUserMenuDB.getAllMenu().observe(this, Observer {
            var list = it as java.util.ArrayList<UserMenu>
            mItemList.clear()
            list.forEach() {
                val groupbmp = BitmapFactory.decodeByteArray(it.image, 0, it.image.size)
                mItemList.add(MenuItemListData(it.menu_id, it.menu_desc, groupbmp))
            }
            //---------------------------------------

            RecycleViewRefresh()
        })


 */
        //Create MenuType
        //queryUserMenuFromFireBase()
/*
        mMenuTypeDB.getAllMenuType().observe(this, Observer {
            var list = it as java.util.ArrayList<MenuType>
            var menuType : MenuType = MenuType((list.size+1).toLong(),"未分類")
            list.add(menuType)
            var menuType2 : MenuType = MenuType((list.size+1).toLong(),"編輯")
            list.add(menuType2)
            list.forEach() {
                addButton(mInflater!!, mSegmentedGroupMenuType!!, it.menu_type)
            }

            //addButton(mInflater!!, mSegmentedGroupMenuType!!, "未分類")

            if (mSegmentedGroupMenuType!!.childCount > 0) {
                val default = mSegmentedGroupMenuType!!.getChildAt(0) as RadioButton
                default.isChecked = true

                val menutype = default.text.toString()
                mMenuType = menutype
                setUserMenu(menutype)
            }
        })


 */


        // get reference to ImageView
        imageAddMenu = root.findViewById(R.id.imageAddMenu) as ImageView

        // set on-click listener for ImageView
        imageAddMenu.setOnClickListener {
            // your code here
            val bundle = Bundle()
            bundle.putString("EDIT", "N")
            bundle.putString("MENU_ID", "")
            bundle.putParcelable("USER_PROFILE", mUserProfile)
            var I = Intent(context, ActivityAddMenu::class.java)

            I.putExtras(bundle)
            startActivityForResult(I, ACTION_ADD_MENU_REQUEST_CODE)
        }

        // get reference to ImageView
        val imageAboutInfo = root.findViewById(R.id.imageAboutInfo) as ImageView
        // set on-click listener for ImageView
        imageAboutInfo.setOnClickListener {
            // your code here

            val VersionInfo = "Version: ${BuildConfig.VERSION_CODE}.${BuildConfig.VERSION_NAME}"
            val item = LayoutInflater.from(this.context).inflate(R.layout.alert_about_us, null)
            val version = item.findViewById<TextView>(R.id.textViewVersion)
            version.text = VersionInfo
            AlertDialog.Builder(this.context)

                .setView(item)
                .setPositiveButton("OK", null)
                .show()
        }

        return root
    }

    fun RecycleViewRefresh() {

        mRecyclerViewUserMenu!!.adapter!!.notifyDataSetChanged()

    }

    fun MenuTypeListRefresh(item:View) {

        /*
        var menuTypelist = mMenuTypeDB.getMenuTypeslist()
        val array = arrayListOf<String>()
        if (menuTypelist.count() > 0) {

            menuTypelist.forEach()
            {
                array.add(it.toString())
            }
        }
        var values = arrayOf(
            "台灣應材",
            "默克",
            "奇美材料"
        )
         */
        var editTextMenuType = item.findViewById(R.id.editTextMenuType) as EditText
        editTextMenuType.setText("")

        val array = arrayListOf<String>()
        mUserProfile!!.brandCategoryList!!.forEach {
            array.add(it)
        }

        var arr_aAdapter: ArrayAdapter<String>? = null

        arr_aAdapter = ArrayAdapter(context, android.R.layout.simple_selectable_list_item, array)

        var listView = item.findViewById(R.id.listViewMenuTypeListItems) as ListView

        listView!!.setAdapter(arr_aAdapter)
        for (i in 0 until listView.getChildCount()) {
            (listView.getChildAt(i) as TextView).setTextColor(Color.GREEN)
        }

    }
    private fun addButton(
        inflater: LayoutInflater,
        group: SegmentedGroup,
        btnName: String
    ) {
        val radioButton =
            inflater.inflate(R.layout.radio_button_item, null) as RadioButton
        radioButton.text = btnName
        group.addView(radioButton)
        group.updateBackground()
    }

    fun setUserMenu(menutype: String) {
        getUserMenuList(menutype)
        /*
        mUserMenuDB.getMenusByType(menutype).observe(this, Observer {
            var list = it as java.util.ArrayList<UserMenu>
            mItemList.clear()
            list.forEach() {
                val groupbmp = BitmapFactory.decodeByteArray(it.image, 0, it.image.size)
                mItemList.add(MenuItemListData(it.menu_id, it.menu_desc, null, null)) //vic wait...
            }

            RecycleViewRefresh()
        })

         */
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("收到 result code $requestCode")

        data?.extras?.let {
            when (requestCode) {
                ACTION_ADD_MENU_REQUEST_CODE -> {
                    println( "onActivityReenter")
                    if (resultCode == Activity.RESULT_OK && data != null) {

                        if(mItemList.count() == 0) {
                            showToolTips_NEXT()
                        }
                        setUserMenu(mMenuType!!)



                    }
                }


                else -> {
                    println("no handler onActivityReenter")
                }
            }
        }

    }

    private fun getUserMenuList(menutype:String) {
        var userMenu: USER_MENU = com.iew.fun2order.db.firebase.USER_MENU()
        /*
        var brandCategory: String? = "",
        var brandName: String? = "",
        var createTime: String? = "",
        var locations: MutableList<LOCATION> = mutableListOf(),
        var menuDescription: String? = "",
        var menuImageURL: String? = "",
        var menuItems: MutableList<PRODUCT> = mutableListOf(),
        var menuNumber: String? = "",
        var menuRecipes: MutableList<RECIPE> = mutableListOf(),
        var userID: String? = "",
        var userName: String? = ""
         */
        var menuType =""
        if(menutype.equals("未分類")){
            menuType=""
        }else{
            menuType = menutype
        }

        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

        }
        var menuPath = "USER_MENU_INFORMATION/${mAuth.currentUser!!.uid.toString()}/"
        val database = Firebase.database
        val myRef = database.getReference(menuPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                mItemList.clear()
                dataSnapshot.children.forEach()
                {

                   var test =  it.getValue(USER_MENU::class.java)
                    //if(menuType!=""){
                        if(test!!.brandCategory==menuType){

                            mItemList.add(MenuItemListData(test!!.brandName, test!!.menuDescription, BitmapFactory.decodeResource(getResources(),R.drawable.image_default_member), test!!.menuImageURL ,test, mUserProfile))
                        }
                    //}else{
                    //    mItemList.add(MenuItemListData(test!!.brandName, test!!.menuDescription, BitmapFactory.decodeResource(getResources(),R.drawable.image_default_member), test!!.menuImageURL, test))
                    //}

                }

                RecycleViewRefresh()
                showToolTips_CreateMenu()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

    }

    private fun showToolTips_CreateMenu()
    {
        if(mItemList.count() ==0) {
            val tooltip: Tooltip = Tooltip.Builder(imageAddMenu)
                .setText("歡迎你使用Fun2Order\n重這裡開始\n製作你第一張菜單")
                .setDismissOnClick(true)
                .setCancelable(true)
                .setCornerRadius(20f)
                .setBackgroundColor(resources.getColor(R.color.blue))
                .setTextColor(resources.getColor(R.color.white))
                .show()
        }
    }


    private fun showToolTips_NEXT()
    {
        val notifyAlert = androidx.appcompat.app.AlertDialog.Builder(requireContext()).create()
        notifyAlert.setMessage("恭喜你設定完成第一張訂單!!\n接下來移至我的設定\n完成新增好友與群組資訊")
        notifyAlert.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "確定") { _, i -> }
        notifyAlert.show()
    }

    override fun onPause() {
        super.onPause()
        //queryUserMenuFromFireBase()


    }
    override fun onResume() {
        super.onResume()

        queryUserMenuFromFireBase()
        /*
        mMenuTypeDB.getAllMenuType().observe(this, Observer {
            mSegmentedGroupMenuType!!.removeAllViews()
            var list = it as java.util.ArrayList<MenuType>
            var menuType : MenuType = MenuType((list.size+1).toLong(),"未分類")
            list.add(menuType)
            var menuType2 : MenuType = MenuType((list.size+1).toLong(),"編輯")
            list.add(menuType2)
            list.forEach() {
                addButton(mInflater!!, mSegmentedGroupMenuType!!, it.menu_type)
            }

            if (mSegmentedGroupMenuType!!.childCount > 0) {

                mSegmentedGroupMenuType!!.children.forEach {
                    val default = it as RadioButton
                    if(default.text.toString() == mMenuType){
                        default.isChecked = true
                        //break;
                    }
                }
                //val default = mSegmentedGroupMenuType!!.getChildAt(0) as RadioButton
                //default.isChecked = true

                //val menutype = default.text.toString()
                //mMenuType = menutype
                //setUserMenu(menutype)
            }
            setUserMenu(mMenuType!!)
        })

         */

    }

    private fun queryUserMenuFromFireBase()
    {
        val context = this
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val queryPath = "USER_PROFILE/$uuid"
        val myRef = Firebase.database.getReference(queryPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userProfile = dataSnapshot.getValue(USER_PROFILE::class.java)
                if (userProfile != null) {
                    mUserProfile = userProfile
                    createMenuTypeButton(userProfile)
                }

            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun addUserMenuFromFireBase(menutype: String, item: View)
    {
        val context = this
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val queryPath = "USER_PROFILE/$uuid"
        val myRef = Firebase.database.getReference(queryPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userProfile = dataSnapshot.getValue(USER_PROFILE::class.java)
                if (userProfile != null) {
                    mUserProfile = userProfile
                    userProfile.brandCategoryList!!.add(menutype)
                    //createMenuTypeButton(userProfile)
                    dataSnapshot.ref.setValue(userProfile)
                    MenuTypeListRefresh(item)

                }

            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun deleteUserMenuFromFireBase(menutype: String, item: View)
    {
        val context = this
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val queryPath = "USER_PROFILE/$uuid"
        val myRef = Firebase.database.getReference(queryPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userProfile = dataSnapshot.getValue(USER_PROFILE::class.java)
                if (userProfile != null) {
                    mUserProfile = userProfile
                    userProfile.brandCategoryList!!.remove(menutype)
                    //createMenuTypeButton(userProfile)
                    dataSnapshot.ref.setValue(userProfile)
                    MenuTypeListRefresh(item)
                    //editTextMenuType.setText("")
                }

            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun createMenuTypeButton(userProfile:USER_PROFILE)
    {
        mSegmentedGroupMenuType!!.removeAllViews()

        userProfile.brandCategoryList!!.forEach {
            addButton(mInflater!!, mSegmentedGroupMenuType!!, it)
        }
        addButton(mInflater!!, mSegmentedGroupMenuType!!, "未分類")
        addButton(mInflater!!, mSegmentedGroupMenuType!!, "編輯")

        val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )

        if(mSegmentedGroupMenuType!!.childCount < 5) {
            params.gravity = Gravity.CENTER
            mSegmentedGroupMenuType!!.layoutParams = params
        }else{
            params.gravity = Gravity.LEFT
            mSegmentedGroupMenuType!!.layoutParams = params
        }

        if (mSegmentedGroupMenuType!!.childCount > 0) {
            if(mMenuType.equals("")){
                val default = mSegmentedGroupMenuType!!.getChildAt(0) as RadioButton
                default.isChecked = true
                mMenuType = default.text.toString()
            }else {
                mSegmentedGroupMenuType!!.children.forEach {
                    val default = it as RadioButton

                    if (default.text.toString() == mMenuType) {
                        default.isChecked = true
                        //break;
                    }
                }
            }
            //val default = mSegmentedGroupMenuType!!.getChildAt(0) as RadioButton
            //default.isChecked = true

            //val menutype = default.text.toString()
            //mMenuType = menutype
            //setUserMenu(mMenuType!!)
        }

    }
}