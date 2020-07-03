// NG Wait Notify

package com.iew.fun2order.ui.notifications

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.order.JoinOrderActivity
import com.iew.fun2order.R
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.entity.entityNotification
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.*
import java.text.SimpleDateFormat
import java.util.*

class RootFragmentInvitation() : Fragment() ,IAdapterOnClick  {
    private var lstNotification: MutableList<Any> = mutableListOf()
    private var rcvNotification: RecyclerView? = null
    private var objIntentNotify: entityNotification? = null
    private lateinit var broadcast: LocalBroadcastManager
    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onStart() {
        super.onStart()
        updateBadge()
    }

    private fun updateBadge()
    {
        val intent = Intent("Message")
        broadcast.sendBroadcast(intent)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        notificationsViewModel = ViewModelProviders.of(this).get(NotificationsViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_notify_invite, container, false)
        val sdfDecode = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val sdfEncode = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")

        broadcast = LocalBroadcastManager.getInstance(requireContext())
        rcvNotification = root.findViewById<RecyclerView>(R.id.rcvNotification)
        rcvNotification!!.layoutManager = LinearLayoutManager(requireActivity())
        rcvNotification!!.adapter = AdapterRC_Notification_WithBannerAds(requireContext(), lstNotification, this)

        ProgressDialogUtil.showProgressDialog(context);
        val notificationDB = AppDatabase(requireContext()).notificationdao()
        notificationDB.getAllInvite().observe(viewLifecycleOwner, Observer { notify ->
            val list = (notify as ArrayList<entityNotification>).asReversed()
            lstNotification.clear()

            //-- 第一筆先放廣告 -----
            lstNotification.add(ItemsLV_Ads(getString(R.string.banner_ad_unit_id)))
            list.forEach() { it ->
                val receiveDateTime = sdfDecode.parse(it.receiveTime)
                val formatReceiveDatetime = sdfEncode.format(receiveDateTime).toString()
                lstNotification.add(
                    ItemsLV_Notify(
                        it.messageID,
                        it.orderOwnerName,
                        it.notificationType,
                        it.replyStatus,
                        it.brandName,
                        formatReceiveDatetime,
                        it.messageBody,
                        it.isRead
                    )
                )
            }
            recycleViewRefresh()
            ProgressDialogUtil.dismiss()
        })
        return root
    }

    override fun onClick(sender: String, pos: Int, type: Int) {
        if (type == 1) {
            checkRemoveNotify(pos)
        } else {
            if(lstNotification[pos] is ItemsLV_Notify) {
                val notification = (lstNotification[pos] as ItemsLV_Notify).copy()
                val notificationDB = AppDatabase(requireContext()).notificationdao()
                val currentNotify = notificationDB.getNotifybyMsgID(notification.messageid)
                if (currentNotify != null) {
                    currentNotify.isRead = "Y"
                    objIntentNotify = currentNotify.copy()
                    try {
                        notificationDB.update(currentNotify)
                        updateBadge()

                    } catch (e: Exception) {
                        val errorMsg = e.localizedMessage
                        Toast.makeText(activity, errorMsg.toString(), Toast.LENGTH_LONG).show()
                    }
                }

                when (objIntentNotify!!.notificationType) {
                    NOTIFICATION_TYPE_ACTION_JOIN_ORDER -> {
                        val bundle = Bundle()
                        bundle.putParcelable("Notification", objIntentNotify)
                        val intent = Intent(context, ActivityTapNotification::class.java)
                        intent.putExtras(bundle)
                        startActivityForResult(intent, ACTION_NOTIFYACTION_REQUEST_CODE)
                    }
                    else ->
                    {
                        // --- DO nothing -----
                    }
                }
            }
        }
    }

    fun recycleViewRefresh() {
        rcvNotification!!.adapter!!.notifyDataSetChanged()
    }

    fun checkRemoveNotify(position: Int) {
        if(lstNotification[position] is ItemsLV_Notify) {
            val deleteItems = lstNotification[position] as ItemsLV_Notify
            val deleteUUID = deleteItems.messageid

            val alert = AlertDialog.Builder(requireContext())
            with(alert) {
                setTitle("刪除通知")
                setPositiveButton("確定") { dialog, _ ->
                    try {
                        val notificationDB = AppDatabase(requireContext()).notificationdao()
                        notificationDB.deleteNotify(deleteUUID)
                        val intent = Intent(LOCALBROADCASE_MESSAGE)
                        broadcast.sendBroadcast(intent)
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
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ACTION_NOTIFYACTION_REQUEST_CODE -> {

                val notificationDB = AppDatabase(requireContext()).notificationdao()
                val currentNotify = notificationDB.getNotifybyMsgID(objIntentNotify!!.messageID)
                if(currentNotify!= null) {
                    if (resultCode == Activity.RESULT_OK) {
                        val bundle = Bundle()
                        bundle.putParcelable("InviteOrderInfo", currentNotify.copy())
                        val I = Intent(context, JoinOrderActivity::class.java)
                        I.putExtras(bundle)
                        startActivity(I)

                    } else if (resultCode == Activity.RESULT_CANCELED) { }
                }
                else
                {
                    Toast.makeText(activity, "Get Notify Error", Toast.LENGTH_LONG).show()
                }
                objIntentNotify = null
            }
            else -> {
                println("no handler onActivityReenter")
            }
        }
    }
}