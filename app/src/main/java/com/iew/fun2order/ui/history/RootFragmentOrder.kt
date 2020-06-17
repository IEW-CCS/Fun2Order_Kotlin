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
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_EXPIRE
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_WAIT
import java.text.SimpleDateFormat
import java.util.*


class RootFragmentOrder() : Fragment(), IAdapterOnClick {

    var listOrders: MutableList<USER_MENU_ORDER> = mutableListOf()
    var rcvOrders: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater!!.inflate(R.layout.fragment_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            rcvOrders = it.findViewById<RecyclerView>(R.id.RecycleView_order)
        }
        rcvOrders!!.layoutManager = LinearLayoutManager(activity!!)
        rcvOrders!!.adapter = RCAdapter_Order(context!!, listOrders, this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userMenuOrderPath = "USER_MENU_ORDER/${FirebaseAuth.getInstance().currentUser!!.uid.toString()}/"
        val database = Firebase.database
        val myRef = database.getReference(userMenuOrderPath)
        listOrders.clear()
        var tmp: MutableList<USER_MENU_ORDER> = mutableListOf()
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach()
                {
                    it->
                    val userOrder =  it.getValue(USER_MENU_ORDER::class.java)
                    if(userOrder != null) {
                        tmp.add(userOrder.copy())
                    }
                }

                tmp.asReversed().forEach()
                {
                    listOrders.add(it.copy())
                }

                rcvOrders!!.adapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // Log.w(TAG, "Failed to read value.", error.toException())
            }
        })


    }

    override fun onClick(sender: String, pos: Int, type: Int) {

        if (type == 0) {

            //------ 點擊的當下馬上去更新狀態 --------

            ProgressDialogUtil.showProgressDialog(context);
            var _menuorder = listOrders[pos].copy()

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
                        bundle.putParcelable("menuOrder", listOrders[pos].copy())
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
            val removeOrderNumber = listOrders[pos].orderNumber
            val removeOrderBrand = listOrders[pos].brandName
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
                    rcvOrders!!.adapter!!.notifyDataSetChanged()
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



