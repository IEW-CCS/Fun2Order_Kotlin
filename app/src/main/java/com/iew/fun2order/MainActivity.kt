package com.iew.fun2order


import android.app.Activity
import android.content.*
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.iew.fun2order.db.dao.friendDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.*
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.db.firebase.USER_PROFILE
import com.iew.fun2order.order.JoinOrderActivity
import com.iew.fun2order.utility.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    //region 導覽行列使用
    private lateinit var navView: BottomNavigationView
    //endregion

    //region LocalBroadcastManager
    //--- Nornal Notify 跳出訊息
    private var messageReceiver = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val text = p1?.getStringExtra("fcmMessage")
            if(text!= null) {
                val notifyAlert = AlertDialog.Builder(this@MainActivity).create()
                notifyAlert.setTitle("訊息通知")
                notifyAlert.setMessage(text)
                notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
                }
                notifyAlert.show()
            }
            UpdateBadge()
        }
    }

    //--- 自己參加的訂單跳轉訂閱畫面
    private var selfOrderJoinReceiver = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val orderInfo = p1?.getParcelableExtra<entityNotification>("joinOrderMessage")
            if(orderInfo != null)
            {
                val bundle = Bundle()
                bundle.putParcelable("InviteOrderInfo", orderInfo.copy())
                val I = Intent(p0, JoinOrderActivity::class.java)
                I.putExtras(bundle)
                startActivity(I)
            }
        }
    }

    //--- 收到互加好友資訊
    private var addFriendReceiver = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val orderInfo = p1?.getParcelableExtra<entityNotification>("AddFriendMessage")
            if(orderInfo != null)
            {
                if(FirebaseAuth.getInstance().currentUser != null) {
                    receiveAddFriendRequest(
                        orderInfo.messageTitle,
                        orderInfo.messageBody,
                        orderInfo.orderOwnerID
                    )
                }
            }
        }
    }

    //--- 收到分享菜單資訊
    private var shareMenuReceiver = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {

            // V1.1. Share Menu Function
            val shareMenuInfo = p1?.getParcelableExtra<entityNotification>("ShareMenuMessage")
            if(shareMenuInfo != null)
            {

                if(FirebaseAuth.getInstance().currentUser != null) {
                    receiveShareMenuRequest(
                        shareMenuInfo.messageTitle,
                        shareMenuInfo.messageBody,
                        shareMenuInfo.orderOwnerID,
                        shareMenuInfo.menuNumber
                    )
                }
            }
        }
    }

    //endregion


    init {
        instance = this
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, IntentFilter(LOCALBROADCASE_MESSAGE))
        LocalBroadcastManager.getInstance(this).registerReceiver(selfOrderJoinReceiver, IntentFilter(LOCALBROADCASE_JOIN))
        LocalBroadcastManager.getInstance(this).registerReceiver(addFriendReceiver, IntentFilter(LOCALBROADCASE_FRIEND))
        LocalBroadcastManager.getInstance(this).registerReceiver(shareMenuReceiver, IntentFilter(LOCALBROADCASE_SHAREMENU))
        UpdateBadge()
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(selfOrderJoinReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(addFriendReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(shareMenuReceiver)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {

        return super.onCreateView(name, context, attrs)


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val context: Context = applicationContext()
        navView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_notifications, R.id.navigation_history, R.id.mobile_navigation
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //----- Default Budge ------
        navView.getOrCreateBadge(R.id.navigation_notifications).isVisible = false

        //----- 專門用來判斷第一次執行 ----
        val sharedPreferences : SharedPreferences = this.getSharedPreferences("share",MODE_PRIVATE);
        val isFirstRun: Boolean = sharedPreferences.getBoolean("isFirstRun", true);
        val editor= sharedPreferences.edit();

        //-----  第一次執行強迫輸入姓名
        if (FirebaseAuth.getInstance().currentUser != null) {
            if(isFirstRun || FirebaseAuth.getInstance().currentUser!!.displayName == null)  // 第一次成功登入 強制輸入 User Name 直接更新Profile
            {
                //---- 第一次登入檢查Firebase Info  Exist ---
                val UUID = FirebaseAuth.getInstance().currentUser!!.uid
                val queryPath = "USER_PROFILE/${UUID}"
                val myRef = Firebase.database.getReference(queryPath)

                val alert = AlertDialog.Builder(this)

                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                        val userProfile : USER_PROFILE
                        if(value != null)
                        {
                            userProfile = value.copy()
                        }
                        else {
                            userProfile = USER_PROFILE()
                            userProfile.photoURL = "UserProfile_Photo/Image_Default_Member.png"
                        }

                        var editTextName: EditText? = null
                        with(alert) {
                            setTitle("請輸入姓名: ")
                            editTextName = EditText(getContext()!!)

                            if (userProfile.userName != null) {
                                editTextName!!.text =
                                    Editable.Factory.getInstance().newEditable(userProfile.userName)
                            } else {
                                editTextName!!.text = Editable.Factory.getInstance().newEditable("")
                                editTextName!!.hint = "請輸入姓名:"
                            }
                            setPositiveButton("確定", null)
                        }

                        val dialog = alert.create()
                        dialog.setCancelable(false)
                        dialog.setView(editTextName, 50, 10, 50, 10)
                        dialog.show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setOnClickListener {
                                val name: String = editTextName!!.text.toString()
                                if (!name.isNullOrBlank()) {
                                    val mAuth = FirebaseAuth.getInstance()
                                    val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                                    mAuth.currentUser!!.updateProfile(profileUpdates)

                                    userProfile.phoneNumber = mAuth.currentUser!!.phoneNumber.toString()
                                    userProfile.userID      = mAuth.currentUser!!.uid.toString()
                                    userProfile.userName    = name
                                    userProfile.tokenID     = ""
                                    myRef.setValue(userProfile)

                                    //------- Download FireBase Info -------
                                    downloadFriendList(this@MainActivity)

                                    //----- Update TokenID ------
                                    updateTokenID(UUID)
                                    initLocalProfile(UUID)

                                    //----- 成功登入將第一次登入Flag off
                                    editor.putBoolean("isFirstRun", false);
                                    editor.apply();

                                    dialog.dismiss()
                                }
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {

                        val userProfile = USER_PROFILE()
                        var editTextName: EditText? = null
                        with(alert) {
                            setTitle("請輸入姓名: ")
                            editTextName = EditText(getContext()!!)
                            editTextName!!.text = Editable.Factory.getInstance().newEditable("")
                            editTextName!!.hint = "請輸入姓名:"
                            setPositiveButton("確定", null)
                        }

                        val dialog = alert.create()
                        dialog.setCancelable(false)
                        dialog.setView(editTextName, 50, 10, 50, 10)
                        dialog.show()
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setOnClickListener {
                                val name: String = editTextName!!.text.toString()
                                if (!name.isNullOrBlank()) {

                                    val mAuth = FirebaseAuth.getInstance()
                                    val profileUpdates =
                                        UserProfileChangeRequest.Builder().setDisplayName(name)
                                            .build()
                                    mAuth.currentUser!!.updateProfile(profileUpdates)
                                    userProfile.phoneNumber =
                                        mAuth.currentUser!!.phoneNumber.toString()
                                    userProfile.userID = mAuth.currentUser!!.uid.toString()
                                    userProfile.userName =
                                        mAuth.currentUser!!.displayName.toString()
                                    userProfile.tokenID = ""
                                    userProfile.photoURL = "UserProfile_Photo/Image_Default_Member.png"

                                    myRef.setValue(userProfile)

                                    downloadFriendList(this@MainActivity)
                                    updateTokenID(UUID)
                                    initLocalProfile(UUID)

                                    editor.putBoolean("isFirstRun", false);
                                    editor.apply();
                                    dialog.dismiss()
                                }
                            }
                    }
                })
            }
            else
            {
                //----- 正常登入邏輯 -----
                updateTokenID(FirebaseAuth.getInstance().currentUser!!.uid)
                //downloadSelfProfile(FirebaseAuth.getInstance().currentUser!!.uid)
                //downloadFriendList(context)
            }
        }

        //------ 處裡 FireBase 傳入的Notify 並且存入DB裡面 -------
        if (intent?.extras != null) {
            //-----  Receive Notify by Intent
            if (intent.extras?.keySet()!!.contains("messageTitle")) {

                try {
                    val notification: entityNotification = entityNotification()
                    notification.messageID = intent!!.extras!!["google.message_id"]!!.toString()
                    notification.messageTitle = intent!!.extras!!["messageTitle"]!!.toString()
                    notification.messageBody = intent!!.extras!!["messageBody"]!!.toString()
                    notification.notificationType = intent!!.extras!!["notificationType"]!!.toString()
                    notification.receiveTime = intent!!.extras!!["receiveTime"]!!.toString()
                    notification.orderOwnerID = intent!!.extras!!["orderOwnerID"]!!.toString()
                    notification.orderOwnerName = intent!!.extras!!["orderOwnerName"]!!.toString()
                    notification.menuNumber = intent!!.extras!!["menuNumber"]!!.toString()
                    notification.orderNumber = intent!!.extras!!["orderNumber"]!!.toString()
                    notification.dueTime = intent!!.extras!!["dueTime"]!!.toString()
                    notification.brandName = intent!!.extras!!["brandName"]!!.toString()
                    notification.attendedMemberCount = intent!!.extras!!["attendedMemberCount"]!!.toString()
                    notification.messageDetail = intent!!.extras!!["messageDetail"]!!.toString()
                    notification.isRead = intent!!.extras!!["isRead"]!!.toString()
                    notification.replyStatus    = MENU_ORDER_REPLY_STATUS_WAIT

                    if(notification.notificationType == NOTIFICATION_TYPE_ACTION_JOIN_NEW_FRIEND)
                    {
                        receiveAddFriendRequest(notification.messageTitle, notification.messageBody, notification.orderOwnerID)
                    }
                    else if (notification.notificationType == NOTIFICATION_TYPE_SHARE_MENU)
                    {
                        // V 1.1
                        receiveShareMenuRequest(notification.messageTitle, notification.messageBody, notification.orderOwnerID, notification.menuNumber)
                    }
                    else if(notification.notificationType == NOTIFICATION_TYPE_CHANGE_DUETIME)
                    {
                        //  改變Due TIme  需要直接修改內容
                    }
                    else {
                        try {
                            val notificationDB = AppDatabase(this).notificationdao()
                            notificationDB.insertRow(notification)

                        } catch (e: Exception) { }
                    }
                }
               catch (e: Exception )
               { }
            }
        }
    }


    //region Init Data when Activity Start
    private fun updateTokenID(uuid: String) {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val queryPath = "USER_PROFILE/$uuid"
                val myRef = Firebase.database.getReference(queryPath)
                myRef.child("tokenID").setValue(task.result?.token.toString());
                localtokenID = task.result?.token.toString()
            })
    }

    private fun downloadFriendList( context:Context) {
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val dbContext: MemoryDatabase = MemoryDatabase(context)
        val friendDB: friendDAO = dbContext.frienddao()
        val queryPath = "USER_PROFILE/$uuid/friendList"
        val myRef = Firebase.database.getReference(queryPath)
        friendDB.deleteall()
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val friendUUID = snapshot.getValue(String::class.java)
                      try {
                            val friend: entityFriend = entityFriend(null, friendUUID!!)
                            friendDB.insertRow(friend)
                        } catch (e: Exception) {
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun downloadFriendListafterAddFriend( context:Context) {
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val dbContext: MemoryDatabase = MemoryDatabase(context)
        val friendDB: friendDAO = dbContext.frienddao()
        val queryPath = "USER_PROFILE/$uuid/friendList"
        val myRef = Firebase.database.getReference(queryPath)
        friendDB.deleteall()
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val friendUUID = snapshot.getValue(String::class.java)
                    try {
                        val friend: entityFriend = entityFriend(null, friendUUID!!)
                        friendDB.insertRow(friend)
                    } catch (e: Exception) {
                    }
                }

                //---- 通知Friend Fragment
                val broadcast = LocalBroadcastManager.getInstance(this@MainActivity)
                val intent = Intent("UpdateFriendList")
                broadcast.sendBroadcast(intent)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun downloadSelfProfile( userID:String) {
        val queryPath = "USER_PROFILE/$userID"
        val myRef = Firebase.database.getReference(queryPath)

        val dbContext = AppDatabase(this)
        val profileDB = dbContext.userprofiledao()

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                val entity = profileDB.getProfileByID(userID)
                if(entity != null) {
                    // Update Profile
                    entity.tokenID  =  value?.tokenID ?: ""
                    entity.photoURL =  value?.photoURL?: ""
                    entity.userName =  value?.userName ?: ""
                    entity.gender   =  value?.gender ?: ""
                    entity.address  =  value?.address ?: ""
                    entity.birthday =  value?.birthday?: ""

                    val photoURL = value?.photoURL ?: ""
                    if (photoURL != "") {
                        val islandRef = Firebase.storage.reference.child(photoURL!!)
                        val ONE_MEGABYTE = (1024 * 1024).toLong()
                        islandRef.getBytes(ONE_MEGABYTE)
                            .addOnSuccessListener { bytesPrm: ByteArray ->
                                entity.image = bytesPrm
                                profileDB.updateTodo(entity)
                            }
                            .addOnCanceledListener {
                                profileDB.updateTodo(entity)
                            }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    //endregion

    //region For FirstRun Init User Profile to Firebase
    private fun initLocalProfile( userID:String) {

        val dbContext = AppDatabase(this)
        val profileDB = dbContext.userprofiledao()
        val entity = profileDB.getProfileByID(userID)

        if(entity!= null) {
            profileDB.delete(entity)
        }

        val UUID = FirebaseAuth.getInstance().currentUser!!.uid
        val queryPath = "USER_PROFILE/${UUID}"
        val myRef = Firebase.database.getReference(queryPath)

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                if(value != null)
                {
                    val profile : entityUserProfile = entityUserProfile()
                    profile.uID          = value.userID
                    profile.phoneNumber  = value.phoneNumber?: ""
                    profile.photoURL     = value.photoURL?: ""
                    profile.userName     = value.userName?: ""
                    profile.tokenID      =  value.tokenID ?: ""
                    profile.gender       =  value.gender ?: ""
                    profile.address      =  value.address ?: ""
                    profile.birthday     =  value.birthday?: ""

                    val photoURL = value.photoURL ?: ""
                    if (photoURL != "") {
                        val islandRef = Firebase.storage.reference.child(photoURL!!)
                        val ONE_MEGABYTE = (1024 * 1024).toLong()
                        islandRef.getBytes(ONE_MEGABYTE)
                            .addOnSuccessListener { bytesPrm: ByteArray ->
                                profile.image = bytesPrm
                                try {
                                    profileDB.insertRow(profile)
                                } catch (e: Exception) {

                                }
                            }
                            .addOnCanceledListener {
                                try {
                                    profileDB.insertRow(profile)
                                } catch (e: Exception) {

                                }
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }
    //endregion


    private fun UpdateBadge()
    {
        val notificationDB = AppDatabase(this).notificationdao()
        val unread = notificationDB.getUnreadNotify().count()
        if(unread == 0) {
            navView.getOrCreateBadge(R.id.navigation_notifications).isVisible = false
        }
        else
        {
            navView.getOrCreateBadge(R.id.navigation_notifications).isVisible = true
            navView.getOrCreateBadge(R.id.navigation_notifications).number = unread
        }
    }

    //region 互家好友功能
    private fun receiveAddFriendRequest(msgTitle: String, msgBody: String, FriendUUID: String) {
        val alert = AlertDialog.Builder(this)
        with(alert) {
            setTitle(msgTitle)
            setMessage(msgBody)
            setCancelable(false)
            setPositiveButton("確定") { dialog, _ ->
                dialog.dismiss()
                CheckFriendExist(FriendUUID)
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = alert.create()
        dialog.show()
    }

    //分享菜單請求資訊
    private fun receiveShareMenuRequest(msgTitle: String, msgBody: String, menuOwner:String, menuNumber: String) {
        val alert = AlertDialog.Builder(this)
        with(alert) {
            setTitle(msgTitle)
            setMessage(msgBody)
            setCancelable(false)
            setPositiveButton("確定") { dialog, _ ->
                dialog.dismiss()
                DownLoadShareMenu(menuOwner,menuNumber)
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = alert.create()
        dialog.show()
    }

    private fun DownLoadShareMenu(menuOwner:String, menuNumber: String) {
        val uuid = FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val queryPath = "USER_PROFILE/$uuid"
        val myRef = Firebase.database.getReference(queryPath)
        var userProfile : USER_PROFILE? = null
        var menuInfo : USER_MENU? = null
        var MemoryDBContext = MemoryDatabase(this!!)
        var MenuImageDB = MemoryDBContext.menuImagedao()

        ProgressDialogUtil.showProgressDialog(this);
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                ProgressDialogUtil.dismiss()
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("錯誤")
                    .setMessage("下載使用者資料時發生異常")
                    .setPositiveButton("確定"){ dialog, _ -> }
                    .create()
                    .show()
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tmp_userProfile = dataSnapshot.getValue(USER_PROFILE::class.java)
                if(tmp_userProfile == null)
                {
                    ProgressDialogUtil.dismiss()
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("錯誤")
                        .setMessage("下載使用者資料時發生異常")
                        .setPositiveButton("確定") { dialog, _ -> }
                        .create()
                        .show()
                }
                else {
                    userProfile = tmp_userProfile.copy()
                    //---- Download Menu Info
                    val menuPath = "USER_MENU_INFORMATION/${menuOwner}/${menuNumber}"
                    val menuRef = Firebase.database.getReference(menuPath)
                    menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            menuInfo = dataSnapshot.getValue(USER_MENU::class.java)
                            if (menuInfo == null) {
                                ProgressDialogUtil.dismiss()
                                AlertDialog.Builder(this@MainActivity)
                                    .setTitle("錯誤")
                                    .setMessage("分享的菜單不存在")
                                    .setPositiveButton("確定") { dialog, _ -> }
                                    .create()
                                    .show()
                            } else {
                                menuInfo!!.multiMenuImageURL?.forEach()
                                {
                                    val islandRef = Firebase.storage.reference.child(it)
                                    val ONE_MEGABYTE = 1024 * 1024.toLong()
                                    islandRef.getBytes(ONE_MEGABYTE)
                                        .addOnSuccessListener { bytesPrm: ByteArray ->
                                            val brandImageStream = bytesPrm.clone()
                                            try {
                                                MenuImageDB.insertRow(
                                                    entityMeunImage(
                                                        null,
                                                        it,
                                                        "",
                                                        brandImageStream!!
                                                    )
                                                )
                                            } catch (e: Exception) {
                                            }
                                        }
                                }

                                ProgressDialogUtil.dismiss()
                                CheckAddUserShareMenu(userProfile!!, menuInfo!!)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                            ProgressDialogUtil.dismiss()
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("錯誤")
                                .setMessage("下載菜單時發生異常")
                                .setPositiveButton("確定") { dialog, _ -> }
                                .create()
                                .show()
                        }
                    })
                }
            }
        })
    }


    private fun CheckAddUserShareMenu(userProfile :USER_PROFILE, menuInfo: USER_MENU) {


        var MemoryDBContext = MemoryDatabase(this!!)
        var MenuImageDB = MemoryDBContext.menuImagedao()

        val dbContext = AppDatabase(this)
        val menuICON = dbContext.localImagedao()

        val editBrandName = EditText(this)
        val editBrandCategory = EditText(this)
        val BrandNamehint = SpannableString(menuInfo.brandName)
        val BrandCategoryhint = SpannableString(menuInfo.brandCategory)
        editBrandName.hint = BrandNamehint
        editBrandCategory.hint = BrandCategoryhint

        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.VERTICAL
        ll.addView(editBrandName)
        ll.addView(editBrandCategory)
        ll.setPadding(60, 0, 60, 0);

        AlertDialog.Builder(this@MainActivity)
            .setTitle("請修改菜單品牌名稱及分類")
            .setView(ll)
            .setCancelable(false)
            .setPositiveButton("確定"){ dialog, _ ->
                ProgressDialogUtil.showProgressDialog(this, "建立菜單中");

                //----確認刪除資料 -------
                // 1. 取得Menu Order Number NEW
                var OwnerUUID = FirebaseAuth.getInstance().currentUser!!.uid.toString()
                val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                val MenuNumber = "${OwnerUUID}-MENU-${timeStamp}"

                // 2. 組合菜單資訊 -------
                menuInfo.createTime = timeStamp
                if(editBrandName.text.toString() != "") {
                    menuInfo.brandName = editBrandName.text.toString()
                }
                if(editBrandCategory.text.toString() != "") {
                    menuInfo.brandCategory =editBrandCategory.text.toString()
                }
                menuInfo.menuNumber = MenuNumber
                menuInfo.userID = OwnerUUID
                menuInfo.userName = userProfile.userName
                var MenuImageList : MutableList<String> = mutableListOf()
                var index : Int = 0
                menuInfo.multiMenuImageURL?.forEach {
                    var menuImage = MenuImageDB.getMenuImageByName(it)
                    if(menuImage != null) {
                        val newPath = generatorFileURL(OwnerUUID,MenuNumber,index)
                        MenuImageDB.insertRow(entityMeunImage(null,newPath , menuImage.desc, menuImage.image))
                        MenuImageList.add(newPath)
                        index++
                    }
                    // 如果菜單照片不存在直接忽略他
                }

                menuInfo.multiMenuImageURL?.clear()
                MenuImageList.forEach()
                {
                    menuInfo.multiMenuImageURL?.add(it)
                }


                //----- 3. 直接上傳fireBase------
                Firebase.database.reference.child("USER_MENU_INFORMATION").child(OwnerUUID).child(MenuNumber).setValue(menuInfo)
                    .addOnSuccessListener {

                        //----- 上傳成功以後 更新 Image ----
                        menuInfo.multiMenuImageURL?.forEach()
                        { it ->
                            val MenuImageObject = MenuImageDB.getMenuImageByName(it)
                            if (MenuImageObject != null) {
                                val islandRef = Firebase.storage.reference.child(it!!)
                                val uploadTask: UploadTask = islandRef.putBytes(MenuImageObject.image)
                                uploadTask.addOnFailureListener {
                                    Toast.makeText(this, "上傳照片失敗!! \n ${it}", Toast.LENGTH_SHORT).show()
                                }.addOnSuccessListener {}
                            }
                        }

                        val iCONImage =  menuInfo.multiMenuImageURL?.firstOrNull()
                        if(iCONImage != null)
                        {
                            val oldICONImage = menuICON.getMenuImageByName(iCONImage)
                            val MenuImageObject = MenuImageDB.getMenuImageByName(iCONImage)
                            if(MenuImageObject!=null)
                            {
                                if(oldICONImage == null) {
                                    menuICON.insertRow(entityLocalmage(null, MenuImageObject.name, "", MenuImageObject.image.clone()!!))
                                }
                                else
                                {
                                    oldICONImage.image = MenuImageObject.image.clone()
                                    menuICON.updateTodo(oldICONImage)
                                }
                            }
                        }

                        //----- 更新MenuInfo ----
                        if(menuInfo.brandCategory != "") {
                            //--------- 檢查Category -------
                            if (userProfile.brandCategoryList == null) {
                                userProfile.brandCategoryList = mutableListOf()
                                userProfile.brandCategoryList!!.add(menuInfo.brandCategory!!)
                                val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
                                val queryPath = "USER_PROFILE/$uuid/brandCategoryList"
                                Firebase.database.getReference(queryPath).setValue(userProfile.brandCategoryList)
                            }
                            else
                            {
                                if (!userProfile.brandCategoryList!!.contains(menuInfo.brandCategory!!))
                                {
                                    userProfile.brandCategoryList!!.add(menuInfo.brandCategory!!)
                                    val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
                                    val queryPath = "USER_PROFILE/$uuid/brandCategoryList"
                                    Firebase.database.getReference(queryPath).setValue(userProfile.brandCategoryList)
                                }
                            }
                        }


                        ProgressDialogUtil.dismiss()

                        val broadcast = LocalBroadcastManager.getInstance(this@MainActivity)
                        val intent = Intent("UpdateMenuList")
                        broadcast.sendBroadcast(intent)

                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("訊息")
                            .setMessage("建立分享菜單完成!!")
                            .setPositiveButton("確定") { dialog, _ -> }
                            .create()
                            .show()

                    }
                    .addOnFailureListener {
                        ProgressDialogUtil.dismiss()
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("錯誤")
                            .setMessage("建立分享菜單時發生錯誤")
                            .setPositiveButton("確定") { dialog, _ -> }
                            .create()
                            .show()

                    }
            }
            .create()
            .show()


    }


    private fun generatorFileURL (MenuUserID:String, MenuNumber:String, index: Int) : String
    {
        val filepath =  "Menu_Image/${MenuUserID}/${MenuNumber}/${index.toString()}.jpg"
        return filepath
    }

    private fun CheckFriendExist(FriendUUID: String) {

        val dbMemoryContext: MemoryDatabase = MemoryDatabase(this)
        val friendDB: friendDAO = dbMemoryContext.frienddao()
        val mFriends = friendDB.getFriendByName(FriendUUID)
        if (mFriends.count() > 0) {
            val alert = AlertDialog.Builder(this)
            with(alert) {
                setMessage("$FriendUUID \n好友已經存在 !!")
                setPositiveButton("確定") { dialog, _ ->
                    dialog.dismiss()
                }
            }
            val dialog = alert.create()
            dialog.show()
        } else {
            addFriendToFireBase(FriendUUID)
        }
    }

    private fun addFriendToFireBase(friendUUID: String) {
        val context = this
        val uuid = FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val queryPath = "USER_PROFILE/$uuid"
        val myRef = Firebase.database.getReference(queryPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userProfile = dataSnapshot.getValue(USER_PROFILE::class.java)
                if (userProfile != null) {
                    userProfile.friendList?.add(friendUUID)
                }
                dataSnapshot.ref.setValue(userProfile)

                //---- 加入好友以後要再重新更新一次好友清單
                downloadFriendListafterAddFriend(context)

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }



    //endregion

    companion object {

        private var instance: MainActivity? = null
        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

        public var localtokenID = ""
        const val FCM_API = "https://fcm.googleapis.com/fcm/send"
        const val serverKey = "key=" + "AAAAc-l4bjA:APA91bHmg82XTJqzC_ORewYl2DbVDiU-_RQuZ8lm35_6puT3FuKRvFjLnoB89MamtEc31_31HVuPjQ27qwIHCLWjWqS8zXcBb6dBg7YaD_tPlfKRcgPredRO5TlU-JoENtLKx4Og1Qa4"
        const val contentType = "application/json"
        private val requestQueue: RequestQueue by lazy { Volley.newRequestQueue(instance!!.applicationContext) }


        public fun sendFirebaseNotification(notification: JSONObject) {
            Log.e("TAG", "sendFirebaseNotification")
            val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
                Response.Listener<JSONObject> { response ->
                    Log.i("TAG", "onResponse: $response")
                },
                Response.ErrorListener {
                    Toast.makeText(instance, "Request error", Toast.LENGTH_LONG).show()
                    Log.i("TAG", "onErrorResponse: Didn't work")
                }) {

                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["Authorization"] = serverKey
                    params["Content-Type"] = contentType
                    return params
                }
            }
            requestQueue.add(jsonObjectRequest)
        }
    }
}
