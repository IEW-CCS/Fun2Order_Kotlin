package com.iew.fun2order.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.MainActivity
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.ORDER_MEMBER
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.ui.my_setup.ItemsLV_Canditate
import com.iew.fun2order.utility.DATATIMEFORMAT_NORMAL
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_WAIT
import com.iew.fun2order.utility.NOTIFICATION_TYPE_ACTION_JOIN_ORDER
import com.iew.fun2order.utility.ORDER_STATUS_INIT
import kotlinx.android.synthetic.main.activity_setup_detail_order_next.*
import kotlinx.android.synthetic.main.alert_date_time_picker.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ActivitySetupNewOrderNext : AppCompatActivity() {

    private val TIME_PICKER_INTERVAL = 15
    private var locations: MutableList<String> = mutableListOf()
    private val ACTION_ADD_MENU_LOCATION_LIST_REQUEST_CODE = 400

    private  lateinit var mUserMenu : USER_MENU
    private  var inviteTokenList: ArrayList<ItemsLV_Canditate>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_detail_order_next)

        supportActionBar?.title = "發出團購邀請"
        locations.clear()

        mUserMenu = intent?.extras?.get("USER_MENU") as USER_MENU
        inviteTokenList = intent.extras?.getParcelableArrayList<ItemsLV_Canditate>("INVITE_TOKEN_ID")

        brandName.text = mUserMenu.brandName ?: ""

        //----  設定團購截止時間 ------
        textViewSetupDueDate.setOnClickListener {

            val sCrTimeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())
            val item = LayoutInflater.from(this).inflate(R.layout.alert_date_time_picker, null)

            val mTabHost = item.tab_host
            mTabHost.setup()
            val mDateTab: TabHost.TabSpec = mTabHost.newTabSpec("date")
            mDateTab.setIndicator("日期");
            mDateTab.setContent(R.id.date_content);
            mTabHost.addTab(mDateTab)

            val mTimeTab: TabHost.TabSpec = mTabHost.newTabSpec("time")
            mTimeTab.setIndicator("時間");
            mTimeTab.setContent(R.id.time_content);
            mTabHost.addTab(mTimeTab)

            //-----  Change 屬性 -----
            for (i in 0 until mTabHost.tabWidget.childCount) {
                val tv = mTabHost.tabWidget.getChildAt(i).findViewById(android.R.id.title) as TextView //Unselected Tabs
                tv.textSize = 18F
                tv.typeface = Typeface.DEFAULT_BOLD;
            }


            val picker = item.findViewById(R.id.tpPicker) as TimePicker
            picker.setIs24HourView(true)

            setTimePickerInterval(picker);
            val alertDialog = AlertDialog.Builder(this)
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

                    //-- 處理年月日---
                    sDay = if (dpPicker.dayOfMonth < 10) {
                        "0" + dpPicker.dayOfMonth.toString()
                    } else {
                        dpPicker.dayOfMonth.toString();
                    }

                    val month = dpPicker.month + 1;
                    sMonth = if (month < 10) {
                        "0$month"
                    } else {
                        month.toString();
                    }

                    sYear = dpPicker.year.toString();

                    //-------處理時分秒 ----
                    sHour = if (tpPicker.hour < 10) {
                        "0" + tpPicker.hour.toString()
                    } else {
                        tpPicker.hour.toString();
                    }

                    sMin = if (tpPicker.minute < 10) {
                        "0" + tpPicker.minute.toString()
                    } else {
                        tpPicker.minute.toString();
                    }

                    val sDueTimeStamp: String = sYear + sMonth + sDay + sHour + sMin

                    if(sDueTimeStamp<sCrTimeStamp){
                        Toast.makeText(
                            applicationContext,
                            "團購單截止時間不得早於現在時間", Toast.LENGTH_SHORT
                        ).show()
                    }else{

                        val tmpDueTimeText = sYear + "年"+ sMonth + "月"+ sDay +"日 "+ sHour +":" + sMin
                        textViewChuGroupDueDate.text = tmpDueTimeText
                        val tmpDueTimeTag = sYear+sMonth+sDay+sHour+sMin+"00000"
                        textViewChuGroupDueDate.tag = tmpDueTimeTag

                        val linearLayoutDueDate = this.findViewById(R.id.linearLayoutDueDate) as LinearLayout
                        linearLayoutDueDate.visibility = View.VISIBLE
                        alertDialog.dismiss()
                    }
                }
        }

        //---  設定地點資訊 -----
        textViewAddLocation.setOnClickListener {
            val item = LayoutInflater.from(this).inflate(R.layout.alert_input_location, null)
            val alertDialog = AlertDialog.Builder(this)
                .setView(item)
                .setPositiveButton("確定", null)
                .setNegativeButton("取消", null)
                .show()

            alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val editTextLocation = item.findViewById(R.id.editTextLocation) as EditText
                    val textViewLocationItemCount = findViewById<TextView>(R.id.textViewLocationItemCount)
                    if (TextUtils.isEmpty(editTextLocation.text.trim()))
                    {
                        editTextLocation.requestFocus()
                        editTextLocation.error = "地點不能為空白!"

                    }else {
                        var bFOund = false
                        locations.forEach {
                            if(it.equals(editTextLocation.text.toString().trim())){
                                bFOund = true
                            }
                        }

                        if(bFOund){
                            editTextLocation.requestFocus()
                            editTextLocation.error = "地點不能重覆!"
                        }else{
                            locations.add(editTextLocation.text.toString())
                            val tempLocationItemCountText = "${locations.size.toString()} 項"
                            textViewLocationItemCount.text = tempLocationItemCountText
                            alertDialog.dismiss()
                        }
                    }
                }
        }

        //--- 檢視地點資訊 -----
        textViewLocationItemList.setOnClickListener {
            getLocationListOfMenu(this)
        }


        //---- SubMit 送出邀請------
        btnSubmit.setOnClickListener {


            createNewOrder()

            /* //------ 測試用
            val bundle = Bundle()
            val intent = Intent(this, ActivityJoinOrderDetail::class.java)
            intent.putExtras(bundle)
            startActivity(intent)*/

        }


    }

    private fun getLocationListOfMenu(context: Context) {
        val array = arrayListOf<String>()
        locations.forEach {
            array.add(it)
        }
        
        val values = arrayOfNulls<String>(array.size)
        array.toArray(values)
        val bound = Bundle();
        bound.putString("TYPE", "LOCATION")
        bound.putStringArray("ItemListData", values)
        val I =  Intent(context, ActivityItemList::class.java)
        I.putExtras(bound);
        startActivityForResult(I,ACTION_ADD_MENU_LOCATION_LIST_REQUEST_CODE)
    }


    private fun setTimePickerInterval(timePicker: TimePicker) {
        try {
            val minutePicker = timePicker.findViewById(Resources.getSystem().getIdentifier("minute", "id", "android")) as NumberPicker
            minutePicker.minValue = 0
            minutePicker.maxValue = 60 / TIME_PICKER_INTERVAL - 1
            val displayedValues: MutableList<String> = ArrayList()
            var i = 0
            while (i < 60) {
                displayedValues.add(String.format("%02d", i))
                i += TIME_PICKER_INTERVAL
            }
            minutePicker.displayedValues = displayedValues.toTypedArray()
        } catch (e: Exception) {
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ACTION_ADD_MENU_LOCATION_LIST_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    var locationList = data.extras.get("Location") as ArrayList<String>
                    locations = locationList.toMutableList()
                    textViewLocationItemCount.text = locations.size.toString() + " 項";
                }
            }
        }
    }


    private fun createNewOrder() {

        var iUserCnt : Int = 0
        val userMenuOrder: USER_MENU_ORDER = USER_MENU_ORDER()
        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())
        val detailMessage = editTextGroupDetailMsg.text.toString()

        if(textViewChuGroupDueDate.text.trim() == ""){
            textViewChuGroupDueDate.requestFocus()
            Toast.makeText(this, "未指定團購截止日期!", Toast.LENGTH_SHORT).show()
            return
        }

        if(mUserMenu == null)
        {
            val notifyAlert = AlertDialog.Builder(this).create()
            notifyAlert.setTitle("錯誤通知")
            notifyAlert.setMessage("品牌資訊有誤!! 無法產生訂單。")
            notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK")
            { _, _ -> }
            notifyAlert.show()
            return
        }


        //Create USER_MENU_ORDER

        userMenuOrder.brandName =  mUserMenu.brandName
        userMenuOrder.createTime= timeStamp
        userMenuOrder.menuNumber = mUserMenu.menuNumber

        userMenuOrder.limitedMenuItems!!.clear()
        mUserMenu.menuItems?.filter { it.quantityLimitation != null }?.forEach()
        {
            userMenuOrder.limitedMenuItems!!.add(it)
        }

        userMenuOrder.dueTime = textViewChuGroupDueDate.tag.toString()
        userMenuOrder.locations = locations //mUserMenu.locations
        userMenuOrder.orderNumber  = "M$timeStamp"
        userMenuOrder.orderOwnerID = FirebaseAuth.getInstance().currentUser!!.uid
        userMenuOrder.orderOwnerName = FirebaseAuth.getInstance().currentUser!!.displayName
        userMenuOrder.orderStatus = ORDER_STATUS_INIT
        userMenuOrder.orderTotalPrice= 0
        userMenuOrder.orderTotalQuantity= 0
        userMenuOrder.orderType="M"
        userMenuOrder.needContactInfoFlag = checkContactInfo.isChecked


        //Create
        //--- 如果自己也要參加 把自己加進去 -------
        if(checkBoxJoinGroupBuy.isChecked)
        {
            iUserCnt++
            val orderMember: ORDER_MEMBER = com.iew.fun2order.db.firebase.ORDER_MEMBER()
            orderMember.memberID      = FirebaseAuth.getInstance().currentUser!!.uid
            orderMember.memberTokenID = com.iew.fun2order.MainActivity.localtokenID
            orderMember.orderOwnerID  = FirebaseAuth.getInstance().currentUser!!.uid
            orderMember.orderContent.createTime = timeStamp
            orderMember.orderContent.itemFinalPrice = 0
            orderMember.orderContent.itemOwnerID = FirebaseAuth.getInstance().currentUser!!.uid
            orderMember.orderContent.itemOwnerName = FirebaseAuth.getInstance().currentUser!!.displayName
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

        inviteTokenList?.forEach {
                iUserCnt++
                val orderMember: ORDER_MEMBER = com.iew.fun2order.db.firebase.ORDER_MEMBER()
                orderMember.memberID = it.Name
                orderMember.memberTokenID=it.tokenid
                orderMember.orderOwnerID = FirebaseAuth.getInstance().currentUser!!.uid
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

        if(iUserCnt == 0 ){
            Toast.makeText(this, "未勾選參與團購人!", Toast.LENGTH_SHORT).show()
            return
        }

        Firebase.database.reference.child("USER_MENU_ORDER").child(FirebaseAuth.getInstance().currentUser!!.uid).child(userMenuOrder.orderNumber.toString()).setValue(userMenuOrder)
            .addOnSuccessListener {
                sendOrderReqFcmMessage(userMenuOrder, detailMessage)

                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                intent.setClass(this, MainActivity::class.java)
                startActivity(intent)
                this.finish()

            }
            .addOnFailureListener {
                Toast.makeText(this, "團購單建立失敗!", Toast.LENGTH_SHORT).show()
            }
    }


    private fun sendMessageNewOrderFCMWithIOS (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, timeStamp: String, msChuGroupDetailMsg:String)
    {
        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()

        var title = "團購邀請"
        var body = if (msChuGroupDetailMsg == "") {
            "由 ${userMenuOrder.orderOwnerName} 發起的團購邀請，請點擊通知以查看詳細資訊。"
        } else {
            "由 ${userMenuOrder.orderOwnerName} 的團購邀請 : \n$msChuGroupDetailMsg。"
        }

        val self = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        notificationHeader.put("title", title)
        notificationHeader.put("body", body)   //Enter your notification message

        notificationBody.put("messageID", "")      //Enter
        notificationBody.put("messageTitle", title)   //Enter
        notificationBody.put("messageBody", body)    //Enter
        notificationBody.put("notificationType", NOTIFICATION_TYPE_ACTION_JOIN_ORDER )   //Enter
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
        notification.put("registration_ids", JSONArray(notifyList))
        notification.put("notification", notificationHeader)
        notification.put("data", notificationBody)


        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)
    }

    private fun sendMessageNewOrderFCMWithAndroid (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, timeStamp: String, msChuGroupDetailMsg:String)
    {
        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()

        var title = "團購邀請"
        var body = if (msChuGroupDetailMsg == "") {
            "由 ${userMenuOrder.orderOwnerName} 發起的團購邀請，請點擊通知以查看詳細資訊。"
        } else {
            "由 ${userMenuOrder.orderOwnerName} 的團購邀請 : \n$msChuGroupDetailMsg。"
        }

        val self = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        notificationHeader.put("title", title)
        notificationHeader.put("body", body)   //Enter your notification message

        notificationBody.put("messageID", "")      //Enter
        notificationBody.put("messageTitle", title)   //Enter
        notificationBody.put("messageBody", body)    //Enter
        notificationBody.put("notificationType", NOTIFICATION_TYPE_ACTION_JOIN_ORDER )   //Enter
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
        notification.put("registration_ids", JSONArray(notifyList))
        notification.put("data", notificationBody)


        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)
    }


    private fun sendOrderReqFcmMessage(userMenuOrder: USER_MENU_ORDER, msChuGroupDetailMsg:String ) {
        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())

        ProgressDialogUtil.showProgressDialog(this,"處理中");

        //-------Notification List 拆開成Android and IOS -----
        val iosType =  userMenuOrder.contentItems?.filter { it -> (it.orderContent.ostype ?: "iOS"  == "iOS" || it.orderContent.ostype ?: "iOS"  == "") && it.memberTokenID != ""}
        val androidType =  userMenuOrder.contentItems?.filter { it -> it.orderContent.ostype ?: "iOS"  == "Android" && it.memberTokenID != "" }
        val iosTypeList = iosType?.map { it -> it.memberTokenID !!}
        val androidTypeList = androidType?.map { it -> it.memberTokenID!! }

        sendMessageNewOrderFCMWithIOS (iosTypeList!!,userMenuOrder,  timeStamp, msChuGroupDetailMsg)
        sendMessageNewOrderFCMWithAndroid (androidTypeList!!,userMenuOrder, timeStamp, msChuGroupDetailMsg)

        ProgressDialogUtil.dismiss()
    }
}