package com.iew.fun2order.ui.home

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.BuildConfig
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.MenuTypeDAO
import com.iew.fun2order.db.dao.friendImageDAO
import com.iew.fun2order.db.dao.localImageDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityLocalmage
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.db.firebase.USER_PROFILE
import com.iew.fun2order.nativead.TemplateView
import com.iew.fun2order.ui.home.adapter.MenuItemAdapter
import com.iew.fun2order.ui.home.data.MenuItemListData
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_WAIT
import com.iew.fun2order.utility.NOTIFICATION_TYPE_SHARE_MENU
import com.tooltip.Tooltip
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.bottom_sheet_menu_operator.view.*
import org.json.JSONObject


class HomeFragment : Fragment(), IAdapterOnClick {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var mAuth: FirebaseAuth
    private val ACTION_ADD_MENU_REQUEST_CODE = 101
    private val ACTION_SHARE_MENU_REQUEST_CODE = 501
    private lateinit var imageAddMenu : ImageView
    private lateinit var DBContext : AppDatabase
    private lateinit var menuICONdao  :localImageDAO
    private lateinit var mMenuTypeDB: MenuTypeDAO
    private lateinit var mDBContext: AppDatabase

    private var mMenuType: String? = ""
    private var mRecyclerViewUserMenu: RecyclerView? = null
    private var mSegmentedGroupMenuType: SegmentedGroup? = null
    private var mItemList: MutableList<MenuItemListData> = mutableListOf()
    private var mMenuCount: Int = 0
    private var mInflater: LayoutInflater? = null
    private lateinit var  mDialog : androidx.appcompat.app.AlertDialog
    private var mUserProfile: USER_PROFILE? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this.context);

        DBContext = AppDatabase(requireContext())
        menuICONdao = DBContext.localImagedao()

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
        mDBContext = AppDatabase(requireContext())
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

        mSegmentedGroupMenuType = root.findViewById<View>(R.id.segmentedGroupMenuType) as SegmentedGroup
        mSegmentedGroupMenuType!!.removeAllViews()
        mSegmentedGroupMenuType!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            radioButton.setOnLongClickListener()
            {
                if(radioButton.text.toString() != "未分類") {
                    checkRemoveMenuCategory(radioButton.text.toString())
                }
                true
            }

            val menutype = radioButton.text.toString()
            mMenuType = menutype
            setUserMenu(menutype)
        })


        val adapter = MenuItemAdapter( requireContext(),mItemList,this)
        mRecyclerViewUserMenu = root.findViewById(R.id.recyclerViewMenuItems) as RecyclerView
        mRecyclerViewUserMenu!!.layoutManager = LinearLayoutManager(requireActivity())
        mRecyclerViewUserMenu!!.adapter = adapter

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

        val imageAboutInfo = root.findViewById(R.id.imageAboutInfo) as ImageView
        imageAboutInfo.setOnClickListener {
            // your code here

            val versionInfo = "Version: ${BuildConfig.VERSION_NAME} - Beta ${BuildConfig.VERSION_CODE}"
            val item = LayoutInflater.from(this.context).inflate(R.layout.alert_about_us, null)
            val version = item.findViewById<TextView>(R.id.textViewVersion)
            val welcome =  item.findViewById<TextView>(R.id.textViewWelcome)

            version.text = versionInfo
            welcome.text = "歡迎使用 ${requireContext().getString(R.string.app_name)}"
            AlertDialog.Builder(this.context)

                .setView(item)
                .setPositiveButton("OK", null)
                .show()
        }
        return root
    }



    fun recycleViewRefresh() {
        mRecyclerViewUserMenu!!.adapter!!.notifyDataSetChanged()
    }


    private fun addButton(inflater: LayoutInflater, group: SegmentedGroup, btnName: String)
    {
        val radioButton = inflater.inflate(R.layout.radio_button_item, null) as RadioButton
        radioButton.text = btnName
        group.addView(radioButton)
        group.updateBackground()
    }

    fun setUserMenu(menutype: String) {
        getUserMenuList(menutype)
    }


    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(messageReceiver, IntentFilter("UpdateMenuList"))

    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver)

    }

    private var messageReceiver = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            queryUserMenuFromFireBase()
        }
    }


    override fun onResume() {
        super.onResume()
        queryUserMenuFromFireBase()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.extras?.let {
            when (requestCode) {
                ACTION_ADD_MENU_REQUEST_CODE -> {
                    if (resultCode == Activity.RESULT_OK && data != null) {
                        if(mItemList.count() == 0) {
                            showToolTips_NEXT()
                        }
                        setUserMenu(mMenuType!!)
                    }
                }

                ACTION_SHARE_MENU_REQUEST_CODE ->
                {
                    if (resultCode == Activity.RESULT_OK && data != null) {

                        val memoryContext = MemoryDatabase(requireContext())
                        val friendImage      = memoryContext.friendImagedao()

                        val tmpMenu = data.extras?.get("USER_MENU")
                        val addMembersList = data.extras?.get("AddMembers") as ArrayList<*> ?: null
                        if(tmpMenu != null)
                        {
                            val Menu = tmpMenu as USER_MENU
                            addMembersList!!.forEach()
                            {
                                var userProfile = friendImage.getFriendImageByName(it.toString())
                                var tokenID = userProfile.tokenID
                                sendShareMenuInfoToFCM(tokenID, Menu)
                            }
                        }
                    }

                }
                else -> {
                    println("no handler onActivityReenter")
                }
            }
        }
    }

    private fun getUserMenuList(menutype:String) {

        var menuType =""
        if(menutype.equals("未分類")){
            menuType=""
        }else{
            menuType = menutype
        }

        var mAuth = FirebaseAuth.getInstance()
        var menuPath = "USER_MENU_INFORMATION/${mAuth.currentUser!!.uid.toString()}/"
        val database = Firebase.database
        val myRef = database.getReference(menuPath)

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                mItemList.clear()
                mMenuCount =dataSnapshot.children.count()
                dataSnapshot.children.forEach()
                {

                   var test =  it.getValue(USER_MENU::class.java)
                    //if(menuType!=""){
                        if(test!!.brandCategory.equals(menuType)) {
                            var imagePath = ""
                            if (test!!.multiMenuImageURL!!.size > 0) {
                                imagePath = test!!.multiMenuImageURL!!.get(0)
                            }

                            var IconBitmap :Bitmap? = null
                            if(imagePath != "")
                            {
                                val menuICON = menuICONdao.getMenuImageByName(imagePath)
                                if(menuICON != null)
                                {
                                    IconBitmap = BitmapFactory.decodeByteArray( menuICON.image, 0,  menuICON.image!!.size)
                                }
                                else
                                {
                                    var islandRef = Firebase.storage.reference.child(imagePath)
                                    val ONE_MEGABYTE = 1024 * 1024.toLong()
                                    islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytesPrm: ByteArray ->
                                        menuICONdao.insertRow(entityLocalmage(null, imagePath, "", bytesPrm.clone()!!))
                                    }
                                }
                            }



                            synchronized(this) {
                                mItemList.add(
                                    MenuItemListData(
                                        test!!.brandName,
                                        test!!.menuDescription,
                                        IconBitmap,
                                        imagePath,
                                        test,
                                        mUserProfile)
                                    )
                            }
                        }
                    //}else{
                    //    mItemList.add(MenuItemListData(test!!.brandName, test!!.menuDescription, BitmapFactory.decodeResource(getResources(),R.drawable.image_default_member), test!!.menuImageURL, test))
                    //}

                }
                recycleViewRefresh()
                if(mItemList.count() == 0) {
                   showToolTips_CreateMenu()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

    }

    private fun showToolTips_CreateMenu()
    {
        if(mMenuCount ==0 ) {
            val sharedPreferences : SharedPreferences = requireContext().getSharedPreferences("share", AppCompatActivity.MODE_PRIVATE);
            val editor= sharedPreferences.edit();
            val tooltipCreateMenu: Boolean = sharedPreferences.getBoolean("tooltipCreateMenu", true);
            if( tooltipCreateMenu ) {

                val tooltip: Tooltip = Tooltip.Builder(imageAddMenu)
                    .setText("歡迎你使用Fun2Order\n重這裡開始\n製作你第一張菜單")
                    .setDismissOnClick(true)
                    .setCancelable(true)
                    .setCornerRadius(20f)
                    .setBackgroundColor(resources.getColor(R.color.blue))
                    .setTextColor(resources.getColor(R.color.white))
                    .setOnDismissListener {
                        editor.putBoolean("tooltipCreateMenu", false);
                        editor.apply();
                    }
                    .show()
            }
        }
    }


    private fun showToolTips_NEXT()
    {
        val sharedPreferences : SharedPreferences = requireContext().getSharedPreferences("share", AppCompatActivity.MODE_PRIVATE);
        val editor= sharedPreferences.edit();
        val tooltipAfterCreateMenu: Boolean = sharedPreferences.getBoolean("tooltipAfterCreateMenu", true);
        if(tooltipAfterCreateMenu ) {

            val notifyAlert = androidx.appcompat.app.AlertDialog.Builder(requireContext()).create()
            notifyAlert.setMessage("恭喜你設定完成第一張訂單!!\n接下來移至我的設定\n完成新增好友與群組資訊")
            notifyAlert.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "確定")
            { _, i ->
                editor.putBoolean("tooltipAfterCreateMenu", false);
                editor.apply();
            }
            notifyAlert.show()
        }
    }



    private fun queryUserMenuFromFireBase()
    {
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




    private fun deleteUserMenuFromFireBase(menutype: String)
    {
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val queryPath = "USER_PROFILE/$uuid"
        val myRef = Firebase.database.getReference(queryPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userProfile = dataSnapshot.getValue(USER_PROFILE::class.java)
                if (userProfile != null) {
                    userProfile.brandCategoryList!!.remove(menutype)
                    mMenuType=""
                    mUserProfile = userProfile.copy()
                    dataSnapshot.ref.setValue(userProfile)
                    createMenuTypeButton(mUserProfile!!)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context!!, "清除分類失敗", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resetUserMenuFromFireBase(menutype: String)
    {
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        var queryPath = "USER_MENU_INFORMATION/${uuid}/"
        val myRef = Firebase.database.getReference(queryPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach()
                {
                    var test = it.getValue(USER_MENU::class.java)
                    if(test!!.brandCategory.equals(menutype)){
                        test!!.brandCategory=""
                        it.ref.setValue(test)
                    }
                }
                deleteUserMenuFromFireBase(menutype)
            }
            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(context!!, "清除分類失敗", Toast.LENGTH_SHORT).show()
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
        //addButton(mInflater!!, mSegmentedGroupMenuType!!, "編輯")

        val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )

        if(mSegmentedGroupMenuType!!.childCount < 4) {
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
                    }
                }
            }
        }
    }

    override fun onClick(sender: String, pos: Int, type: Int) {
        if (type == 0) {
            val bundle = Bundle()
            bundle.putString("EDIT", "Y")
            bundle.putString("MENU_ID", mItemList[pos].getItemName())
            bundle.putParcelable("USER_MENU", mItemList[pos].getUserMenu())
            bundle.putParcelable("USER_PROFILE", mItemList[pos].getUserProfile())
            var intent = Intent(context, ActivityAddMenu::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        else if(type == 1)
        {

            val userUUID = FirebaseAuth.getInstance().currentUser!!.uid.toString()
            val menuInformation = mItemList[pos].getUserMenu()!!
            val dialog = BottomSheetDialog(requireContext())
            val bottomSheet = layoutInflater.inflate(R.layout.bottom_sheet_menu_operator, null)
            bottomSheet.buttonShare.setOnClickListener { ShareMenuToFriend(menuInformation);dialog.dismiss() }
            bottomSheet.buttonDelete.setOnClickListener {checkRemoveMenu( userUUID, menuInformation, pos); dialog.dismiss() }
            bottomSheet.buttonSubmit.setOnClickListener { dialog.dismiss() }
            dialog.setContentView(bottomSheet)
            dialog.show()


            /*
            val buttonActions = arrayOf("刪除菜單", "將菜單分享給好友")
            val userUUID = FirebaseAuth.getInstance().currentUser!!.uid.toString()
            val menuInformation = mItemList[pos].getUserMenu()!!
            BottomSheetDialog(requireContext())
                .setTitle("請選擇操作項目")
                .setItems(buttonActions,  DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        0 -> { checkRemoveMenu( userUUID, menuInformation, pos) }
                        // 分享菜單功能 v1.1
                        1 -> {  ShareMenuToFriend(menuInformation)}
                    }
                })
                .setNegativeButton("關閉", null)
                .create()
                .show()

             */

        }
    }

    private fun checkRemoveMenu(userUUID: String, MenuInfo: USER_MENU, Position:Int) {
        val alert = AlertDialog.Builder(context)
        with(alert) {
            setTitle("確認刪除菜單")
            setCancelable(false)
            setMessage("菜單名稱:${MenuInfo.brandName}")
            setPositiveButton("確定") { dialog, _ ->
                try {
                    // 刪除菜單之前先把影像砍掉
                    if (userUUID != "" && MenuInfo.menuNumber != "") {
                        //----------順便砍掉LocalDB 資料------------
                        var Imagepath = "Menu_Image/" + userUUID + "/" + MenuInfo.menuNumber
                        var DBContext = AppDatabase(requireContext())
                        var menuICONdao = DBContext.localImagedao()
                        menuICONdao.deleteICONImage(Imagepath)

                        //--------------------------------
                        var menuPath = "USER_MENU_INFORMATION/${userUUID}/${MenuInfo.menuNumber}"
                        val database = Firebase.database
                        database.getReference(menuPath).removeValue()

                        //--------------------------------
                        var imageFolder = "Menu_Image/${userUUID}/${MenuInfo.menuNumber}"
                        val listRef = Firebase.storage.reference.child(imageFolder!!)
                        listRef.listAll()
                            .addOnSuccessListener { listResult ->
                                listResult.prefixes.forEach { prefix ->
                                }

                                listResult.items.forEach { item ->
                                    item.delete()
                                }
                            }
                            .addOnFailureListener {
                                // Uh-oh, an error occurred!
                            }
                    }
                    mItemList.removeAt(Position)
                    recycleViewRefresh()
                } catch (e: Exception) {
                }
                dialog.dismiss()
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = alert.create()
        dialog.show()
    }


    private fun checkRemoveMenuCategory(MenuCategory:String) {
        val alert = AlertDialog.Builder(context)
        with(alert) {
            setTitle("確認刪除菜單分類:")
            setMessage(MenuCategory)
            setPositiveButton("確定") { dialog, _ ->
                resetUserMenuFromFireBase(MenuCategory)
                dialog.dismiss()
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = alert.create()
        dialog.show()
    }


    private fun ShareMenuToFriend( MenuInfo: USER_MENU) {
        val memoryContext = MemoryDatabase(requireContext())
        val friendDB      = memoryContext.frienddao()
        val friendList = friendDB.getFriendslist()
        if (friendList.count() > 0) {
            val array =  ArrayList(friendList)
            val bundle = Bundle()
            bundle.putStringArrayList("Candidate", array)
            bundle.putParcelable("USER_MENU",MenuInfo)
            val intent = Intent(context, ActivityAddShareMeunMember::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, ACTION_SHARE_MENU_REQUEST_CODE)
        } else {
            Toast.makeText(activity, "沒有好友可以分享", Toast.LENGTH_SHORT).show()
        }

    }


    private fun sendShareMenuInfoToFCM(tokenID:String, Menu:USER_MENU)
    {
        val dbContext: MemoryDatabase = MemoryDatabase(requireContext())
        val friendImageDB: friendImageDAO = dbContext.friendImagedao()

        val topic = tokenID
        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()
        var body = "有來自 [ ${Menu.userName} ] 分享的菜單資訊, 請問妳願意接受嗎？。"
        notificationHeader.put("title", "菜單分享")
        notificationHeader.put("body", body ?: "")   //Enter your notification message
        notificationBody.put("messageID", "")      //Enter
        notificationBody.put("messageTitle", "菜單分享")   //Enter
        notificationBody.put("messageBody", body ?: "")    //Enter
        notificationBody.put("notificationType", NOTIFICATION_TYPE_SHARE_MENU )   //Enter
        notificationBody.put("receiveTime", "")   //Enter
        notificationBody.put("orderOwnerID", Menu.userID)   //Enter
        notificationBody.put("orderOwnerName", Menu.userName)   //Enter
        notificationBody.put("menuNumber", Menu.menuNumber)   //Enter
        notificationBody.put("orderNumber", "")   //Enter
        notificationBody.put("dueTime",    "")   //Enter  20200515 addition
        notificationBody.put("brandName","")   //Enter
        notificationBody.put("attendedMemberCount", "0")   //Enter
        notificationBody.put("messageDetail", Menu.userID)   //Enter
        notificationBody.put("isRead", "N")   //Enter
        notificationBody.put("replyStatus", MENU_ORDER_REPLY_STATUS_WAIT)   //Enter
        notificationBody.put("replyTime", "")   //Enter

        // your notification message
        notification.put("to", topic)

        val getDate = friendImageDB.getFriendImageByTokenID(tokenID)
        if(getDate!= null)
        {
            if(getDate.OSType?:"" == "iOS")
            {
                notification.put("notification", notificationHeader)
            }
        }

        notification.put("data", notificationBody)

        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotification(notification)

    }
}