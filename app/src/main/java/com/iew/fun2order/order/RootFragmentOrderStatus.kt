package com.iew.fun2order.order

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.ParseException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.ORDER_MEMBER
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.utility.*
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
            sendCallableFcmMessage(menuorder)

            //-------- Chris42 20200514 
            val notifyAlert = AlertDialog.Builder( requireContext()).create()
            notifyAlert.setTitle("訊息")
            notifyAlert.setMessage("已經對尚未回覆者發出催訂通知")
            notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
            }
            notifyAlert.show()
        }

        layoutNotify.setOnClickListener{
            sendNotify(menuorder)
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



    private fun sendNotifyFcmMessage(userMenuOrder: USER_MENU_ORDER, message: String) {

        val notificationMsgList = userMenuOrder.contentItems?.toMutableList()
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
            notificationBody.put("receiveTime", userMenuOrder.createTime)   //Enter
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
            com.iew.fun2order.MainActivity.sendFirebaseNotification(notification)
        }
    }

    private fun sendCallableFcmMessage(userMenuOrder: USER_MENU_ORDER) {

        val callableList = userMenuOrder.contentItems?.filter { it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_WAIT }?.toMutableList()
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
            notificationBody.put("receiveTime", userMenuOrder.createTime)   //Enter
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
            com.iew.fun2order.MainActivity.sendFirebaseNotification(notification)

        }
    }

    private fun sendMenuOrderRefresh(userMenuOrder: USER_MENU_ORDER) {
        val intent = Intent("UpdateMessage")
        intent.putExtra("userMenuOrder", userMenuOrder)
        broadcast.sendBroadcast(intent)
    }
}



