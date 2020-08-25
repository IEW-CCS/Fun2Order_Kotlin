package com.iew.fun2order.order

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.net.ParseException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.db.dao.friendImageDAO
import com.iew.fun2order.db.database.MemoryDatabase
import com.iew.fun2order.db.firebase.ORDER_MEMBER
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.utility.*
import kotlinx.android.synthetic.main.alert_date_time_picker.view.*
import kotlinx.android.synthetic.main.bottom_sheet_duetime_notice.view.*
import kotlinx.android.synthetic.main.bottom_sheet_duetime_notice.view.buttonSubmit
import kotlinx.android.synthetic.main.bottom_sheet_shipping_notice.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RootFragmentOrderStatus(var _menuorder: USER_MENU_ORDER) : Fragment() {


    private lateinit var menuorder: USER_MENU_ORDER

    private lateinit var txtOrderBrand: TextView
    private lateinit var txtOrderStartTime: TextView
    private lateinit var txtOrderEndTime: TextView
    private lateinit var txtOrderJoinCount: TextView
    private lateinit var pieChart: com.github.mikephil.charting.charts.PieChart

    private lateinit var layoutRefresh: LinearLayout
    private lateinit var layoutCallable: LinearLayout
    private lateinit var textCallable: TextView
    private lateinit var layoutNotify: LinearLayout

    private lateinit var broadcast: LocalBroadcastManager

    private var floatOrderExpected: Float = 0.0F
    private var floatOrderWait: Float = 0.0F
    private var floatOrderJoined: Float = 0.0F
    private var floatOrderNotJoin: Float = 0.0F

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater!!.inflate(R.layout.fragment_orderstatus, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menuorder = _menuorder.copy()


    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(messageReceiver_maintain, IntentFilter("UpdateMessage"))

    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver_maintain)

    }

    private var messageReceiver_maintain = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val  tmpmunuOrder = p1?.getParcelableExtra<USER_MENU_ORDER>("userMenuOrder")
            if(tmpmunuOrder!= null) {
                menuorder = tmpmunuOrder.copy()
                checkOrderStatus()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            pieChart = it.findViewById<com.github.mikephil.charting.charts.PieChart>(
                R.id.pieChart
            )
            txtOrderBrand = it.findViewById<TextView>(R.id.orderStatustitle)
            txtOrderEndTime = it.findViewById<TextView>(R.id.orderStatusDueTime)
            txtOrderStartTime = it.findViewById<TextView>(R.id.orderStatusStartTime)
            txtOrderJoinCount = it.findViewById<TextView>(R.id.orderStatusJoinCount)

            layoutRefresh = it.findViewById<LinearLayout>(R.id.orderStatusReflash)
            layoutCallable = it.findViewById<LinearLayout>(R.id.orderStatusCallable)
            layoutNotify = it.findViewById<LinearLayout>(R.id.orderStatusNotify)
            textCallable = it.findViewById<TextView>(R.id.orderStatusCallableText)

            broadcast = LocalBroadcastManager.getInstance(it)
        }

        layoutRefresh.setOnClickListener{
            refreshMenuOrder()
        }

        layoutCallable.setOnClickListener{
            val dialog = BottomSheetDialog(requireContext())
            val bottomSheet = layoutInflater.inflate(R.layout.bottom_sheet_duetime_notice, null)
            bottomSheet.buttonCallable.setOnClickListener {
                dialog.dismiss()
                sendCallableFcmMessage(menuorder)
                showNoticeAlert("訊息","已經對尚未回覆者發出催訂通知")
            }
            bottomSheet.buttonChangeduetime.setOnClickListener {
                dialog.dismiss()
                changeDueTimeRequest(menuorder)
            }
            bottomSheet.buttonSubmit.setOnClickListener { dialog.dismiss() }
            dialog.setContentView(bottomSheet)
            dialog.show()
        }

        layoutNotify.setOnClickListener{

            val dialog = BottomSheetDialog(requireContext())
            val bottomSheet = layoutInflater.inflate(R.layout.bottom_sheet_shipping_notice, null)
            bottomSheet.buttonNormalNotice.setOnClickListener {
                dialog.dismiss()
                sendMessageNotice(menuorder)

            }
            bottomSheet.buttonShippingNotice.setOnClickListener {
                dialog.dismiss()
                sendShippingNotice(menuorder)
            }
            bottomSheet.buttonSubmit.setOnClickListener { dialog.dismiss() }
            dialog.setContentView(bottomSheet)
            dialog.show()

        }

        checkOrderStatus()
    }

    private fun checkOrderStatus() {
        if (menuorder != null) {
            val timeExpired = checkDueTime(menuorder.dueTime)
            layoutCallable.isClickable = true
            layoutCallable.isEnabled = true
            if (timeExpired) {
                //--- 關閉催訂通知 -----
                layoutCallable.isClickable = false
                layoutCallable.isEnabled = false
                textCallable.setTextColor(Color.GRAY)
                txtOrderEndTime.setTextColor(requireContext().resources.getColor(R.color.red))
            }
        }
        setOrderInfo()
    }

    fun setOrderInfo() {

        txtOrderBrand.text = menuorder.brandName
        txtOrderJoinCount.text = menuorder.contentItems?.count().toString()
        txtOrderStartTime.text = ""

        val sdfDecode = DATATIMEFORMAT_NORMAL
        val sdfEncode = DATATIMEFORMAT_CHINESE_TYPE1

        if(menuorder.createTime!= "") {
            val startDateTime = sdfDecode.parse(menuorder.createTime)
            val formatStartDatetime = sdfEncode.format(startDateTime).toString()
            txtOrderStartTime.text = formatStartDatetime
        }

        if(menuorder.dueTime!= "") {
            val dueDateTime = sdfDecode.parse(menuorder.dueTime)
            val formatDueDatetime = sdfEncode.format(dueDateTime).toString()
            txtOrderEndTime.text =formatDueDatetime
        }
        else
        {
            txtOrderEndTime.setTextColor(requireContext().resources.getColor(R.color.red))
            txtOrderEndTime.text = "無逾期時間"
        }

        val orderExpire = menuorder.contentItems?.filter { it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_EXPIRE }?.count() ?: 0
        val orderWait   = menuorder.contentItems?.filter { it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_WAIT }?.count()   ?: 0
        val orderJoin   = menuorder.contentItems?.filter { it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT }?.count()  ?: 0
        val orderReject = menuorder.contentItems?.filter { it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_REJECT }?.count()  ?: 0

        floatOrderExpected = orderExpire.toFloat()
        floatOrderWait = orderWait.toFloat()
        floatOrderJoined = orderJoin.toFloat()
        floatOrderNotJoin = orderReject.toFloat()


        val noOfEmp = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        if (floatOrderExpected > 0.0F) {
            noOfEmp.add(PieEntry(floatOrderExpected, "逾期未回覆"))
            colors.add(resources.getColor(R.color.SUMMARY_EXPIRE_COLOR))
        }

        if (floatOrderWait > 0.0F) {
            noOfEmp.add(PieEntry(floatOrderWait, "等待回覆"))
            colors.add(resources.getColor(R.color.SUMMARY_WAIT_COLOR))
        }

        if (floatOrderJoined > 0.0F) {
            noOfEmp.add(PieEntry(floatOrderJoined, "已回覆"))
            colors.add(resources.getColor(R.color.SUMMARY_ACCEPT_COLOR))
        }

        if (floatOrderNotJoin > 0.0F) {
            noOfEmp.add(PieEntry(floatOrderNotJoin, "不參加"))
            colors.add(resources.getColor(R.color.SUMMARY_REJECT_COLOR))
        }


        val dataSet = PieDataSet(noOfEmp, "")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0F, 40F)
        dataSet.selectionShift = 10f
        dataSet.colors = colors
        //dataSet.setColors(*ColorTemplate.COLORFUL_COLORS)

        val data = PieData(dataSet)

        data.setValueTextSize(18f)
        data.setValueTextColor(Color.WHITE)
        data.setValueFormatter(DefaultValueFormatter(0))

        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
        pieChart.setDrawEntryLabels(false)

        pieChart.animateXY(500, 500)
        pieChart.description.text = ""

        pieChart.legend.textSize = 9f
        pieChart.legend.formSize = 9f
        pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER

    }



    private fun refreshMenuOrder()
    {
        ProgressDialogUtil.showProgressDialog(context);
        val userMenuOrderPath = "USER_MENU_ORDER/${FirebaseAuth.getInstance().currentUser!!.uid.toString()}/${_menuorder.orderNumber}"
        val database = Firebase.database
        val myRef = database.getReference(userMenuOrderPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val menuOrder = dataSnapshot.getValue(USER_MENU_ORDER::class.java)
                if (menuOrder != null) {
                    val timeExpired = checkDueTime(menuorder.dueTime)
                    layoutCallable.isClickable = true
                    layoutCallable.isEnabled = true
                    if (timeExpired) {
                        layoutCallable.isClickable = false
                        layoutCallable.isEnabled = false
                        textCallable.setTextColor(Color.GRAY)
                        txtOrderEndTime.setTextColor(requireContext().resources.getColor(R.color.red))
                        var checkContentExpireStatus = false
                        menuOrder.contentItems?.forEach {
                            if (it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_WAIT) {
                                it.orderContent.replyStatus = MENU_ORDER_REPLY_STATUS_EXPIRE
                                checkContentExpireStatus = true
                            }
                        }
                        if (checkContentExpireStatus) {
                            myRef.setValue(menuOrder)
                        }
                    }
                    menuorder = menuOrder.copy()
                    setOrderInfo()
                    sendMenuOrderRefresh(menuOrder.copy())   //廣播更新資料通知其他fragment
                    ProgressDialogUtil.dismiss();
                }
                else
                {
                    ProgressDialogUtil.dismiss();
                }
            }
            override fun onCancelled(error: DatabaseError) {
                ProgressDialogUtil.dismiss();
            }
        })
    }

    private fun checkDueTime(dueDatetime: String?): Boolean {
        return if(dueDatetime == null) {
            false
        } else {
            try {
                val beginTime: Date = DATATIMEFORMAT_NORMAL.parse(dueDatetime)
                val endTime: Date = Date()
                (endTime.time - beginTime.time) > 0
            } catch (e: ParseException) {
                false
            }
        }
    }


    private fun sendMessageNotice(userMenuOrder: USER_MENU_ORDER)
    {
        val alert = AlertDialog.Builder(requireContext())
        var editTextNote: EditText? = null
        with (alert) {
            setTitle("請輸入訊息")
            editTextNote = EditText(requireContext())
            setPositiveButton("確定") { dialog, _ ->
                sendMessageNotifyFcmMessage(userMenuOrder,editTextNote?.text.toString())
                dialog.dismiss()
            }
            setNegativeButton("取消") {
                    dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = alert.create()
        dialog.setView(editTextNote,  50 ,10, 50 , 10)
        dialog.show()
    }

    private fun sendShippingNotice(userMenuOrder: USER_MENU_ORDER)
    {

        //--- 填寫Shipping 資料 ---
        val shippingNoticeItem = LayoutInflater.from(requireContext()).inflate(R.layout.alert_input_shipping_information, null)
        val txtSelectSelectShippingDateTime = shippingNoticeItem.findViewById(R.id.txtSelectShippingTime) as TextView
        val txtSelectShippingDateTime = shippingNoticeItem.findViewById(R.id.txtShippingTime) as TextView
        val editTextShippingLoc = shippingNoticeItem.findViewById(R.id.editShippingLocation) as EditText
        val editTextShippingNotice = shippingNoticeItem.findViewById(R.id.editShippingNotice) as EditText

        txtSelectSelectShippingDateTime.setOnClickListener {

            val item = LayoutInflater.from(requireContext()).inflate(R.layout.alert_date_time_picker, null)
            val mTabHost = item.tab_host

            mTabHost.setup()
            val mDateTab: TabHost.TabSpec = mTabHost.newTabSpec("date")
            mDateTab.setIndicator("日期");
            mDateTab.setContent(R.id.date_content);
            mTabHost.addTab(mDateTab)
            // Create Time Tab and add to TabHost
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

            var alertDialog = AlertDialog.Builder(requireContext())
                .setView(item)
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("確定"){ dialog, _ ->
                    dialog.dismiss()

                    val dpPicker = item.findViewById(R.id.dpPicker) as DatePicker
                    val tpPicker = item.findViewById(R.id.tpPicker) as TimePicker

                    var sDay = "01"
                    var sMonth = "01"
                    var sYear = "2099"
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

                    val selectDateTime = "${sYear}年${sMonth}月${sDay}日 ${sHour}:${sMin}"
                    txtSelectShippingDateTime.text = selectDateTime
                }
                .show()
        }

        val alert = AlertDialog.Builder(requireContext())
        with(alert) {
            setTitle("到貨通知")
            setView(shippingNoticeItem)
            setPositiveButton("確定") { dialog, _ ->
                dialog.dismiss()
                val shippingDateTime = txtSelectShippingDateTime.text.toString()
                val shippingLocation = editTextShippingLoc.text.toString()
                val shippingNotice   = editTextShippingNotice.text.toString()
                sendNotifyShippingFcmMessage(userMenuOrder, shippingDateTime, shippingLocation, shippingNotice)
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }


    private fun sendMessageNotifyFCMWithIOS (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, message: String, timeStamp: String)
    {

        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()

        val title = "團購訊息"
        val body = "來自團購主的訂單訊息，請點擊通知以查看詳細資訊。"

        notificationHeader.put("title",title )
        notificationHeader.put("body", body )

        notificationBody.put("messageID", "")
        notificationBody.put("messageTitle", title)
        notificationBody.put("messageBody", body )

        notificationBody.put("notificationType", NOTIFICATION_TYPE_MESSAGE_INFORMATION)
        notificationBody.put("receiveTime", timeStamp)
        notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)
        notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)
        notificationBody.put("menuNumber", userMenuOrder.menuNumber)
        notificationBody.put("orderNumber", userMenuOrder.orderNumber)
        notificationBody.put("dueTime", userMenuOrder.dueTime)
        notificationBody.put("brandName", userMenuOrder.brandName)
        notificationBody.put("attendedMemberCount", userMenuOrder.contentItems!!.count().toString())
        notificationBody.put("messageDetail", message)
        notificationBody.put("isRead", "N")
        notificationBody.put("replyStatus", "N")
        notificationBody.put("replyTime", "N")

        // your notification message
        notification.put("registration_ids", JSONArray(notifyList))
        notification.put("notification", notificationHeader)
        notification.put("data", notificationBody)

        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)
    }

    private fun sendMessageNotifyFCMWithAndroid (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, message: String, timeStamp: String)
    {

        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()

        val title = "團購訊息"
        val body = "來自團購主的訂單訊息，請點擊通知以查看詳細資訊。"

        notificationHeader.put("title",title )
        notificationHeader.put("body", body )

        notificationBody.put("messageID", "")
        notificationBody.put("messageTitle", title)
        notificationBody.put("messageBody", body )

        notificationBody.put("notificationType", NOTIFICATION_TYPE_MESSAGE_INFORMATION)
        notificationBody.put("receiveTime", timeStamp)
        notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)
        notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)
        notificationBody.put("menuNumber", userMenuOrder.menuNumber)
        notificationBody.put("orderNumber", userMenuOrder.orderNumber)
        notificationBody.put("dueTime", userMenuOrder.dueTime)
        notificationBody.put("brandName", userMenuOrder.brandName)
        notificationBody.put("attendedMemberCount", userMenuOrder.contentItems!!.count().toString())
        notificationBody.put("messageDetail", message)
        notificationBody.put("isRead", "N")
        notificationBody.put("replyStatus", "N")
        notificationBody.put("replyTime", "N")

        notification.put("registration_ids", JSONArray(notifyList))
        notification.put("data", notificationBody)

        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)
    }

    private fun sendMessageNotifyFcmMessage(userMenuOrder: USER_MENU_ORDER, message: String) {

        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())
        val notificationMsgList = userMenuOrder.contentItems?.toMutableList()
        ProgressDialogUtil.showProgressDialog(context, "處理中");

        //-------Notification List 拆開成Android and IOS -----
        val iosType = notificationMsgList?.filter { it -> (it.orderContent.ostype ?: "iOS"  == "iOS" || it.orderContent.ostype ?: "iOS"  == "") && it.memberTokenID != "" }
        val androidType = notificationMsgList?.filter { it -> it.orderContent.ostype ?: "iOS"  == "Android" && it.memberTokenID != ""}

        val iosTypeList = iosType?.map { it -> it.memberTokenID !!}
        val androidTypeList = androidType?.map { it -> it.memberTokenID!! }



        sendMessageNotifyFCMWithIOS (iosTypeList!!,userMenuOrder, message, timeStamp)
        sendMessageNotifyFCMWithAndroid (androidTypeList!!,userMenuOrder, message, timeStamp)
        ProgressDialogUtil.dismiss()
    }

    private fun sendMessageShippingFCMWithIOS (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER,  timeStamp: String, shippingDateTime: String?, shippingLocation: String?, shippingNote: String?)
    {
        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()

        val title = "到貨通知"
        val body = "『${userMenuOrder.orderOwnerName}』對於 『${userMenuOrder.brandName}』的訂單發出了到貨通知"

        notificationHeader.put("title", title)
        notificationHeader.put("body", body)

        notificationBody.put("messageID", "")
        notificationBody.put("messageTitle", title)
        notificationBody.put("messageBody", body )

        notificationBody.put("notificationType", NOTIFICATION_TYPE_SHIPPING_NOTICE)
        notificationBody.put("receiveTime", timeStamp)
        notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)
        notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)
        notificationBody.put("menuNumber", userMenuOrder.menuNumber)
        notificationBody.put("orderNumber", userMenuOrder.orderNumber)
        notificationBody.put("dueTime", userMenuOrder.dueTime)
        notificationBody.put("brandName", userMenuOrder.brandName)
        notificationBody.put("attendedMemberCount", userMenuOrder.contentItems!!.count().toString())
        notificationBody.put("messageDetail", shippingNote ?: "")
        notificationBody.put("isRead", "N")
        notificationBody.put("replyStatus", "N")
        notificationBody.put("replyTime", "N")
        notificationBody.put("shippingDate", shippingDateTime)
        notificationBody.put("shippingLocation", shippingLocation)

        // your notification message
        notification.put("registration_ids", JSONArray(notifyList))
        notification.put("notification", notificationHeader)
        notification.put("data", notificationBody)

        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)

    }

    private fun sendMessageShippingFCMWithAndroid (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER,  timeStamp: String, shippingDateTime: String?, shippingLocation: String?, shippingNote: String?)
    {
        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()

        val title = "到貨通知"
        val body = "『${userMenuOrder.orderOwnerName}』對於 『${userMenuOrder.brandName}』的訂單發出了到貨通知"

        notificationHeader.put("title", title)
        notificationHeader.put("body", body)

        notificationBody.put("messageID", "")
        notificationBody.put("messageTitle", title)
        notificationBody.put("messageBody", body )

        notificationBody.put("notificationType", NOTIFICATION_TYPE_SHIPPING_NOTICE)
        notificationBody.put("receiveTime", timeStamp)
        notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)
        notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)
        notificationBody.put("menuNumber", userMenuOrder.menuNumber)
        notificationBody.put("orderNumber", userMenuOrder.orderNumber)
        notificationBody.put("dueTime", userMenuOrder.dueTime)
        notificationBody.put("brandName", userMenuOrder.brandName)
        notificationBody.put("attendedMemberCount", userMenuOrder.contentItems!!.count().toString())
        notificationBody.put("messageDetail", shippingNote ?: "")
        notificationBody.put("isRead", "N")
        notificationBody.put("replyStatus", "N")
        notificationBody.put("replyTime", "N")
        notificationBody.put("shippingDate", shippingDateTime)
        notificationBody.put("shippingLocation", shippingLocation)

        // your notification message
        notification.put("registration_ids", JSONArray(notifyList))
        notification.put("data", notificationBody)

        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)

    }


    private fun sendNotifyShippingFcmMessage(userMenuOrder: USER_MENU_ORDER, shippingDateTime: String?, shippingLocation: String?, shippingNote: String?) {

        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())
        val notificationShippingMsgList = userMenuOrder.contentItems?.toMutableList()
        ProgressDialogUtil.showProgressDialog(context,"處理中");

        //-------Notification List 拆開成Android and IOS -----
        val iosType = notificationShippingMsgList?.filter { it -> (it.orderContent.ostype ?: "iOS"  == "iOS" || it.orderContent.ostype ?: "iOS"  == "") || it.memberTokenID != ""}
        val androidType = notificationShippingMsgList?.filter { it -> it.orderContent.ostype ?: "iOS"  == "Android" || it.memberTokenID != ""}

        val iosTypeList = iosType?.map { it -> it.memberTokenID !!}
        val androidTypeList = androidType?.map { it -> it.memberTokenID!! }

        sendMessageShippingFCMWithIOS (iosTypeList!!,userMenuOrder,  timeStamp,shippingDateTime, shippingLocation, shippingNote)
        sendMessageShippingFCMWithAndroid (androidTypeList!!,userMenuOrder, timeStamp, shippingDateTime, shippingLocation, shippingNote)

        ProgressDialogUtil.dismiss()
    }

    private fun sendMessageCallableFCMWithIOS (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, timeStamp: String)
    {
        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()

        val title = "團購催訂"
        val body = "團購訂單的訂購時間即將截止，請儘速決定是否參與團購，謝謝"

        notificationHeader.put("title", title)
        notificationHeader.put("body", body)

        notificationBody.put("messageID", "")
        notificationBody.put("messageTitle", title)
        notificationBody.put("messageBody", body)
        notificationBody.put("notificationType", NOTIFICATION_TYPE_MESSAGE_DUETIME)
        notificationBody.put("receiveTime", timeStamp)
        notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)
        notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)
        notificationBody.put("menuNumber", userMenuOrder.menuNumber)
        notificationBody.put("orderNumber", userMenuOrder.orderNumber)
        notificationBody.put("dueTime", userMenuOrder.dueTime)
        notificationBody.put("brandName", userMenuOrder.brandName)
        notificationBody.put("attendedMemberCount", userMenuOrder.contentItems!!.count().toString())
        notificationBody.put("messageDetail", "")
        notificationBody.put("isRead", "N")
        notificationBody.put("replyStatus", "N")
        notificationBody.put("replyTime", "N")

        // your notification message
        notification.put("registration_ids", JSONArray(notifyList))
        notification.put("notification", notificationHeader)
        notification.put("data", notificationBody)

        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)

    }

    private fun sendMessageCallableFCMWithAndroid (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, timeStamp: String)
    {
        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()

        val title = "團購催訂"
        val body = "團購訂單的訂購時間即將截止，請儘速決定是否參與團購，謝謝"

        notificationHeader.put("title", title)
        notificationHeader.put("body", body)

        notificationBody.put("messageID", "")
        notificationBody.put("messageTitle", title)
        notificationBody.put("messageBody", body)
        notificationBody.put("notificationType", NOTIFICATION_TYPE_MESSAGE_DUETIME)
        notificationBody.put("receiveTime", timeStamp)
        notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)
        notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)
        notificationBody.put("menuNumber", userMenuOrder.menuNumber)
        notificationBody.put("orderNumber", userMenuOrder.orderNumber)
        notificationBody.put("dueTime", userMenuOrder.dueTime)
        notificationBody.put("brandName", userMenuOrder.brandName)
        notificationBody.put("attendedMemberCount", userMenuOrder.contentItems!!.count().toString())
        notificationBody.put("messageDetail", "")
        notificationBody.put("isRead", "N")
        notificationBody.put("replyStatus", "N")
        notificationBody.put("replyTime", "N")

        // your notification message
        notification.put("registration_ids", JSONArray(notifyList))
        notification.put("data", notificationBody)

        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)

    }

    private fun sendCallableFcmMessage(userMenuOrder: USER_MENU_ORDER) {

        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())
        val callableList = userMenuOrder.contentItems?.filter { it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_WAIT }?.toMutableList()
        ProgressDialogUtil.showProgressDialog(context,"處理中");

        //-------Notification List 拆開成Android and IOS -----
        val iosType = callableList?.filter { it -> (it.orderContent.ostype ?: "iOS"  == "iOS" || it.orderContent.ostype ?: "iOS"  == "") || it.memberTokenID != ""}
        val androidType = callableList?.filter { it -> it.orderContent.ostype ?: "iOS"  == "Android" || it.memberTokenID != "" }
        val iosTypeList = iosType?.map { it -> it.memberTokenID !!}
        val androidTypeList = androidType?.map { it -> it.memberTokenID!! }

        sendMessageCallableFCMWithIOS (iosTypeList!!,userMenuOrder,  timeStamp)
        sendMessageCallableFCMWithAndroid (androidTypeList!!,userMenuOrder, timeStamp)

        ProgressDialogUtil.dismiss()
    }

    private fun changeDueTimeRequest(userMenuOrder: USER_MENU_ORDER) {

        val sCrTimeStamp: String = userMenuOrder.dueTime ?: DATATIMEFORMAT_NORMAL.format(Date())
        val item = LayoutInflater.from(requireContext()).inflate(R.layout.alert_date_time_picker, null)
        val mTabHost = item.tab_host
        mTabHost.setup()
        val mDateTab: TabHost.TabSpec = mTabHost.newTabSpec("date")
        mDateTab.setIndicator("日期");
        mDateTab.setContent(R.id.date_content);
        mTabHost.addTab(mDateTab)
        // Create Time Tab and add to TabHost
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

        var alertDialog = AlertDialog.Builder(requireContext())
            .setView(item)
            .setPositiveButton("確定", null)
            .setNegativeButton("取消", null)
            .show()

        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener {
                alertDialog.dismiss()

                val dpPicker = item.findViewById(R.id.dpPicker) as DatePicker
                val tpPicker = item.findViewById(R.id.tpPicker) as TimePicker
                var sDay = "01"
                var sMonth = "01"
                var sYear = "2099"
                var sHour = "12"
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

                val sDueTimeStamp: String = "${sYear}${sMonth}${sDay}${sHour}${sMin}00000"
                val oldDueTime: Date = DATATIMEFORMAT_NORMAL.parse(sCrTimeStamp)
                val newDueTime: Date = DATATIMEFORMAT_NORMAL.parse(sDueTimeStamp)
                val diff = newDueTime.time - oldDueTime.time
                if (diff<0) {
                    val notifyAlert = AlertDialog.Builder(requireContext()).create()
                    notifyAlert.setTitle("錯誤訊息")
                    notifyAlert.setMessage("新的截止時間不得早於之前設定的截止時間")
                    notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "確定"){_,_->}
                    notifyAlert.show()
                } else {
                    sendChangeDueTimeFcmMessage(userMenuOrder, sDueTimeStamp)
                    showNoticeAlert("訊息","已對所有參與者發出更改截止日的通知")
                }
            }
    }



    private fun sendMessageChangeDueTimeFCMWithIOS (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, timeStamp: String, newDueTime:String)
    {

        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()

        val title = "團購相關訊息"
        val body = "『${userMenuOrder.orderOwnerName}』對於 『${userMenuOrder.brandName}』的訂單截止時間已更動"
        val detail = "『${userMenuOrder.orderOwnerName}』對於 『${userMenuOrder.brandName}』的訂單截止時間已更動\n按下確定後將更新相關資料"

        notificationHeader.put("title", title)
        notificationHeader.put("body", body)

        notificationBody.put("messageID", "")      //Enter
        notificationBody.put("messageTitle", title)   //Enter
        notificationBody.put("messageBody", body)    //Enter
        notificationBody.put("notificationType", NOTIFICATION_TYPE_CHANGE_DUETIME)   //Enter
        notificationBody.put("receiveTime", timeStamp)   //Enter
        notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)   //Enter
        notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)   //Enter
        notificationBody.put("menuNumber", userMenuOrder.menuNumber)   //Enter
        notificationBody.put("orderNumber", userMenuOrder.orderNumber)   //Enter
        notificationBody.put("dueTime", newDueTime)   //Enter
        notificationBody.put("brandName", userMenuOrder.brandName)   //Enter
        notificationBody.put("attendedMemberCount", userMenuOrder.contentItems!!.count().toString())   //Enter
        notificationBody.put("messageDetail", detail)   //Enter
        notificationBody.put("isRead", "Y")   //Enter
        notificationBody.put("replyStatus", "N")   //Enter
        notificationBody.put("replyTime", "N")   //Enter

        // your notification message
        notification.put("registration_ids", JSONArray(notifyList))
        notification.put("notification", notificationHeader)
        notification.put("data", notificationBody)

        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)

    }

    private fun sendMessageChangeDueTimeFCMWithAndroid (notifyList : List<String>,userMenuOrder: USER_MENU_ORDER, timeStamp: String, newDueTime:String)
    {
        val notification = JSONObject()
        val notificationHeader = JSONObject()
        val notificationBody = JSONObject()

        val title = "團購相關訊息"
        val body = "『${userMenuOrder.orderOwnerName}』對於 『${userMenuOrder.brandName}』的訂單截止時間已更動"
        val detail = "『${userMenuOrder.orderOwnerName}』對於 『${userMenuOrder.brandName}』的訂單截止時間已更動\n按下確定後將更新相關資料"

        notificationHeader.put("title", title)
        notificationHeader.put("body", body)

        notificationBody.put("messageID", "")      //Enter
        notificationBody.put("messageTitle", title)   //Enter
        notificationBody.put("messageBody", body)    //Enter
        notificationBody.put("notificationType", NOTIFICATION_TYPE_CHANGE_DUETIME)   //Enter
        notificationBody.put("receiveTime", timeStamp)   //Enter
        notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)   //Enter
        notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)   //Enter
        notificationBody.put("menuNumber", userMenuOrder.menuNumber)   //Enter
        notificationBody.put("orderNumber", userMenuOrder.orderNumber)   //Enter
        notificationBody.put("dueTime", newDueTime)   //Enter
        notificationBody.put("brandName", userMenuOrder.brandName)   //Enter
        notificationBody.put("attendedMemberCount", userMenuOrder.contentItems!!.count().toString())   //Enter
        notificationBody.put("messageDetail", detail)   //Enter
        notificationBody.put("isRead", "Y")   //Enter
        notificationBody.put("replyStatus", "N")   //Enter
        notificationBody.put("replyTime", "N")   //Enter

        // your notification message
        notification.put("registration_ids", JSONArray(notifyList))
        notification.put("data", notificationBody)

        Thread.sleep(100)
        com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)

    }


    private fun sendChangeDueTimeFcmMessage(userMenuOrder: USER_MENU_ORDER, newDueTime:String ) {

        userMenuOrder.dueTime = newDueTime
        val timeStamp: String = DATATIMEFORMAT_NORMAL.format(Date())
        val notificationChangeDueTimeList = userMenuOrder.contentItems?.toMutableList()
        ProgressDialogUtil.showProgressDialog(context, "處理中");

        //-------Notification List 拆開成Android and IOS -----
        val iosType = notificationChangeDueTimeList?.filter { it -> (it.orderContent.ostype ?: "iOS"  == "iOS" || it.orderContent.ostype ?: "iOS"  == "") || it.memberTokenID != ""}
        val androidType = notificationChangeDueTimeList?.filter { it -> it.orderContent.ostype ?: "iOS"  == "Android" || it.memberTokenID != "" }
        val iosTypeList = iosType?.map { it -> it.memberTokenID !!}
        val androidTypeList = androidType?.map { it -> it.memberTokenID!! }

        sendMessageChangeDueTimeFCMWithIOS (iosTypeList!!,userMenuOrder,  timeStamp, newDueTime)
        sendMessageChangeDueTimeFCMWithAndroid (androidTypeList!!,userMenuOrder, timeStamp, newDueTime)

        ProgressDialogUtil.dismiss()
    }

    private fun sendMenuOrderRefresh(userMenuOrder: USER_MENU_ORDER) {
        val intent = Intent("UpdateMessage")
        intent.putExtra("userMenuOrder", userMenuOrder)
        broadcast.sendBroadcast(intent)
    }


    private fun setTimePickerInterval(timePicker: TimePicker) {
        val TIME_PICKER_INTERVAL = 30
        try {
            val minutePicker = timePicker.findViewById(
                Resources.getSystem().getIdentifier(
                    "minute", "id", "android"
                )
            ) as NumberPicker
            minutePicker.minValue = 0
            minutePicker.maxValue = 60 / TIME_PICKER_INTERVAL - 1
            val displayedValues: MutableList<String> =
                java.util.ArrayList()
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

    private fun showNoticeAlert(title:String, Message:String)
    {
        val notifyAlert = AlertDialog.Builder( requireContext()).create()
        notifyAlert.setTitle(title)
        notifyAlert.setMessage(Message)
        notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i -> }
        notifyAlert.show()
    }
}



