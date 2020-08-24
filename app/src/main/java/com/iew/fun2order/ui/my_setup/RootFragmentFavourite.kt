//------  20200511 Chris 整理修正 ----
package com.iew.fun2order.ui.my_setup

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.internal.Utility.arrayList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.zxing.Result
import com.google.zxing.integration.android.IntentIntegrator
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.contact.contactUtils_PhoneBase
import com.iew.fun2order.contact.PhoneBase
import com.iew.fun2order.db.dao.friendDAO
import com.iew.fun2order.db.dao.friendImageDAO
import com.iew.fun2order.db.dao.group_detailDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityFriend
import com.iew.fun2order.db.entity.entityFriendImage
import com.iew.fun2order.db.firebase.USER_PROFILE
import com.iew.fun2order.ui.my_setup.decodeQR.DecodeImgCallback
import com.iew.fun2order.ui.my_setup.decodeQR.DecodeImgThread
import com.iew.fun2order.ui.my_setup.decodeQR.ImageUtil
import com.iew.fun2order.ui.my_setup.decodeQR.ScanActivity
import com.iew.fun2order.utility.*
import org.json.JSONObject
import kotlin.math.roundToInt


class RootFragmentFavourite() : Fragment(),IAdapterOnClick {

    private lateinit var friendDB: friendDAO
    private lateinit var friendImageDB: friendImageDAO

    private lateinit var groupDetailDB : group_detailDAO
    private lateinit var dbContext: AppDatabase
    private lateinit var memoryContext: MemoryDatabase

    private var lstFavorite: MutableList<ItemsLV_Favourite> = mutableListOf()
    private var rcvFavorite: RecyclerView? = null
    private var swipeFavorite: SwipeRefreshLayout? = null
    private var btnAddFriend: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater!!.inflate(R.layout.fragment_favourite, container, false)
    }


    override fun onStart() {
        super.onStart()
        prepareFriendListShow()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(messageReceiver, IntentFilter("UpdateFriendList"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver)
    }


    private var messageReceiver = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            prepareFriendListShow()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbContext = AppDatabase(requireContext())
        memoryContext = MemoryDatabase(requireContext())

        friendDB = memoryContext.frienddao()
        friendImageDB = memoryContext.friendImagedao()
        groupDetailDB = dbContext.groupdetaildao()

        activity?.let {
            rcvFavorite = it.findViewById<RecyclerView>(R.id.RecycleViewAddFriend)
            swipeFavorite= it.findViewById<SwipeRefreshLayout>(R.id.SwipeRefresh)
            btnAddFriend = it.findViewById<Button>(R.id.addfriend)
        }

        val buttonActions = arrayOf("從相機掃描加入", "從相簿讀取加入", "從通訊錄加入")
        rcvFavorite!!.layoutManager = LinearLayoutManager(requireContext())
        rcvFavorite!!.adapter = AdapterRC_Favourite(requireContext(), lstFavorite, this)
        btnAddFriend!!.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("選擇加入好友的方式")
                .setItems(buttonActions, DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    when (which) {
                        0 -> { QRCodeScanFromCameraWithIntent() }
                        1 -> {
                            if (!hasReadExternalStoragePermission()) {
                                if (!requestReadExternalStoragePermission()) {
                                    QRCodeScanFromAlbumWithIntent()
                                }
                            } else {
                                QRCodeScanFromAlbumWithIntent()
                            }
                        }
                        2-> {
                            if (!hasReadContactsPermission()) {
                                if (!requestReadContactsPermission()) {
                                    getPhoneNoFromContact()
                                }
                            } else {
                                getPhoneNoFromContact()
                            }
                        }
                        else -> {
                            Toast.makeText(activity, "選取到取消", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        prepareFriendListShow()

        swipeFavorite!!.setOnRefreshListener {
            downloadFriendList(context!!)
            swipeFavorite!!.isRefreshing = false;
        }

    }

    private fun prepareFriendListShow()
    {
        val friendList = friendDB.getFriendslist()
        lstFavorite.clear()
        friendList.forEach() {it->
           lstFavorite.add(ItemsLV_Favourite(it, "image_default_member",""))
        }
        recycleViewRefresh()
    }

    override fun onClick(sender: String, pos: Int, type: Int) {
        if (type == 1) {
            CheckRemoveFavourite(pos)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IntentIntegrator.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                    CheckAddFavourite(result.contents)
                }
            }

            ACTION_ALBUM_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val path = ImageUtil.getImageAbsolutePath(context, data!!.data)
                    DecodeImgThread(
                        path!!,
                        object :
                            DecodeImgCallback {
                            override fun onImageDecodeSuccess(result: Result?) {
                                CheckAddFavourite(result.toString())
                            }

                            override fun onImageDecodeFailed() {
                                Toast.makeText(
                                    activity,
                                    "Scan image failed, please retry.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }).run()
                }
            }

            ACTION_ADD_FRIEND_BY_CONTACT_REQUEST_CODE ->
            {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val addMembersList = data.extras?.get("AddMembers") as ArrayList<String> ?: arrayList()
                    addMembersList.forEach() {
                        addFriendProcess(it)
                        Thread.sleep(100)
                    }
                }
            }

            else -> {
                println("no handler onActivityReenter")
            }
        }
    }

    private fun QRCodeScanFromCameraWithIntent() {

        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.captureActivity = ScanActivity::class.java
        integrator.setOrientationLocked(false)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setCameraId(0)
        integrator.setBeepEnabled(false)
        integrator.initiateScan()

        //integrator.setPrompt("Scan")
        //integrator.setBarcodeImageEnabled(false)
    }


    private fun QRCodeScanFromAlbumWithIntent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, ACTION_ALBUM_REQUEST_CODE)
    }


    private fun CheckRemoveFavourite(position: Int) {

        val deleteItems = lstFavorite[position] as ItemsLV_Favourite
        val deleteUUID = deleteItems.Name

        val alert = AlertDialog.Builder(requireContext())
        with(alert) {
            setTitle("確認刪除好友")
            setMessage(deleteItems.displayname)
            setPositiveButton("確定") { dialog, _ ->
                val mFriends = friendDB.getFriendByName(deleteUUID)
                mFriends.forEach()
                {
                    try {
                        friendDB.delete(it)
                        removeFriendToFireBase(deleteUUID)
                        removeFriendFromGroupList(deleteUUID)
                        prepareFriendListShow()
                    } catch (e: Exception) {
                        val errorMsg = e.localizedMessage
                        Toast.makeText(activity, errorMsg.toString(), Toast.LENGTH_LONG).show()
                    }
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

    private fun CheckAddFavourite(uuid: String) {

        val alert = AlertDialog.Builder(context!!)
        with(alert) {
            setTitle("確認加入好友")
            setMessage(uuid)
            setPositiveButton("確定") { dialog, _ ->

                var illegalUUID : Boolean = false
                val illegalch = charArrayOf('.', '#', '$', '[', ']')
                for (i in illegalch) {
                    if(uuid.contains(i) == true)
                    {
                        illegalUUID = true
                    }
                }

                if(illegalUUID == true)
                {
                    dialog.dismiss()

                    val alert = AlertDialog.Builder(context!!)
                    with(alert) {
                        setTitle("資料格式錯誤")
                        setMessage("$uuid \n資料不合法, 無法加入好友 !!")
                        setPositiveButton("確定") { dialog, _ ->
                            dialog.dismiss()
                        }
                    }
                    val dialog = alert.create()
                    dialog.show()

                }
                else if(uuid == FirebaseAuth.getInstance().currentUser!!.uid.toString())
                {
                    dialog.dismiss()
                    val alert = AlertDialog.Builder(context!!)
                    with(alert) {
                        setTitle("資料內容錯誤")
                        setMessage("無法自己加入自己好友 !!")
                        setPositiveButton("確定") { dialog, _ ->
                            dialog.dismiss()
                        }
                    }
                    val dialog = alert.create()
                    dialog.show()
                }
                else {
                    addFriendProcess(uuid)
                    dialog.dismiss()
                }
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = alert.create()
        dialog.show()
    }

    fun recycleViewRefresh() {
        rcvFavorite!!.adapter!!.notifyDataSetChanged()
    }

    private fun Bitmap.resizeByWidth(width: Int): Bitmap {
        val ratio: Float = this.width.toFloat() / this.height.toFloat()
        val height: Int = (width / ratio).roundToInt()

        return Bitmap.createScaledBitmap(
            this,
            width,
            height,
            false
        )
    }


    private fun hasReadExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else true
    }


    private fun requestReadExternalStoragePermission(): Boolean{
        //MarshMallow(API-23)之後要在 Runtime 詢問權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val perms: Array<String> = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val permsRequestCode : Int= ACTION_ALBUM_REQUEST_CODE;
            requestPermissions(perms, permsRequestCode);
            return true;
        }
        return false;
    }

    private fun hasReadContactsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        } else true
    }


    private fun requestReadContactsPermission(): Boolean{
        //MarshMallow(API-23)之後要在 Runtime 詢問權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val perms: Array<String> = arrayOf(Manifest.permission.READ_CONTACTS)
            val permsRequestCode : Int= ACTION_CONTACT_REQUEST_CODE;
            requestPermissions(perms, permsRequestCode);
            return true;
        }
        return false;
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACTION_ALBUM_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    QRCodeScanFromAlbumWithIntent()
                }
                else
                {
                    dealwithPermiss(requireContext())
                }
            }
        }
        else if (requestCode == ACTION_CONTACT_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getPhoneNoFromContact()
                }
                else
                {
                    dealwithPermiss(requireContext())
                }
            }
        }
    }

    fun dealwithPermiss(context: Context) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("操作提示")
            .setMessage("注意：缺少必要權限 !! \n請進行授權才有辦法繼續進行.")
            .setPositiveButton("進行授權"){_,_ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts(
                    "package",
                    context.applicationContext.packageName,
                    null
                )
                intent.data = uri
                context.startActivity(intent) }
            .setNegativeButton("取消") { _,_ ->

                Toast.makeText(requireContext(), "取消操作", Toast.LENGTH_SHORT).show()

            }
            .create()
            .show()
    }

    private fun addFriendToFireBase(friendUUID: String)
    {
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val queryPath = "USER_PROFILE/$uuid"
        val myRef = Firebase.database.getReference(queryPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userProfile = dataSnapshot.getValue(USER_PROFILE::class.java)
                if (userProfile != null) {
                    userProfile.friendList?.add(friendUUID)
                }
                dataSnapshot.ref.setValue(userProfile)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

    }


    private fun sendoutAddFriendRequest(friendUUID: String, friendTokenID: String, friendOSType: String?)
    {
            var selfInfo = FirebaseAuth.getInstance().currentUser
            val notification = JSONObject()
            val notificationHeader = JSONObject()
            val notificationBody = JSONObject()

            val body = "『 ${selfInfo?.displayName} 』已經把你加入好友，請問你願意將『 ${selfInfo?.displayName} 』也加入成為妳的好友嗎？"

            notificationHeader.put("title", "好友邀請")
            notificationHeader.put("body", body ?: "")   //Enter your notification message

            notificationBody.put("messageID", "")      //Enter
            notificationBody.put("messageTitle", "好友邀請")   //Enter
            notificationBody.put("messageBody", body ?: "")    //Enter

            notificationBody.put("notificationType", NOTIFICATION_TYPE_ACTION_JOIN_NEW_FRIEND)   //Enter
            notificationBody.put("receiveTime", "")   //Enter
            notificationBody.put("orderOwnerID", selfInfo?.uid)   //Enter
            notificationBody.put("orderOwnerName", selfInfo?.displayName)   //Enter
            notificationBody.put("menuNumber", "")   //Enter

            notificationBody.put("orderNumber", "")   //Enter
            notificationBody.put("dueTime", "")   //Enter

            notificationBody.put("brandName", "")   //Enter
            notificationBody.put("attendedMemberCount", "")   //Enter

            notificationBody.put("messageDetail", friendUUID)   //Enter  //
            notificationBody.put("isRead", "N")   //Enter
            notificationBody.put("replyStatus", "")   //Enter
            notificationBody.put("replyTime", "")   //Enter

            // your notification message
            notification.put("to", friendTokenID)
            notification.put("notification", notificationHeader)
            notification.put("data", notificationBody)

            if(friendOSType?:"iOS" == "Android")
            {
                notification.remove("notification")
            }

            Thread.sleep(100)
            com.iew.fun2order.MainActivity.sendFirebaseNotificationSingle(notification)
    }

    private fun removeFriendToFireBase(friendUUID: String)
    {
        val uuid =  FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val queryPath = "USER_PROFILE/$uuid"
        val myRef = Firebase.database.getReference(queryPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userProfile = dataSnapshot.getValue(USER_PROFILE::class.java)
                if (userProfile != null) {
                    userProfile.friendList?.remove(friendUUID)
                }
                dataSnapshot.ref.setValue(userProfile)
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

    }


    private fun removeFriendFromGroupList(friendUUID: String)
    {
        groupDetailDB.deleteFriend(friendUUID)
    }



    private fun downloadFriendList( context: Context) {
        val uuid = FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val queryPath = "USER_PROFILE/$uuid/friendList"
        val myRef = Firebase.database.getReference(queryPath)
        friendDB.deleteall()
        friendImageDB.deleteall()
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val friendUUID = snapshot.getValue(String::class.java)
                    try {
                        val friend: entityFriend = entityFriend(null, friendUUID!!)
                        friendDB.insertRow(friend)
                    }
                    catch (e: Exception) {

                    }
                    var queryPath = "USER_PROFILE/" + friendUUID.toString()
                    val database = Firebase.database
                    val myRef = database.getReference(queryPath)
                    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val value = dataSnapshot.getValue(USER_PROFILE::class.java)
                            val photoURL = value?.photoURL
                            if (photoURL != null) {
                                var islandRef = Firebase.storage.reference.child(photoURL)
                                val ONE_MEGABYTE = 1024 * 1024.toLong()
                                islandRef.getBytes(ONE_MEGABYTE)
                                    .addOnSuccessListener { bytesPrm: ByteArray ->
                                        try {
                                            val friendImage: entityFriendImage = entityFriendImage(
                                                null,
                                                value?.userID!!,
                                                value?.userName!!,
                                                value?.tokenID!!,
                                                value?.ostype,
                                                bytesPrm
                                            )
                                            friendImageDB.insertRow(friendImage)
                                        } catch (ex: Exception) {
                                        }
                                    }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            // Failed to read value
                            // Log.w(TAG, "Failed to read value.", error.toException())
                        }
                    })
                }
                prepareFriendListShow()
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private fun getPhoneNoFromContact() {

        val queryPath = "USER_PROFILE/"
        val contactsList: ArrayList<PhoneBase> = contactUtils_PhoneBase.getAllContacts(requireContext())
        val contactCount = contactsList.count()
        var count = 0

        ProgressDialogUtil.showProgressDialog(context)
        contactsList.forEach {
            val phoneQuery = Firebase.database.getReference(queryPath).orderByChild("phoneNumber").equalTo(it.phone.toString())
            phoneQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    count++
                    if(count == contactCount)
                    {
                        ProgressDialogUtil.dismiss()
                        setUpContactInfo(contactsList)

                    }
                }
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var profile : USER_PROFILE? = null
                    for (snapshot in dataSnapshot.children) {
                         profile = snapshot.getValue(USER_PROFILE::class.java)
                         break;
                    }

                    it.firebaseDisplayName = profile?.userName
                    it.firebaseTokenID = profile?.tokenID
                    it.firebaseUUID = profile?.userID
                    it.firebaseImagePath = profile?.photoURL
                    count++
                    if(count == contactCount)
                    {
                        ProgressDialogUtil.dismiss()
                        setUpContactInfo(contactsList)
                    }
                }
            })
        }
    }

    private fun setUpContactInfo(contacts: ArrayList<PhoneBase>) {

        val candidate = contacts.filter { it -> it.firebaseUUID != null }
        candidate.forEach {
            it ->
            if(it.firebaseUUID ==FirebaseAuth.getInstance().currentUser!!.uid)
            {
                it.firebaseDisplayName = null
                it.firebaseTokenID = null
                it.firebaseUUID = null
            }

            val firebaseUUID = it.firebaseUUID ?: ""
            if(firebaseUUID != "") {
                val exist = friendDB.getFriendByName(firebaseUUID)
                if (exist.count() != 0) {
                    it.firebaseDisplayName = null
                    it.firebaseTokenID = null
                    it.firebaseUUID = null
                }
            }
        }

        val afterFilterCandidate = candidate.filter { it -> it.firebaseUUID != null }
        if (afterFilterCandidate.count() > 0) {
            val bundle = Bundle()
            bundle.putParcelableArrayList("AddFriendCandidate", ArrayList(afterFilterCandidate))
            val intent = Intent(context, ActivityAddFriendByContact::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, ACTION_ADD_FRIEND_BY_CONTACT_REQUEST_CODE)

        } else {
            val alert = AlertDialog.Builder(requireContext())
            with(alert) {
                setTitle("訊息通知")
                setMessage("通訊錄中的好友均已加入\n目前並沒有可以加入的好友清單!!")
                setPositiveButton("確定") { dialog, _ ->
                    dialog.dismiss()
                }
            }
        }
    }


    private fun addFriendProcess(uuid : String)
    {
        //---- 確認加入好友的當下 馬上去檢查 FireBase Exist info ----
        val queryPath = "USER_PROFILE/$uuid"
        val database = Firebase.database
        val myRef = database.getReference(queryPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    val alert = AlertDialog.Builder(context!!)
                    with(alert) {
                        setTitle("錯誤通知")
                        setMessage("$uuid \n資料不存在 !!")
                        setPositiveButton("確定") { dialog, _ ->
                            dialog.dismiss()
                        }
                    }
                    val dialog = alert.create()
                    dialog.show()
                } else {
                    val friendInfo = dataSnapshot.getValue(USER_PROFILE::class.java)
                    if (friendInfo == null) {
                        Toast.makeText(activity, "$uuid 好友資料格式錯誤!!", Toast.LENGTH_LONG).show()
                    } else {
                        val mFriends = friendDB.getFriendByName(uuid)
                        if (mFriends.count() > 0) {
                            val alert = AlertDialog.Builder(context!!)
                            with(alert) {
                                setMessage("$uuid \n好友已經存在 !!")
                                setPositiveButton("確定") { dialog, _ ->
                                    dialog.dismiss()
                                }
                            }
                            val dialog = alert.create()
                            dialog.show()
                        } else {
                            try {
                                // Insert DB
                                val friend: entityFriend = entityFriend(null, uuid)
                                friendDB.insertRow(friend)
                                addFriendToFireBase(uuid)
                                prepareFriendListShow()
                                sendoutAddFriendRequest(friendInfo.userID, friendInfo.tokenID, friendInfo.ostype)

                            } catch (e: Exception) {
                                val errorMsg = e.localizedMessage
                                Toast.makeText(
                                    activity,
                                    errorMsg.toString(),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}



