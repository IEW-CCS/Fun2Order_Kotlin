package com.iew.fun2order.ui.notifications

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.R
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.entity.entityNotification
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.db.firebase.ORDER_MEMBER
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.order.AdapterRC_SelectedProductNoClick
import com.iew.fun2order.utility.*
import kotlinx.android.synthetic.main.activity_check_notification.*
import kotlinx.android.synthetic.main.row_selectedproduct.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class ActivityTapNotification : AppCompatActivity() {

    private var mFirebaseUserOrder: USER_MENU_ORDER? = null
    private val lstSelectedProduct: MutableList<MENU_PRODUCT> = mutableListOf()

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_notification)
        supportActionBar?.hide()


        val sdfDecode = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val sdfEncode = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")
        var messageID = ""

        txtTitle.text = ""
        txtDrafter.text = ""
        txtStarttime.text = ""
        txtEndtime.text = ""
        txtBrand.text = ""
        txtAttendance.text = ""
        txtDescription.text = ""

        intent?.extras?.let {

            val values = it.getParcelable("Notification") as entityNotification ?: entityNotification()
            lstSelectedProduct.clear()

            when (values.notificationType) {

                NOTIFICATION_TYPE_ACTION_JOIN_ORDER -> {
                    txtTitle.text = "團購邀請"
                    lstSelectedProduct.clear()
                    loadFireBaseMenuOrder(values.orderOwnerID, values.orderNumber, values.orderOwnerName)

                }
                NOTIFICATION_TYPE_MESSAGE_DUETIME -> {
                    txtTitle.text = "團購催訂"
                    txtTitle.setTextColor(Color.rgb(177, 0, 28))
                }
            }

            if (values.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT || values.replyStatus == MENU_ORDER_REPLY_STATUS_REJECT) {

                val replyDateTime = sdfDecode.parse(values.replyTime)
                val formatReplyTime = sdfEncode.format(replyDateTime).toString()
                var replyStatus = ""
                if (values.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT) {
                    replyStatus = "參加"
                    txtDescription.text = "已於 $formatReplyTime 回覆 $replyStatus "
                    txtDescription.setTextColor(Color.BLUE)
                } else if (values.replyStatus == MENU_ORDER_REPLY_STATUS_REJECT) {
                    replyStatus = "不參加"
                    txtDescription.text = "已於 $formatReplyTime 回覆 $replyStatus "
                    txtDescription.setTextColor(Color.RED)
                }


            } else if (values.replyStatus == MENU_ORDER_REPLY_STATUS_EXPIRE) {
                txtDescription.text = "團購邀請--團購單已逾期"
                txtDescription.setTextColor(Color.rgb(255, 0, 0))
                btnCheckNotifyOK.isEnabled = false
                btnCheckNotifyCancel.isEnabled = false
                btnCheckNotifyOK.isClickable = false
                btnCheckNotifyCancel.isClickable = false

            } else {
                txtDescription.text = "尚未回覆"
            }


           // ---------------------------------------
            if (values.dueTime != "" ) {
                val timeExpired = timeCompare(values.dueTime)
                if (timeExpired) {
                    txtDescription.text = "團購邀請--團購單已逾期"
                    txtDescription.setTextColor(Color.rgb(255, 0, 0))
                    btnCheckNotifyOK.isEnabled = false
                    btnCheckNotifyCancel.isEnabled = false
                    btnCheckNotifyOK.isClickable = false
                    btnCheckNotifyCancel.isClickable = false
                }
            }

            txtDrafter.text = values.orderOwnerName ?: ""
            txtBrand.text = values.brandName ?: ""
            txtAttendance.text = values.attendedMemberCount ?: ""
            messageID = values.messageID ?: ""


            try {
                val receiveDateTime = sdfDecode.parse(values.receiveTime)
                txtStarttime.text = sdfEncode.format(receiveDateTime).toString()
            } catch (ex: ParseException) {
                Log.v("Exception", ex.localizedMessage)
            }

            try {
                val dueDateTime = sdfDecode.parse(values.dueTime)
                txtEndtime.text = sdfEncode.format(dueDateTime).toString()
            } catch (ex: ParseException) {
                Log.v("Exception", ex.localizedMessage)
                txtEndtime.text = "無逾期時間"
            }

            txt_joinOrderSelectedTitle.text = ""

        }


        //---- 點選參加回到上一頁再轉到 Order 頁面
        btnCheckNotifyOK.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        //---- 點選不參加直接更新FireBase 與 DB
        btnCheckNotifyCancel.setOnClickListener {
            if (messageID != "") {
                val notificationDB = AppDatabase(this).notificationdao()
                val currentNotify = notificationDB.getNotifybyMsgID(messageID)
                var orderNumber = ""
                if (currentNotify != null) {
                    try {
                        currentNotify.replyStatus = MENU_ORDER_REPLY_STATUS_REJECT
                        currentNotify.replyTime = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                        orderNumber = currentNotify.orderNumber ?: ""
                        notificationDB.update(currentNotify)
                        updateFireBase(currentNotify.orderOwnerID, currentNotify.orderNumber, MENU_ORDER_REPLY_STATUS_REJECT)
                        updateOrderStatusToLocalDB(orderNumber, MENU_ORDER_REPLY_STATUS_REJECT)

                    } catch (e: Exception) {
                        val errorMsg = e.localizedMessage
                        Toast.makeText(this, errorMsg.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun updateFireBase(menuOrderOwnerID: String, menuOrderNumber: String, replyStatus: String) {

        val menuPath = "USER_MENU_ORDER/${menuOrderOwnerID}/${menuOrderNumber}/contentItems"
        val database = Firebase.database
        val myRef = database.getReference(menuPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach()
                {
                    val test = it.getValue(ORDER_MEMBER::class.java)
                    if (test != null) {
                        if (test.memberID == FirebaseAuth.getInstance().currentUser!!.uid.toString()) {
                            test.orderContent.createTime =
                                SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                            test.orderContent.replyStatus = replyStatus
                            it.ref.setValue(test)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateOrderStatusToLocalDB(orderNumber: String, replyStatus: String) {

        val notificationDB = AppDatabase(this).notificationdao()
        if (orderNumber != "") {
            val orderMessages = notificationDB.getNotifybyOrderNo(orderNumber)
            orderMessages.forEach()
            {
                it.replyStatus = MENU_ORDER_REPLY_STATUS_REJECT
                it.replyTime = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                notificationDB.update(it)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun timeCompare(compareDatetime: String): Boolean {
        val currentTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        return try {
            val beginTime: Date = currentTime.parse(compareDatetime)
            val endTime: Date = Date()
            (endTime.time - beginTime.time) > 0
        } catch (e: android.net.ParseException) {
            false
        }
    }


    private fun loadFireBaseMenuOrder(menuOrderOwnerID: String, menuOrderNumber: String, menuOrderOwnerName:String) {
        val menuPath = "USER_MENU_ORDER/${menuOrderOwnerID}/${menuOrderNumber}"
        val database = Firebase.database
        val myRef = database.getReference(menuPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mFirebaseUserOrder = dataSnapshot.getValue(USER_MENU_ORDER::class.java)
                if(mFirebaseUserOrder == null)
                {
                    btnCheckNotifyOK.isEnabled = false
                    btnCheckNotifyCancel.isEnabled = false
                    btnCheckNotifyOK.isClickable = false
                    btnCheckNotifyCancel.isClickable = false

                    val notifyAlert = AlertDialog.Builder(this@ActivityTapNotification).create()
                    notifyAlert.setTitle("訊息通知")
                    notifyAlert.setCancelable(false)
                    notifyAlert.setMessage("編號 ${menuOrderNumber} 訂單不存在 \n請聯繫訂單發起人: ${menuOrderOwnerName}")
                    notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
                        finish()
                    }
                    notifyAlert.show()
                }

                else
                {
                    showSelfContentItemsItems()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                mFirebaseUserOrder = null
                btnCheckNotifyOK.isEnabled = false
                btnCheckNotifyCancel.isEnabled = false
                btnCheckNotifyOK.isClickable = false
                btnCheckNotifyCancel.isClickable = false

                val notifyAlert = AlertDialog.Builder(this@ActivityTapNotification).create()
                notifyAlert.setTitle("訊息通知")
                notifyAlert.setCancelable(false)
                notifyAlert.setMessage("訂單資料讀取錯誤, 請再次一次!!")
                notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
                    finish()
                }
                notifyAlert.show()
            }
        })
    }

    private fun showSelfContentItemsItems() {
        if (mFirebaseUserOrder != null) {
            mFirebaseUserOrder!!.contentItems?.forEach { orderMember ->
                if (orderMember.memberID == FirebaseAuth.getInstance().currentUser!!.uid.toString() && orderMember.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT) {
                    val refProductItems = orderMember.orderContent.menuProductItems?.toMutableList() ?: mutableListOf()
                    refProductItems.forEach()
                    {
                        txt_joinOrderSelectedTitle.text = "已回覆產品列表"
                        lstSelectedProduct.add(it)
                    }

                    lstSelectedProduct.forEach()
                    {
                        menuProduct ->
                        val itemView = LayoutInflater.from(this).inflate(R.layout.row_selectedproduct, null)

                        var recipeItems = ""
                        menuProduct.menuRecipes?.forEach {
                            it.recipeItems!!.forEach { recipeItem ->
                                if (recipeItem.checkedFlag == true) {
                                    recipeItems = recipeItems + recipeItem.recipeName + " "
                                }
                            }
                        }

                        var comments = ""
                        comments = if (menuProduct.itemComments != "") {
                            "(${menuProduct.itemComments})"
                        } else {
                            menuProduct.itemComments!!
                        }

                        itemView.selectedproductItem.text = menuProduct.itemName
                        itemView.selectedproductNote.text = "$recipeItems $comments"
                        itemView.selectedproductCount.text = menuProduct.itemQuantity.toString()
                        rcv_joinOrderSelectedLayout.addView(itemView)
                    }
                }
            }
        }
    }
}
