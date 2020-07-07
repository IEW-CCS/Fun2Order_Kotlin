package com.iew.fun2order.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.friendDAO
import com.iew.fun2order.db.dao.friendImageDAO
import com.iew.fun2order.db.dao.groupDAO
import com.iew.fun2order.db.dao.group_detailDAO
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.entity.entityGroup
import com.iew.fun2order.db.firebase.ORDER_MEMBER
import com.iew.fun2order.db.firebase.USER_MENU

import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.ui.my_setup.*
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_WAIT
import com.iew.fun2order.utility.NOTIFICATION_TYPE_ACTION_JOIN_ORDER
import kotlinx.android.synthetic.main.alert_date_time_picker.view.*
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class ActivitySetupOrder : AppCompatActivity(), IAdapterOnClick, IAdapterCheckBOXChanged {

    var  listGroup: MutableList<ItemsLV_Group> = mutableListOf()
    val  listGroupDetail: MutableList<Any> = mutableListOf()
    val  List_Candidate: MutableList<ItemsLV_Canditate> = mutableListOf()

    var  rcvGroupDetail : RecyclerView? = null
    var  rcvGroup : RecyclerView? = null
    var  txtGroupInfo : TextView? = null
    var  btnJoinGroupBuy : CheckBox? = null

    var locations: MutableList<String> = mutableListOf()

    private lateinit var DBContext: AppDatabase
    private lateinit var MemoryDBContext: MemoryDatabase

    private lateinit var groupdetailDB : group_detailDAO
    private lateinit var groupDB: groupDAO
    private lateinit var friendDB : friendDAO

    private  var SelectGroupID: String = ""
    private  var SelectGroupName: String = ""
    private  var mMenuID: String = ""
    private  var mUserMenu: USER_MENU = USER_MENU()
    private  var msChuGroupDetailMsg: String = ""
    private val TIME_PICKER_INTERVAL = 15

    private lateinit var textViewChuGroupDueDate : TextView

    //Firebase DB
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.iew.fun2order.R.layout.activity_setup_order)
        supportActionBar?.hide()
        locations.clear()

        val context: Context = this@ActivitySetupOrder

        // [START initialize_database_ref]
        mDatabase = Firebase.database.reference

        // [END initialize_database_ref]

        DBContext = AppDatabase(context!!)

        MemoryDBContext = MemoryDatabase(context!!)

        groupDB = DBContext.groupdao()
        friendDB = MemoryDBContext.frienddao()
        groupdetailDB = DBContext.groupdetaildao()

        mMenuID = intent.extras.getString("MENU_ID")
        mUserMenu = intent?.extras?.get("USER_MENU") as USER_MENU

        var textViewMenuName = findViewById(com.iew.fun2order.R.id.textViewMenuName) as TextView
        textViewMenuName.setText(mMenuID)


        var textViewSendVite = findViewById(com.iew.fun2order.R.id.textViewSendVite) as TextView
        textViewSendVite.setOnClickListener {
            createNewOrder()
        }


        rcvGroupDetail = findViewById(com.iew.fun2order.R.id.recyclerViewGroupMemberList) as RecyclerView
        rcvGroup = findViewById(com.iew.fun2order.R.id.recyclerViewGroupList) as RecyclerView
        txtGroupInfo = findViewById(com.iew.fun2order.R.id.textViewMemberGroupName) as TextView
        btnJoinGroupBuy = findViewById(com.iew.fun2order.R.id.checkBoxJoinGroupBuy) as CheckBox

        //recyclerViewGroupMemberList!!.layoutManager = LinearLayoutManager(this!!)
        //recyclerViewGroupMemberList!!.adapter = RCAdapter_Candidate( this, listpersion, this)

        rcvGroup!!.layoutManager =  LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL ,false)
        rcvGroup!!.adapter = AdapterRC_Group( context, listGroup , this)

        rcvGroupDetail!!.layoutManager =  LinearLayoutManager(context,LinearLayoutManager.VERTICAL ,false)
        rcvGroupDetail!!.adapter = AdapterRC_Candidate(context, List_Candidate, this)

        groupDB.getAllGroup().observe(this, Observer {
            var list = it as java.util.ArrayList<entityGroup>
            listGroup.clear()
            list.forEach() {
                val groupbmp = BitmapFactory.decodeByteArray(it.image,0,it.image.size)
                listGroup.add(ItemsLV_Group(it.name, groupbmp, it.groupid))
            }

            //---------------------------------------
            if(list.count()!=0)
            {
                if(SelectGroupID == "") {
                    SelectGroupID = list[0].groupid
                    SelectGroupName = list[0].name
                }

                txtGroupInfo!!.setText(SelectGroupName + ":好友列表")
                var getfriend =  groupdetailDB.getMemberByGroupID(SelectGroupID)
                listGroupDetail.clear()
                List_Candidate.clear()
                //listGroupDetail.add(ItemsLV_GroupDetail("新增好友", "icon_add_group"))
                getfriend.forEach()
                {
                    listGroupDetail.add(ItemsLV_Favourite(it, "image_default_member",""))
                    List_Candidate.add(ItemsLV_Canditate(it, "image_default_member","","", "",true))
                }
            }
            else
            {
                SelectGroupID = ""
                SelectGroupName = ""
                txtGroupInfo!!.setText("好友列表")
                listGroupDetail.clear()
                List_Candidate.clear()
            }
            RecycleViewRefresh()
        })

        //setListViewHeightBasedOnChildren(listViewAddMember, view)
        val textViewSetupDueDate = findViewById(R.id.textViewSetupDueDate) as TextView
        // set on-click listener for ImageView
        textViewSetupDueDate.setOnClickListener {

            val sCrTimeStamp: String = SimpleDateFormat("yyyyMMddHHmm").format(Date())

            val item = LayoutInflater.from(this).inflate(R.layout.alert_date_time_picker, null)

            //-----準備設定Fragment ------
            // Extract the TabHost
            val mTabHost = item.tab_host
            mTabHost.setup()
            // Create Date Tab and add to TabHost
            val mDateTab: TabHost.TabSpec = mTabHost.newTabSpec("date")
            mDateTab.setIndicator("日期");
            mDateTab.setContent(R.id.date_content);

            mTabHost.addTab(mDateTab)
            // Create Time Tab and add to TabHost
            val mTimeTab: TabHost.TabSpec = mTabHost.newTabSpec("time")
            mTimeTab.setIndicator("時間");
            mTimeTab.setContent(R.id.time_content);
            mTabHost.addTab(mTimeTab)


            val picker = item.findViewById(R.id.tpPicker) as TimePicker
            picker.setIs24HourView(true)
            //picker.setCurrentHour(0);
            picker.setCurrentMinute(0);

            setTimePickerInterval(picker);

            var alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setPositiveButton("確定", null)
                .setNegativeButton("取消", null)
                .show()

            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val dpPicker = item.findViewById(R.id.dpPicker) as DatePicker
                    val tpPicker = item.findViewById(R.id.tpPicker) as TimePicker

                    var sDay = "01"
                    var sMonth ="01"
                    var sYear ="2099"
                    var sHour = "12";
                    var sMin = "00"

                    if(dpPicker.getDayOfMonth()<10){
                        sDay = "0" + dpPicker.getDayOfMonth().toString()
                    }else{
                        sDay = dpPicker.getDayOfMonth().toString();
                    }
                    var month = dpPicker.getMonth()+1;

                    if(month<10){
                        sMonth = "0" + month.toString()
                    }else{
                        sMonth = month.toString();
                    }

                    sYear = dpPicker.getYear().toString();


                    if(tpPicker.getHour()<10){
                        sHour = "0" + tpPicker.getHour().toString()
                    }else{
                        sHour = tpPicker.getHour().toString();
                    }

                    if(tpPicker.getMinute()<10){
                        sMin = "0" + tpPicker.getMinute().toString()
                    }else{
                        sMin = tpPicker.getMinute().toString();
                    }

                    val sDueTimeStamp: String = sYear + sMonth + sDay + sHour + sMin


                    if(sDueTimeStamp<sCrTimeStamp){
                        Toast.makeText(
                            applicationContext,
                            "團購單截止時間不得早於現在時間", Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        textViewChuGroupDueDate = this.findViewById(R.id.textViewChuGroupDueDate) as TextView
                        textViewChuGroupDueDate.text = sYear + "年"+ sMonth + "月"+ sDay +"日 "+ sHour +":" + sMin
                        textViewChuGroupDueDate.tag= sYear+sMonth+sDay+sHour+sMin+"00000"

                        var linearLayoutDueDate = this.findViewById(R.id.linearLayoutDueDate) as LinearLayout
                        linearLayoutDueDate.setVisibility(View.VISIBLE)
                        /*
                        var editTextLocation = item.findViewById(R.id.editTextLocation) as EditText

                        if (TextUtils.isEmpty(editTextLocation.text))
                        {
                            editTextLocation.error = "地點不能為空白!"
                        }else {

                            Toast.makeText(
                                applicationContext,
                                "加入地點:" + editTextLocation.getText().toString(), Toast.LENGTH_SHORT
                            ).show()

                            alertDialog.dismiss()
                        }

                         */
                        alertDialog.dismiss()
                    }

                }
        }


        //--- Setup up Location Information
        val textViewAddLocation = findViewById<TextView>(R.id.textViewAddLocation)
        // set on-click listener for ImageView
        textViewAddLocation.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_location, null)

            var alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setPositiveButton("確定", null)
                .setNegativeButton("取消", null)
                .show()

            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    var editTextLocation = item.findViewById(R.id.editTextLocation) as EditText
                    val textViewLocationItemCount = findViewById<TextView>(R.id.textViewLocationItemCount)

                    if (TextUtils.isEmpty(editTextLocation.text.trim()))
                    {
                        editTextLocation.requestFocus()
                        editTextLocation.error = "地點不能為空白!"
                    }else {
                        var bFOund = false
                        locations.forEach {
                            if(it.equals(editTextLocation.getText().toString().trim())){
                                bFOund = true
                            }
                        }

                        if(bFOund){
                            editTextLocation.requestFocus()
                            editTextLocation.error = "地點不能重覆!"
                        }else{
                            locations.add(editTextLocation.getText().toString())
                            textViewLocationItemCount.setText(locations!!.size.toString() + " 項");
                            alertDialog.dismiss()
                        }

                    }
                }
        }



        val textViewLocationItemList = findViewById<LinearLayout>(R.id.textViewLocationItemList)
        textViewLocationItemList.setOnClickListener {
            getLocationListOfMenu(context)

        }

    }

    private fun getLocationListOfMenu(context:Context) {

        /*
        val array = arrayListOf<String>()
        locations.forEach {
            array.add(it)
        }

        val values = arrayOfNulls<String>(array.size)
        array.toArray(values)

        val bound = Bundle();
        bound.putString("TYPE", "LOCATION")
        bound.putStringArray("ItemListData", values)
        bound.putParcelable("USER_MENU", mFirebaseUserMenu)
        var I =  Intent(context, ActivityItemList::class.java)
        I.putExtras(bound);
        startActivityForResult(I,ACTION_ADD_MENU_LOCATION_LIST_REQUEST_CODE)*/
    }

    private fun setTimePickerInterval(timePicker: TimePicker) {
        try {
            val minutePicker = timePicker.findViewById(
                Resources.getSystem().getIdentifier(
                    "minute", "id", "android"
                )
            ) as NumberPicker
            minutePicker.minValue = 0
            minutePicker.maxValue = 60 / TIME_PICKER_INTERVAL - 1
            val displayedValues: MutableList<String> =
                ArrayList()
            var i = 0
            while (i < 60) {
                displayedValues.add(String.format("%02d", i))
                i += TIME_PICKER_INTERVAL
            }
            minutePicker.displayedValues = displayedValues.toTypedArray()
        } catch (e: Exception) {
           // Log.e(FragmentActivity.TAG, "Exception: $e")
        }
    }

    private fun createNewOrder() {
        var userMenuOrder: USER_MENU_ORDER = com.iew.fun2order.db.firebase.USER_MENU_ORDER()
        var iUserCnt : Int = 0
        /*
        var brandName: String? = "",
        var contentItems: String? = "",
        var createTime: String? = "",
        var dueTime: String? = "",
        var locations: String? = "",
        var menuNumber: String? = "",
        var orderNumber: String? = "",
        var orderOwnerID: String? = "",
        var orderOwnerName: String? = "",
        var orderStatus: String? = "",
        var orderTotalPrice: Int = 0,
        var orderTotalQuantity: Int = 0,
        var orderType: String? = ""
         */
        var mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {

        }
        //val sdf_Decode = SimpleDateFormat("yyyyMMddHHmmssSSS")
        //var receiveDateTime = sdf_Decode.parse(LocalDateTime.now().toString())
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())

        //Create USER_MENU_ORDER
        userMenuOrder.brandName = mMenuID
        userMenuOrder.createTime=timeStamp
        var textViewChuGroupDueDate = this.findViewById(R.id.textViewChuGroupDueDate) as TextView
        if(!textViewChuGroupDueDate.getText().trim().equals("")){
            userMenuOrder.dueTime=textViewChuGroupDueDate.tag.toString()
        }else{
            textViewChuGroupDueDate.requestFocus()
            Toast.makeText(this, "未指定團購截止日期!", Toast.LENGTH_SHORT).show()
            return
        }

        var editTextChuGroupDetailMsg = this.findViewById(R.id.editTextChuGroupDetailMsg) as EditText
        msChuGroupDetailMsg=editTextChuGroupDetailMsg.getText().toString()
        userMenuOrder.menuNumber= mUserMenu.menuNumber

        mUserMenu.menuItems?.filter { it.quantityLimitation != null }?.forEach()
        {
            userMenuOrder.limitedMenuItems!!.add(it)
        }

        userMenuOrder.locations = mUserMenu.locations

        userMenuOrder.orderNumber="M"+timeStamp
        userMenuOrder.orderOwnerID=mAuth.currentUser!!.uid
        userMenuOrder.orderOwnerName=mAuth.currentUser!!.displayName
        //userMenuOrder.menuNumber=mAuth.currentUser!!.uid+"-MENU-"+timeStamp
        userMenuOrder.orderStatus="READY"
        userMenuOrder.orderTotalPrice=0
        userMenuOrder.orderTotalQuantity=0
        userMenuOrder.orderType="M"
        userMenuOrder.storeInfo = mUserMenu.storeInfo

        //Create


        //--- 如果自己也要參加 把自己加進去 -------
        if(btnJoinGroupBuy!!.isChecked)
        {
            iUserCnt++
            val orderMember: ORDER_MEMBER = com.iew.fun2order.db.firebase.ORDER_MEMBER()
            orderMember.memberID      = mAuth.currentUser!!.uid
            orderMember.memberTokenID = com.iew.fun2order.MainActivity.localtokenID
            orderMember.orderOwnerID  = mAuth.currentUser!!.uid
            orderMember.orderContent.createTime = timeStamp
            orderMember.orderContent.itemFinalPrice = 0
            orderMember.orderContent.itemOwnerID = mAuth.currentUser!!.uid
            orderMember.orderContent.itemOwnerName = mAuth.currentUser!!.displayName
            orderMember.orderContent.itemQuantity = 0
            orderMember.orderContent.itemSinglePrice = 0
            orderMember.orderContent.location = ""
            orderMember.orderContent.orderNumber = userMenuOrder.orderNumber
            orderMember.orderContent.payCheckedFlag = false
            orderMember.orderContent.payNumber = 0
            orderMember.orderContent.payTime = ""
            orderMember.orderContent.replyStatus = MENU_ORDER_REPLY_STATUS_WAIT
            orderMember.orderContent.ostype = "Android"
            userMenuOrder.contentItems!!.add(orderMember)
        }

        List_Candidate.forEach {
            if(it.checked) {
                iUserCnt++
                val orderMember: ORDER_MEMBER = com.iew.fun2order.db.firebase.ORDER_MEMBER()
                orderMember.memberID = it.Name
                orderMember.memberTokenID=it.tokenid
                orderMember.orderOwnerID=mAuth.currentUser!!.uid
                orderMember.orderContent.createTime = timeStamp
                orderMember.orderContent.itemFinalPrice = 0
                orderMember.orderContent.itemOwnerID = it.Name
                orderMember.orderContent.itemOwnerName = it.displayName
                orderMember.orderContent.itemQuantity = 0
                orderMember.orderContent.itemSinglePrice = 0
                orderMember.orderContent.location = ""
                orderMember.orderContent.orderNumber = userMenuOrder.orderNumber
                orderMember.orderContent.payCheckedFlag = false
                orderMember.orderContent.payNumber = 0
                orderMember.orderContent.payTime = ""
                orderMember.orderContent.replyStatus = MENU_ORDER_REPLY_STATUS_WAIT
                orderMember.orderContent.ostype = it.ostype
                userMenuOrder.contentItems!!.add(orderMember)
            }
        }
        if(iUserCnt == 0 ){
            Toast.makeText(this, "未勾選參與團購人!", Toast.LENGTH_SHORT).show()
            return
        }


        mDatabase.child("USER_MENU_ORDER").child(mAuth.currentUser!!.uid).child(userMenuOrder.orderNumber.toString()).setValue(userMenuOrder)
            .addOnSuccessListener {
                sendFcmMessage(userMenuOrder)
                //Toast.makeText(this, "團購單建立成功!", Toast.LENGTH_SHORT).show()
                // Write was successful!
                val bundle = Bundle()
                bundle.putString("Result", "OK")
                val intent = Intent().putExtras(bundle)
                setResult(Activity.RESULT_OK, intent)
                finish()

            }
            .addOnFailureListener {
                // Write failed
                Toast.makeText(this, "團購單建立失敗!", Toast.LENGTH_SHORT).show()
            }
    }
    fun RecycleViewRefresh() {

        rcvGroup!!.adapter!!.notifyDataSetChanged()
        rcvGroupDetail!!.adapter!!.notifyDataSetChanged()
    }

    override fun onClick(sender: String,pos: Int, type:Int) {
        var position = pos
        var clicktype = type

        when(clicktype)
        {
            // Normal Click
            0 -> {
                if(sender == "Group") {
                    var click = listGroup[position] as ItemsLV_Group
                    if (click.Name != "新增群組") {
                        SelectGroupName = click.Name
                        SelectGroupID = click.GroupID
                        txtGroupInfo!!.setText(SelectGroupName + ":好友列表")
                        listGroupDetail.clear()
                        List_Candidate.clear()
                        //listGroupDetail.add(ItemsLV_GroupDetail("新增好友", "icon_add_group"))

                        var groupmemberlist = groupdetailDB.getMemberByGroupID(SelectGroupID)
                        groupmemberlist.forEach() {
                            listGroupDetail.add(ItemsLV_Favourite(it, "image_default_member",""))
                            List_Candidate.add(ItemsLV_Canditate(it, "image_default_member","","","",true))
                        }
                        RecycleViewRefresh()

                    } else {

                        //var I = Intent(context, ActivityAddGroup::class.java)
                        //startActivityForResult(I, ACTION_ADD_GROUP_REQUEST_CODE)
                    }
                }

                else if (sender == "Group_Detail")
                {
                    if (List_Candidate[position] is ItemsLV_Canditate) {

                        if(SelectGroupID != "") {
                            var groupmemberlist = groupdetailDB.getMemberByGroupID(SelectGroupID)
                            var friendlist = friendDB.getFriendslist()
                            var candidatelist = (friendlist - groupmemberlist)
/*
                            if (candidatelist.count() > 0) {
                                val array = arrayListOf<String>()
                                candidatelist.forEach()
                                {
                                    array.add(it.toString())
                                }
                                val bundle = Bundle()
                                bundle.putStringArrayList("Candidate", array)
                                var I = Intent(context, ActivityAddMember::class.java)
                                I.putExtras(bundle)
                                startActivityForResult(I, ACTION_ADD_MEMBER_REQUEST_CODE)
                            } else {
                                Toast.makeText(activity, "沒有可加入好友的清單", Toast.LENGTH_SHORT).show()
                            }

 */
                        }
                        else
                        {
                            Toast.makeText(this, "請先選擇Group", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            // Long Click
            1 -> {

                if(sender == "Group") {
                    /*
                    var Click = listGroup[position] as ItemsLV_Group
                    if (Click.Name != "新增群組") {

                        val ButtonAction = arrayOf("編輯群組", "刪除群組","取消")

                        val Alert = AlertDialog.Builder(context!!)
                            .setItems(ButtonAction, DialogInterface.OnClickListener { dialog, which ->
                                when (which) {
                                    0 -> {


                                        var mgroup = groupDB.getGroupByID(Click.GroupID)
                                        if (mgroup != null) {

                                            val bundle = Bundle()
                                            bundle.putString("GroupID", mgroup.groupid)
                                            bundle.putString("GroupName", mgroup.name)
                                            bundle.putString("GroupDesc", mgroup.desc)
                                            bundle.putByteArray("GroupImage", mgroup.image)

                                            var I = Intent(context, ActivityAddGroup::class.java)
                                            I.putExtras(bundle)
                                            startActivityForResult(
                                                I,
                                                ACTION_MODIFY_GROUP_REQUEST_CODE
                                            )
                                        }
                                    }
                                    1 -> {
                                        deleteGroup(Click.GroupID, Click.Name)
                                    }
                                    else -> {
                                        Toast.makeText(activity, "選取到取消", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })
                            .create()
                            .show()

                    }

                     */
                }
                else if (sender == "Group_Detail")
                {
                    /*
                    if (listGroupDetail[position] is ItemsLV_Favourite)
                    {
                        var group_id = SelectGroupID
                        var memberentity = listGroupDetail[position] as ItemsLV_Favourite
                        var mamber_name = memberentity.Name
                        deleteGroupFriend(group_id,mamber_name)
                    }

                     */
                }
            }
        }
    }

    override fun onChanged(Position:Int, checked:Boolean) {

        List_Candidate[Position].checked = checked
        rcvGroupDetail!!.adapter!!.notifyDataSetChanged()
        //Toast.makeText(this, Position.toString() + "is" + checked.toString(), Toast.LENGTH_SHORT).show()
    }
    /*
    override fun onChanged(Position:Int, checked:Boolean) {

        Toast.makeText(this, Position.toString() + "is" + checked.toString(), Toast.LENGTH_SHORT).show()
    }

     */

    private fun sendFcmMessage(userMenuOrder: USER_MENU_ORDER) {
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
        val dbContext: MemoryDatabase = MemoryDatabase(this)
        val friendImageDB: friendImageDAO = dbContext.friendImagedao()

        ProgressDialogUtil.showProgressDialog(this,"處理中");
        userMenuOrder.contentItems!!.forEach() {

            val orderMember = it as ORDER_MEMBER
            val topic = orderMember.memberTokenID
            val notification = JSONObject()
            val notificationHeader = JSONObject()
            val notificationBody = JSONObject()

            var body = if (msChuGroupDetailMsg == "") {
                "由 ${userMenuOrder.orderOwnerName} 發起的團購邀請，請點擊通知以查看詳細資訊。"
            } else {
                "由 ${userMenuOrder.orderOwnerName} 的團購邀請 : \n$msChuGroupDetailMsg。"
            }

            val self = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            if(orderMember.memberID == self)
            {
                body = "自己並參與的團購單"
            }

            notificationHeader.put("title", "團購邀請")
            notificationHeader.put("body", body ?: "")   //Enter your notification message

            notificationBody.put("messageID", "")      //Enter
            notificationBody.put("messageTitle", "團購邀請")   //Enter
            notificationBody.put("messageBody", "來自 ${userMenuOrder.orderOwnerName} 的團購邀請, 請點擊通知以查看詳細資訊.")    //Enter
            notificationBody.put("notificationType",NOTIFICATION_TYPE_ACTION_JOIN_ORDER )   //Enter
            notificationBody.put("receiveTime", timeStamp)   //Enter
            notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)   //Enter
            notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)   //Enter
            notificationBody.put("menuNumber", userMenuOrder.menuNumber)   //Enter
            notificationBody.put("orderNumber", userMenuOrder.orderNumber)   //Enter
            notificationBody.put("dueTime",    userMenuOrder.dueTime ?: "")   //Enter  20200515 addition
            notificationBody.put("brandName", userMenuOrder.brandName)   //Enter
            notificationBody.put("attendedMemberCount", userMenuOrder.contentItems!!.count().toString())   //Enter
            notificationBody.put("messageDetail", msChuGroupDetailMsg?: "")   //Enter
            notificationBody.put("isRead", "N")   //Enter
            notificationBody.put("replyStatus", "")   //Enter
            notificationBody.put("replyTime", "")   //Enter

            // your notification message
            notification.put("to", topic)
            notification.put("notification", notificationHeader)
            notification.put("data", notificationBody)

            if(orderMember.orderContent.ostype ?: "iOS" =="Android")
            {
                notification.remove("notification")
            }

            Thread.sleep(100)
            com.iew.fun2order.MainActivity.sendFirebaseNotification(notification)
        }
        ProgressDialogUtil.dismiss()
    }
}