package com.iew.fun2order.order

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
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
                val notifyAlert = AlertDialog.Builder( requireContext()).create()
                notifyAlert.setTitle("訊息")
                notifyAlert.setMessage("已經對尚未回覆者發出催訂通知")
                notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
                }
                notifyAlert.show()
            }
            bottomSheet.buttonChangeduetime.setOnClickListener {

                dialog.dismiss()
                sendChangeDueTimeFcmMessage(menuorder)

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
                sendNotify(menuorder)
            }
            bottomSheet.buttonShippingNotice.setOnClickListener {

                dialog.dismiss()
                sendShippingNotice(menuorder)

            }
            bottomSheet.buttonSubmit.setOnClickListener { dialog.dismiss() }
            dialog.setContentView(bottomSheet)
            dialog.show()

        }

        checkOrdeStatus()
    }


    fun setOrderInfo() {

        txtOrderBrand.text = menuorder.brandName
        txtOrderJoinCount.text = menuorder.contentItems?.count().toString()

        val sdfDecode = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val sdfEncode = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")

        if(menuorder.createTime!= "") {
            val startDateTime = sdfDecode.parse(menuorder.createTime)
            val formatStartDatetime = sdfEncode.format(startDateTime).toString()
            txtOrderStartTime.text = formatStartDatetime
        }
        else
        {
            txtOrderStartTime.text = ""
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

    private fun checkOrdeStatus() {

        if (menuorder != null) {
            if (menuorder.dueTime != null) {
                var timeExpired = false
                if (menuorder.dueTime!! != "") {
                    timeExpired = timeCompare(menuorder.dueTime!!)
                }

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
                if(menuOrder!= null) {
                    if (menuOrder.dueTime != null) {
                        var timeExpired = false
                        if(menuOrder.dueTime!! != "") {
                            timeExpired = timeCompare(menuOrder.dueTime!!)
                        }

                        layoutCallable.isClickable = true
                        layoutCallable.isEnabled = true
                        if (timeExpired) {
                            //--- 關閉催訂通知 -----
                            layoutCallable.isClickable = false
                            layoutCallable.isEnabled = false
                            textCallable.setTextColor(Color.GRAY)
                            txtOrderEndTime.setTextColor(requireContext().resources.getColor(R.color.red))
                            var needUpdate = false
                            menuOrder.contentItems?.forEach {
                                if (it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_WAIT) {
                                    it.orderContent.replyStatus = MENU_ORDER_REPLY_STATUS_EXPIRE
                                    needUpdate = true
                                }
                            }
                            if(needUpdate) {
                                myRef.setValue(menuOrder)
                            }
                        }
                    }
                    menuorder = menuOrder.copy()
                    setOrderInfo()
                    sendMenuOrderRefresh(menuOrder.copy())
                    ProgressDialogUtil.dismiss();
                    //------ 廣播更新資料 ------
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

    @SuppressLint("SimpleDateFormat")
    private fun timeCompare(compareDatetime: String): Boolean {
        val currentTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        return try {
            val beginTime: Date = currentTime.parse(compareDatetime)
            val endTime: Date = Date()
            //判斷是否大於兩天
            (endTime.time - beginTime.time) > 0
        } catch (e: ParseException) {
            false
        }
    }


    private fun sendNotify( userMenuOrder: USER_MENU_ORDER)
    {
        val alert = AlertDialog.Builder(requireContext())
        var editTextNote: EditText? = null

        with (alert) {
            setTitle("請輸入訊息")
            editTextNote = EditText(requireContext())

            setPositiveButton("確定") { dialog, _ ->
                sendNotifyFcmMessage(userMenuOrder,editTextNote?.text.toString())
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


    private fun sendShippingNotice( userMenuOrder: USER_MENU_ORDER)
    {

        //--- 填寫Shipping 資料 ---
        val ShippingNoticeitem = LayoutInflater.from(requireContext()).inflate(R.layout.alert_input_shipping_information, null)
        var txtSelectSelectShippingDateTime = ShippingNoticeitem.findViewById(R.id.txtSelectShippingTime) as TextView
        var txtSelectShippingDateTime = ShippingNoticeitem.findViewById(R.id.txtShippingTime) as TextView
        var editTextShippingLoc = ShippingNoticeitem.findViewById(R.id.editShippingLocation) as EditText
        var editTextShippingNotice = ShippingNoticeitem.findViewById(R.id.editShippingNotice) as EditText

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

                    if (dpPicker.getDayOfMonth() < 10) {
                        sDay = "0" + dpPicker.getDayOfMonth().toString()
                    } else {
                        sDay = dpPicker.getDayOfMonth().toString();
                    }
                    var month = dpPicker.getMonth() + 1;

                    if (month < 10) {
                        sMonth = "0" + month.toString()
                    } else {
                        sMonth = month.toString();
                    }

                    sYear = dpPicker.getYear().toString();

                    if (tpPicker.getHour() < 10) {
                        sHour = "0" + tpPicker.getHour().toString()
                    } else {
                        sHour = tpPicker.getHour().toString();
                    }

                    if (tpPicker.getMinute() < 10) {
                        sMin = "0" + tpPicker.getMinute().toString()
                    } else {
                        sMin = tpPicker.getMinute().toString();
                    }
                    txtSelectShippingDateTime.text = "${sYear}年${sMonth}月${sDay}日 ${sHour}:${sMin}"
                }
                .show()
        }

        val alert = AlertDialog.Builder(requireContext())
        with(alert) {
            setTitle("到貨通知")
            setView(ShippingNoticeitem)
            setPositiveButton("確定") { dialog, _ ->
                dialog.dismiss()
                val shippingDateTime = txtSelectShippingDateTime.text.toString()
                val shippingLocation = editTextShippingLoc.text.toString()
                val shippingNotice = editTextShippingNotice.text.toString()
                sendNotifyShippingFcmMessage(userMenuOrder, shippingDateTime, shippingLocation, shippingNotice)
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }



    private fun sendNotifyFcmMessage(userMenuOrder: USER_MENU_ORDER, message: String) {

        val dbContext: MemoryDatabase = MemoryDatabase(context!!)
        val friendImageDB: friendImageDAO = dbContext.friendImagedao()

        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
        val notificationMsgList = userMenuOrder.contentItems?.toMutableList()
        ProgressDialogUtil.showProgressDialog(context, "處理中");
        notificationMsgList?.forEach {it->
            val orderMember = it as ORDER_MEMBER
            val topic = orderMember.memberTokenID
            val notification = JSONObject()
            val notificationHeader = JSONObject()
            val notificationBody = JSONObject()

            val body = "來自團購主的訂單訊息，請點擊通知以查看詳細資訊。"

            notificationHeader.put("title", "團購訊息")
            notificationHeader.put("body", body ?: "")   //Enter your notification message

            notificationBody.put("messageID", "")      //Enter
            notificationBody.put("messageTitle", "團購訊息")   //Enter
            notificationBody.put("messageBody", body ?: "")    //Enter

            notificationBody.put("notificationType", NOTIFICATION_TYPE_MESSAGE_INFORMATION)   //Enter
            notificationBody.put("receiveTime", timeStamp)   //Enter
            notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)   //Enter
            notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)   //Enter
            notificationBody.put("menuNumber", userMenuOrder.menuNumber)   //Enter

            notificationBody.put("orderNumber", userMenuOrder.orderNumber)   //Enter
            notificationBody.put("dueTime", userMenuOrder.dueTime)   //Enter

            notificationBody.put("brandName", userMenuOrder.brandName)   //Enter
            notificationBody.put(
                "attendedMemberCount",
                userMenuOrder.contentItems!!.count().toString()
            )   //Enter

            notificationBody.put("messageDetail", message)   //Enter  //
            notificationBody.put("isRead", "N")   //Enter
            notificationBody.put("replyStatus", "N")   //Enter
            notificationBody.put("replyTime", "N")   //Enter

            // your notification message
            notification.put("to", topic)
            notification.put("notification", notificationHeader)
            notification.put("data", notificationBody)


            if(orderMember.orderContent.ostype ?: "iOS" == "Android")
            {
                notification.remove("notification")
            }

            Thread.sleep(100)
            com.iew.fun2order.MainActivity.sendFirebaseNotification(notification)
        }
        ProgressDialogUtil.dismiss()
    }

    private fun sendNotifyShippingFcmMessage(userMenuOrder: USER_MENU_ORDER, shippingDateTime: String?, shippingLocation: String?, shippingNote: String?) {

        val dbContext: MemoryDatabase = MemoryDatabase(context!!)
        val friendImageDB: friendImageDAO = dbContext.friendImagedao()

        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
        val notificationShippingMsgList = userMenuOrder.contentItems?.toMutableList()
        ProgressDialogUtil.showProgressDialog(context,"處理中");
        notificationShippingMsgList?.forEach {it->
            val orderMember = it as ORDER_MEMBER
            val topic = orderMember.memberTokenID
            val notification = JSONObject()
            val notificationHeader = JSONObject()
            val notificationBody = JSONObject()

            val body = "『${userMenuOrder.orderOwnerName}』對於 『${userMenuOrder.brandName}』的訂單發出了到貨通知"

            notificationHeader.put("title", "到貨通知")
            notificationHeader.put("body", body ?: "")   //Enter your notification message

            notificationBody.put("messageID", "")      //Enter
            notificationBody.put("messageTitle", "到貨通知")   //Enter
            notificationBody.put("messageBody", body ?: "")    //Enter

            notificationBody.put("notificationType", NOTIFICATION_TYPE_SHIPPING_NOTICE)   //Enter
            notificationBody.put("receiveTime", timeStamp)   //Enter
            notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)   //Enter
            notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)   //Enter
            notificationBody.put("menuNumber", userMenuOrder.menuNumber)   //Enter

            notificationBody.put("orderNumber", userMenuOrder.orderNumber)   //Enter
            notificationBody.put("dueTime", userMenuOrder.dueTime)   //Enter

            notificationBody.put("brandName", userMenuOrder.brandName)   //Enter
            notificationBody.put(
                "attendedMemberCount",
                userMenuOrder.contentItems!!.count().toString()
            )   //Enter

            notificationBody.put("messageDetail", shippingNote ?: "")   //Enter  //
            notificationBody.put("isRead", "N")   //Enter
            notificationBody.put("replyStatus", "N")   //Enter
            notificationBody.put("replyTime", "N")   //Enter

            if(shippingDateTime != null) {
                notificationBody.put("shippingDate", shippingDateTime.toString())   //Enter
            }
            if(shippingLocation != null) {
                notificationBody.put("shippingLocation", shippingLocation.toString())   //Enter
            }

            // your notification message
            notification.put("to", topic)
            notification.put("notification", notificationHeader)
            notification.put("data", notificationBody)

            if(orderMember.orderContent.ostype ?: "iOS" == "Android")
            {
                notification.remove("notification")
            }

            Thread.sleep(100)
            com.iew.fun2order.MainActivity.sendFirebaseNotification(notification)
        }
        ProgressDialogUtil.dismiss()
    }

    private fun sendCallableFcmMessage(userMenuOrder: USER_MENU_ORDER) {

        val dbContext: MemoryDatabase = MemoryDatabase(context!!)
        val friendImageDB: friendImageDAO = dbContext.friendImagedao()

        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
        val callableList = userMenuOrder.contentItems?.filter { it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_WAIT }?.toMutableList()
        ProgressDialogUtil.showProgressDialog(context,"處理中");
        callableList?.forEach {it->

            val orderMember = it as ORDER_MEMBER
            val topic = orderMember.memberTokenID
            val notification = JSONObject()
            val notificationHeader = JSONObject()
            val notificationBody = JSONObject()

            val body = "團購訂單的訂購時間即將截止，請儘速決定是否參與團購，謝謝"

            notificationHeader.put("title", "團購催訂")
            notificationHeader.put("body", body ?: "")   //Enter your notification message

            notificationBody.put("messageID", "")      //Enter
            notificationBody.put("messageTitle", "團購催訂")   //Enter
            notificationBody.put("messageBody", body ?: "")    //Enter

            notificationBody.put("notificationType", NOTIFICATION_TYPE_MESSAGE_DUETIME)   //Enter
            notificationBody.put("receiveTime", timeStamp)   //Enter
            notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)   //Enter
            notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)   //Enter
            notificationBody.put("menuNumber", userMenuOrder.menuNumber)   //Enter

            notificationBody.put("orderNumber", userMenuOrder.orderNumber)   //Enter
            notificationBody.put("dueTime", userMenuOrder.dueTime)   //Enter

            notificationBody.put("brandName", userMenuOrder.brandName)   //Enter
            notificationBody.put(
                "attendedMemberCount",
                userMenuOrder.contentItems!!.count().toString()
            )   //Enter

            notificationBody.put("messageDetail", "")   //Enter
            notificationBody.put("isRead", "N")   //Enter
            notificationBody.put("replyStatus", "N")   //Enter
            notificationBody.put("replyTime", "N")   //Enter

            // your notification message
            notification.put("to", topic)
            notification.put("notification", notificationHeader)
            notification.put("data", notificationBody)

            if(orderMember.orderContent.ostype ?:"iOS" == "Android")
            {
                notification.remove("notification")
            }

            Thread.sleep(100)
            com.iew.fun2order.MainActivity.sendFirebaseNotification(notification)

        }
        ProgressDialogUtil.dismiss()
    }

    @SuppressLint("SimpleDateFormat")
    private fun sendChangeDueTimeFcmMessage(userMenuOrder: USER_MENU_ORDER) {


        var dateFormat = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val sCrTimeStamp: String = userMenuOrder.dueTime ?: SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())

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
                var sHour = "12";
                var sMin = "00"

                if (dpPicker.getDayOfMonth() < 10) {
                    sDay = "0" + dpPicker.getDayOfMonth().toString()
                } else {
                    sDay = dpPicker.getDayOfMonth().toString();
                }
                var month = dpPicker.getMonth() + 1;

                if (month < 10) {
                    sMonth = "0" + month.toString()
                } else {
                    sMonth = month.toString();
                }

                sYear = dpPicker.getYear().toString();

                if (tpPicker.getHour() < 10) {
                    sHour = "0" + tpPicker.getHour().toString()
                } else {
                    sHour = tpPicker.getHour().toString();
                }

                if (tpPicker.getMinute() < 10) {
                    sMin = "0" + tpPicker.getMinute().toString()
                } else {
                    sMin = tpPicker.getMinute().toString();
                }

                val sDueTimeStamp: String = sYear + sMonth + sDay + sHour + sMin +"00"+"000"
                val oldDueTime: Date = dateFormat.parse(sCrTimeStamp)
                val newDueTime: Date = dateFormat.parse(sDueTimeStamp)
                val diff = newDueTime.time - oldDueTime.time

                if (diff<0) {
                    val notifyAlert = AlertDialog.Builder(requireContext()).create()
                    notifyAlert.setTitle("錯誤訊息")
                    notifyAlert.setMessage("新的截止時間不得早於之前設定的截止時間")
                    notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "確定"){_,_->}
                    notifyAlert.show()

                } else {

                    val dbContext: MemoryDatabase = MemoryDatabase(context!!)
                    val friendImageDB: friendImageDAO = dbContext.friendImagedao()

                    userMenuOrder.dueTime = sDueTimeStamp
                    val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                    val notificationChangeDueTimeList = userMenuOrder.contentItems?.toMutableList()
                    ProgressDialogUtil.showProgressDialog(context, "處理中");
                    notificationChangeDueTimeList?.forEach {it->

                        val orderMember = it as ORDER_MEMBER
                        val topic = orderMember.memberTokenID
                        val notification = JSONObject()
                        val notificationHeader = JSONObject()
                        val notificationBody = JSONObject()

                        val body = "『${userMenuOrder.orderOwnerName}』對於 『${userMenuOrder.brandName}』的訂單截止時間已更動"
                        val detail = "『${userMenuOrder.orderOwnerName}』對於 『${userMenuOrder.brandName}』的訂單截止時間已更動\n按下確定後將更新相關資料"

                        notificationHeader.put("title", "團購相關訊息")
                        notificationHeader.put("body", body ?: "")

                        notificationBody.put("messageID", "")      //Enter
                        notificationBody.put("messageTitle", "團購相關訊息")   //Enter
                        notificationBody.put("messageBody", body ?: "")    //Enter
                        notificationBody.put("notificationType", NOTIFICATION_TYPE_CHANGE_DUETIME)   //Enter
                        notificationBody.put("receiveTime", timeStamp)   //Enter
                        notificationBody.put("orderOwnerID", userMenuOrder.orderOwnerID)   //Enter
                        notificationBody.put("orderOwnerName", userMenuOrder.orderOwnerName)   //Enter
                        notificationBody.put("menuNumber", userMenuOrder.menuNumber)   //Enter
                        notificationBody.put("orderNumber", userMenuOrder.orderNumber)   //Enter
                        notificationBody.put("dueTime", userMenuOrder.dueTime)   //Enter
                        notificationBody.put("brandName", userMenuOrder.brandName)   //Enter
                        notificationBody.put("attendedMemberCount", userMenuOrder.contentItems!!.count().toString())   //Enter
                        notificationBody.put("messageDetail", detail)   //Enter
                        notificationBody.put("isRead", "Y")   //Enter
                        notificationBody.put("replyStatus", "N")   //Enter
                        notificationBody.put("replyTime", "N")   //Enter

                        // your notification message
                        notification.put("to", topic)
                        notification.put("notification", notificationHeader)
                        notification.put("data", notificationBody)

                        if(orderMember.orderContent.ostype ?: "iOS" == "Android")
                        {
                            notification.remove("notification")
                        }

                        Thread.sleep(100)
                        com.iew.fun2order.MainActivity.sendFirebaseNotification(notification)

                    }
                    ProgressDialogUtil.dismiss()

                    val notifyAlert = AlertDialog.Builder(requireContext()).create()
                    notifyAlert.setTitle("訊息")
                    notifyAlert.setMessage("已對所有參與者發出更改截止日的通知")
                    notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "確定"){_,_->}
                    notifyAlert.show()
                }
            }
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
}



