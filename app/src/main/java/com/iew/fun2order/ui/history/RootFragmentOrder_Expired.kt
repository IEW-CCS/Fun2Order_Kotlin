package com.iew.fun2order.ui.history

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.ParseException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.ProgressDialogUtil
import com.iew.fun2order.order.OrderDetailActivity
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.notifications.ItemsLV_Ads
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_EXPIRE
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_WAIT
import kotlinx.android.synthetic.main.row_history_order.view.*
import java.text.SimpleDateFormat
import java.util.*


class RootFragmentOrder_Expired() : Fragment(), IAdapterOnClick {

    var listOrders: MutableList<Any> = mutableListOf()
    var rcvOrders_expired: RecyclerView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater!!.inflate(R.layout.fragment_order_expired, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            rcvOrders_expired = it.findViewById<RecyclerView>(R.id.RecycleView_order_expired)
        }
        rcvOrders_expired!!.layoutManager = LinearLayoutManager(requireActivity())
        rcvOrders_expired!!.adapter = RCAdapter_Order_WithBannerAds(requireContext(), listOrders, this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProgressDialogUtil.showProgressDialog(context);
        val userMenuOrderPath = "USER_MENU_ORDER/${FirebaseAuth.getInstance().currentUser!!.uid.toString()}/"
        val database = Firebase.database
        val myRef = database.getReference(userMenuOrderPath)
        val sdfDecode = SimpleDateFormat("yyyyMMddHHmmssSSS")
        val sdfEncode = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")
        listOrders.clear()
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach()
                {
                    it->
                    val userOrder =  it.getValue(USER_MENU_ORDER::class.java)
                    if (userOrder != null) {
                        try {
                            val tmpStartTime = sdfDecode.parse(userOrder.createTime)
                            val startTime = sdfEncode.format(tmpStartTime).toString()
                            val joinCount = userOrder.contentItems!!.count().toString()
                            var dueTime = ""
                            var expired = false

                            if (userOrder.dueTime != null) {
                                var timeExpired = timeCompare(userOrder.dueTime!!)
                                val tmpDueTime = sdfDecode.parse(userOrder.dueTime)
                                dueTime = sdfEncode.format(tmpDueTime).toString()
                                if (timeExpired) {
                                    expired = true
                                }
                            }
                            if(expired)
                            {
                                listOrders.add(ItemsLV_Order(userOrder.orderNumber!!, userOrder.brandName!!, startTime, dueTime, joinCount, expired))
                            }
                        } catch (ex: Exception) {
                        }
                    }
                }
                //---因為會反轉所以最後一筆放廣告 ----
                listOrders.add(ItemsLV_Ads(getString(R.string.banner_ad_unit_id)))
                listOrders.reverse()
                rcvOrders_expired!!.adapter!!.notifyDataSetChanged()
                ProgressDialogUtil.dismiss();
            }

            override fun onCancelled(error: DatabaseError) {
                ProgressDialogUtil.dismiss();
            }
        })
    }

    override fun onClick(sender: String, pos: Int, type: Int) {

        if (type == 0) {

            //------ 點擊的當下馬上去更新狀態 --------
            ProgressDialogUtil.showProgressDialog(context);
            var _menuorder = (listOrders[pos] as ItemsLV_Order).copy()
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
                            if (timeExpired) {
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
                        ProgressDialogUtil.dismiss();
                        //----- 更新完以後轉換畫面 ------
                        val bundle = Bundle()
                        bundle.putParcelable("menuOrder", menuOrder.copy())
                        val intent = Intent(context, OrderDetailActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                    else
                    {
                        ProgressDialogUtil.dismiss();
                        val alert = AlertDialog.Builder(context!!)
                        with(alert) {
                            setTitle("訂單資料不存在")
                            setMessage("訂單編號 : ${_menuorder.orderNumber}")
                            setPositiveButton("確定") { dialog, _ ->
                            }
                        }
                        val dialog = alert.create()
                        dialog.show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    ProgressDialogUtil.dismiss();
                    val alert = AlertDialog.Builder(context!!)
                    with(alert) {
                        setTitle("訂單資料讀取異常")
                        setMessage("訂單編號 : ${_menuorder.orderNumber}")
                        setPositiveButton("確定") { dialog, _ ->
                        }
                    }
                    val dialog = alert.create()
                    dialog.show()
                }
            })
        }
        else if (type == 1)
        {
            val removeOrderNumber = (listOrders[pos] as ItemsLV_Order).orderNumber
            val removeOrderBrand = (listOrders[pos] as ItemsLV_Order).brandName
            checkRemoveOrderInfo(removeOrderBrand!!, removeOrderNumber!!, pos)
        }

    }

    private fun checkRemoveOrderInfo(OrderBrand: String, OrderNumber: String, Position: Int) {
        val alert = AlertDialog.Builder(context!!)
        with(alert) {
            setTitle("確認刪除訂單 : $OrderBrand")
            setMessage("訂單編號 : $OrderNumber")
            setPositiveButton("確定") { dialog, _ ->
                try {
                    val userMenuOrderPath = "USER_MENU_ORDER/${FirebaseAuth.getInstance().currentUser!!.uid.toString()}/$OrderNumber"
                    val database = Firebase.database
                    database.getReference(userMenuOrderPath).removeValue()
                    listOrders.removeAt(Position)
                    rcvOrders_expired!!.adapter!!.notifyDataSetChanged()
                }
                catch (e: Exception)
                {
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
}



