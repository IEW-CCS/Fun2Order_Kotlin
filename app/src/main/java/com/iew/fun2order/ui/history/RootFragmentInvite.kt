// NG Wait Notify

package com.iew.fun2order.ui.history

import android.content.Intent
import android.net.ParseException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.order.JoinOrderActivity
import com.iew.fun2order.R
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.entity.entityNotification
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.db.firebase.ORDER_MEMBER
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.order.AdapterRC_SelectedProductNoClick
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.*
import kotlinx.android.synthetic.main.activity_setup_order.*
import java.text.SimpleDateFormat
import java.util.*


class RootFragmentInvite() : Fragment(), IAdapterOnClick , IAdapterBtnOnClick {

    private var mFirebaseUserOrder: USER_MENU_ORDER? = null
    var listInvites: MutableList<ItemsLV_Invite> = mutableListOf()
    var rcvInvites: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater!!.inflate(R.layout.fragment_invite, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationDB = AppDatabase(context!!).notificationdao()
        notificationDB.getAllInvite().observe(this, Observer {
            val list = (it as ArrayList<entityNotification>).asReversed()
            listInvites.clear()
            list.forEach() { it ->
                var duetime = false
                if (it.dueTime != ""  ) {
                    val timeExpired = timeCompare(it.dueTime)
                    if (timeExpired) {
                        duetime = true
                    }
                }
                listInvites.add(
                    ItemsLV_Invite(
                        it.messageID,
                        it.brandName,
                        it.receiveTime,
                        it.orderOwnerName,
                        it.orderOwnerID,
                        it.orderNumber,
                        it.replyStatus,
                        it.replyTime,
                        duetime
                    )
                )
            }
            rcvInvites!!.adapter!!.notifyDataSetChanged()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            rcvInvites = it.findViewById<RecyclerView>(R.id.RecycleView_invite)
        }

        rcvInvites!!.layoutManager = LinearLayoutManager(activity!!)
        rcvInvites!!.adapter = RCAdapter_Invite(context!!, listInvites, this, this)
    }


    override fun onClick(sender: String, pos: Int, type: Int) {
        if (type == 1) {
            checkRemoveNotify(pos)
        }
        else if(type == 0)
        {
            val notificationDB = AppDatabase(requireContext()).notificationdao()
            val currentNotify = notificationDB.getNotifybyMsgID(listInvites[pos]!!.messageid)
            loadFireBaseMenuOrder(currentNotify.orderOwnerID, currentNotify.orderNumber, currentNotify.orderOwnerName)
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
                    val notifyAlert = AlertDialog.Builder(context!!).create()
                    notifyAlert.setTitle("訊息通知")
                    notifyAlert.setCancelable(false)
                    notifyAlert.setMessage("編號 ${menuOrderNumber} 訂單不存在 \n請聯繫訂單發起人: ${menuOrderOwnerName}")
                    notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
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

                val notifyAlert = AlertDialog.Builder(context!!).create()
                notifyAlert.setTitle("訊息通知")
                notifyAlert.setCancelable(false)
                notifyAlert.setMessage("訂單資料讀取錯誤, 請再次一次!!")
                notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i -> }
                notifyAlert.show()
            }
        })
    }

    override fun onBtnClick(sender: String, pos: Int) {

        //------ 回來的時候Call同一個Function Update Firebase
        val notificationDB = AppDatabase(context!!).notificationdao()
        val currentNotify = notificationDB.getNotifybyMsgID(listInvites[pos]!!.messageid)
        var orderNumber = ""

        if (sender == MENU_ORDER_REPLY_STATUS_ACCEPT) {
            val bundle = Bundle()
            bundle.putParcelable("InviteOrderInfo", currentNotify.copy())
            val intent = Intent(context, JoinOrderActivity::class.java)
            intent.putExtras(bundle)
            startActivityForResult(intent, ACTION_JOINORDER_CODE)
        } else if(sender == MENU_ORDER_REPLY_STATUS_REJECT) {

            orderNumber = currentNotify.orderNumber ?: ""
            currentNotify.replyStatus = MENU_ORDER_REPLY_STATUS_REJECT
            currentNotify.replyTime  =  SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
            try {
                notificationDB.update(currentNotify)
                updateFireBase( listInvites[pos].inviteUserUUID, listInvites[pos].inviteOrderNumber, MENU_ORDER_REPLY_STATUS_REJECT)
                updateOrderStatusToLocalDB(orderNumber, MENU_ORDER_REPLY_STATUS_REJECT)
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage
                Toast.makeText(activity, errorMsg.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun updateOrderStatusToLocalDB(orderNumber: String, replyStatus: String)
    {

        val notificationDB = AppDatabase(requireContext()).notificationdao()
        if (orderNumber!= "") {
            val orderMessages = notificationDB.getNotifybyOrderNo(orderNumber)
            orderMessages.forEach()
            {
                it.replyStatus = MENU_ORDER_REPLY_STATUS_REJECT
                it.replyTime = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                notificationDB.update(it)
            }
        }
    }

    private fun timeCompare(compareDatetime: String): Boolean {

        val currentTime = SimpleDateFormat("yyyyMMddHHmmssSSS")
        try {
            val beginTime: Date = currentTime.parse(compareDatetime)
            val endTime: Date = Date()
            return (endTime.time - beginTime.time) > 0
        } catch (e: ParseException) {
            return false
        }
    }

    private fun checkRemoveNotify(position: Int) {
        val deleteItems = listInvites[position]
        val deleteUUID = deleteItems.messageid
        val alert = AlertDialog.Builder(context!!)
        with(alert) {
            setTitle("刪除邀請單 : ${deleteItems.title}")
            setPositiveButton("確定") { dialog, _ ->

                try {
                    val notificationDB = AppDatabase(context).notificationdao()
                    notificationDB.deleteNotify(deleteUUID)
                } catch (e: Exception) {
                    val errorMsg = e.localizedMessage
                    Toast.makeText(activity, errorMsg.toString(), Toast.LENGTH_LONG).show()
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


    private fun updateFireBase(menuOrderOwnerID: String, menuOrderNumber: String, replyStatus:String ) {

        //-------  Update to FireBase -------
        val menuPath = "USER_MENU_ORDER/${menuOrderOwnerID}/${menuOrderNumber}/contentItems"
        val database = Firebase.database
        val myRef = database.getReference(menuPath)

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach()
                {
                    val test = it.getValue(ORDER_MEMBER::class.java)
                    if(test!= null) {
                        if (test.memberID == FirebaseAuth.getInstance().currentUser!!.uid.toString()) {
                            test.orderContent.createTime =  SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                            test.orderContent.replyStatus = replyStatus
                            it.ref.setValue(test)
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


    private fun showSelfContentItemsItems() {

        val item = LayoutInflater.from(context).inflate(R.layout.alert_invite_order_selected, null)
        val rcvSelectedProduct = item.findViewById <ListView>(R.id.alert_joinOrderSelectedList)

        val lstSelectedProduct: MutableList<MENU_PRODUCT> = mutableListOf()
        lstSelectedProduct.clear()

        if (mFirebaseUserOrder != null) {
            mFirebaseUserOrder!!.contentItems?.forEach { orderMember ->
                if (orderMember.memberID == FirebaseAuth.getInstance().currentUser!!.uid.toString() && orderMember.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT) {
                    val refProductItems = orderMember.orderContent.menuProductItems?.toMutableList() ?: mutableListOf()
                    val refLocation = orderMember.orderContent.location
                    refProductItems.forEach()
                    {
                        lstSelectedProduct.add(it)
                    }
                }
            }
        }

        if(lstSelectedProduct.count() == 0)
        {
            val notifyAlert = AlertDialog.Builder(requireContext()).create()
            notifyAlert.setTitle("提示訊息")
            notifyAlert.setCancelable(false)
            notifyAlert.setMessage("這張邀請單未選購任何產品")
            notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
            }
            notifyAlert.show()
        }

        else
        {
            rcvSelectedProduct.adapter = AdapterLV_SelectedProductInvite(requireContext(), lstSelectedProduct)
            var alertDialog = AlertDialog.Builder(requireContext())
                .setView(item)
                .setTitle("已回覆 產品列表")
                .setCancelable(false)
                .setPositiveButton("確定", null)
                .show()
        }





    }
}



