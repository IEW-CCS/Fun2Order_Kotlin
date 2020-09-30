package com.iew.fun2order.brand.location

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.MainActivity
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.firebase.*
import com.iew.fun2order.ui.shop.ActivitySetupDetailOrder
import com.iew.fun2order.utility.*
import info.hoang8f.android.segmented.SegmentedGroup
import kotlinx.android.synthetic.main.activity_cowork_order_info.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class ActivityCoworkOrderInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cowork_order_info)
        supportActionBar?.hide()

        val brandName = intent.extras?.getString("BRAND_NAME")
        val menuNumber = intent.extras?.getString("BRAND_MENU_NUMBER")

        val storeName = intent.extras?.getString("STORE_NAME") ?: ""
        val storeAddress = intent.extras?.getString("STORE_ADDRESS") ?: ""
        val storePhoneNumber = intent.extras?.getString("STORE_PHONE_NUMBER") ?: ""
        val selfOrder = intent.extras?.getBoolean("SELF_ORDER") ?: false
        val deliveryService =  intent.extras?.getBoolean("DELIVERY_SERVICE") ?: false

        var deliveryType = DELIVERY_TYPE_TAKEOUT

        val dbContext = AppDatabase(this)
        val profileDB = dbContext.userprofiledao()
        val entity = profileDB.getProfileByID(FirebaseAuth.getInstance().currentUser!!.uid.toString())

        if(entity!= null) {
            editPhoneNumber.text = Editable.Factory.getInstance().newEditable(entity.phoneNumber ?: "")
        }

        if(selfOrder)
        {
            btnNextAction.text = "訂購"
        }
        else
        {
            btnNextAction.text = "下一步"
        }

        textBrandStoreName.text = storeName

        editContactName.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
            }
            false
        })

        editContactAddress.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
            }
            false
        })

        if (deliveryService)
        {
            val mInflater: LayoutInflater? = LayoutInflater.from(this);
            addRadioButton(mInflater!!, mSegmentedGroup, "外送")

        }

        //---- User Select Location  -----
        mSegmentedGroup!!.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            when (radioButton.text) {
                "外帶" -> {
                    groupContactAddress.visibility = View.GONE
                    deliveryType = DELIVERY_TYPE_TAKEOUT
                }
                "外送" -> {
                    groupContactAddress.visibility = View.VISIBLE
                    deliveryType = DELIVERY_TYPE_DELIVERY
                }

            }

        })


        btnNextAction.setOnClickListener {

            val currentDay = SimpleDateFormat("yyyyMMdd").format(Date()).toString()
            val sHour = String.format("%02d",tpPicker.hour)
            val sMin = String.format("%02d",tpPicker.minute)
            val pickUptime = "${currentDay}${sHour}${sMin}00000"

            var menuOrderDeliveryInfo = MenuOrderDeliveryInformation()
            menuOrderDeliveryInfo.contactName  = editContactName.text.toString()
            menuOrderDeliveryInfo.contactPhoneNumber  = editPhoneNumber.text.toString()
            menuOrderDeliveryInfo.deliveryTime  = DATATIMEFORMAT_NORMAL.format(Date())
            menuOrderDeliveryInfo.deliveryType = deliveryType
            menuOrderDeliveryInfo.deliveryAddress  = editContactAddress.text.toString()


            if(selfOrder )
            {
                createSelfOrder(brandName!!, menuNumber!!, storeName,storeAddress,storePhoneNumber, menuOrderDeliveryInfo)
            }
            else
            {

                val bundle = Bundle()
                bundle.putString("BRAND_NAME", brandName)
                bundle.putString("BRAND_MENU_NUMBER", menuNumber)
                bundle.putString("STORE_NAME", storeName)
                bundle.putString("STORE_ADDRESS", storeAddress)
                bundle.putString("STORE_PHONE_NUMBER", storePhoneNumber)
                bundle.putParcelable("MENU_DELIVERY_INFO", menuOrderDeliveryInfo)
                bundle.putBoolean("COWORK", true)
                bundle.putBoolean("GROUP_ORDER", true)

                var intent = Intent(this, ActivitySetupDetailOrder::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            }

        }
    }

    private fun addRadioButton(inflater: LayoutInflater, group: SegmentedGroup, btnName: String) {
        val radioButton = inflater.inflate(R.layout.radio_button_item, null) as RadioButton
        radioButton.text = btnName

        val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.weight = 1.0f

        radioButton.width = 0
        radioButton.layoutParams = p

        group.addView(radioButton)
        group.updateBackground()

    }

    private fun createSelfOrder(brandName: String, brandMenuNumber: String, storeName: String,storeAddress: String,storePhoneNumber: String, orderDeliveryInfo :MenuOrderDeliveryInformation)
    {

        val userMenuOrder: USER_MENU_ORDER = USER_MENU_ORDER()
        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())

        val dueDayTime = Calendar.getInstance()
        //---- Default 一天的時間
        dueDayTime.add(Calendar.DAY_OF_YEAR,1)

        val dueTimeStamp = DATATIMEFORMAT_NORMAL.format(dueDayTime.time)

        if (brandName == null || brandMenuNumber == null) {
            val notifyAlert = AlertDialog.Builder(this).create()
            notifyAlert.setTitle("錯誤通知")
            notifyAlert.setMessage("品牌資訊有誤!! 無法產生訂單。")
            notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK")
            { _, _ -> }
            notifyAlert.show()
            return
        }

        //Create USER_MENU_ORDER
        userMenuOrder.brandName = brandName
        userMenuOrder.createTime = timeStamp
        userMenuOrder.menuNumber = brandMenuNumber
        userMenuOrder.dueTime = dueTimeStamp
        userMenuOrder.locations = null
        userMenuOrder.orderNumber = "M$timeStamp"
        userMenuOrder.orderOwnerID = FirebaseAuth.getInstance().currentUser!!.uid
        userMenuOrder.orderOwnerName = FirebaseAuth.getInstance().currentUser!!.displayName
        userMenuOrder.orderStatus = ORDER_STATUS_INIT
        userMenuOrder.orderTotalPrice = 0
        userMenuOrder.orderTotalQuantity = 0
        userMenuOrder.orderType = "F"
        userMenuOrder.needContactInfoFlag = false

        //-------組合BrandInfo資訊 ------
        userMenuOrder.deliveryInfo    = orderDeliveryInfo
        userMenuOrder.coworkBrandFlag = true
        userMenuOrder.groupOrderFlag  = false


        val storeInfo : STORE_INFO = STORE_INFO()
        storeInfo.storeName = storeName ?: ""
        storeInfo.storeAddress = storeAddress ?: ""
        storeInfo.storePhoneNumber = storePhoneNumber ?: ""
        userMenuOrder.storeInfo= storeInfo

        //----- 自己訂購只填寫自己的資訊 -----
        val orderMember: ORDER_MEMBER = com.iew.fun2order.db.firebase.ORDER_MEMBER()
        orderMember.memberID = FirebaseAuth.getInstance().currentUser!!.uid
        orderMember.memberTokenID = com.iew.fun2order.MainActivity.localtokenID
        orderMember.orderOwnerID = FirebaseAuth.getInstance().currentUser!!.uid
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

        Firebase.database.reference.child("USER_MENU_ORDER")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(userMenuOrder.orderNumber.toString()).setValue(userMenuOrder)
            .addOnSuccessListener {
                sendOrderReqFcmMessage (userMenuOrder, "自己訂購發起的訂單")
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                intent.setClass(this, MainActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "團購單建立失敗!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendOrderReqFcmMessage(userMenuOrder: USER_MENU_ORDER, msChuGroupDetailMsg:String ) {
        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())
        ProgressDialogUtil.showProgressDialog(this,"處理中");
        val androidType =  userMenuOrder.contentItems?.filter { it -> it.orderContent.ostype ?: "iOS"  == "Android" && it.memberTokenID != ""}
        val androidTypeList = androidType?.map { it -> it.memberTokenID!! }
        sendMessageDetailOrderFCMWithAndroid (androidTypeList!!,userMenuOrder, timeStamp, msChuGroupDetailMsg)
        ProgressDialogUtil.dismiss()
    }

    private fun sendMessageDetailOrderFCMWithAndroid (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, timeStamp: String, msChuGroupDetailMsg:String)
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
}