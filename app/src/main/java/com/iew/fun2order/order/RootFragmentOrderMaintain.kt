package com.iew.fun2order.order

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.DATATIMEFORMAT_NORMAL
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_ACCEPT
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_EXPIRE
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_WAIT
import java.text.SimpleDateFormat
import java.util.*


class RootFragmentOrderMaintain(var _menuorder: USER_MENU_ORDER) : Fragment() , IAdapterOnClick {

    private var lstMaintainStatus: MutableList<ItemsLV_OrderMaintain> = mutableListOf()
    private lateinit var menuOrder: USER_MENU_ORDER
    private lateinit var menuOrderOwnerID : String
    private lateinit var menuOrderNumber : String
    private lateinit var rcvMaintain: RecyclerView
    private lateinit var broadcast: LocalBroadcastManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater!!.inflate(R.layout.fragment_ordermaintain, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menuOrder = _menuorder.copy()
        menuOrderOwnerID = menuOrder.orderOwnerID!!
        menuOrderNumber = menuOrder.orderNumber!!

    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(messageReceiver, IntentFilter("UpdateMessage"))

    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            rcvMaintain = it.findViewById<RecyclerView>(R.id.RecycleViewMaintain)
            broadcast = LocalBroadcastManager.getInstance(it)
        }

        checkMaintainStatus()
        rcvMaintain.layoutManager = LinearLayoutManager(requireActivity())
        rcvMaintain.adapter = AdapterRC_OrderMaintain(requireContext(), lstMaintainStatus, this)

    }

    private fun checkMaintainStatus() {
        lstMaintainStatus.clear()
        menuOrder.contentItems?.forEach ()
        {
            if(it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT) {
                val orderContent = it.orderContent
                val maintainStatus = ItemsLV_OrderMaintain(
                    orderContent.itemOwnerID!!,
                    orderContent.menuProductItems!!.toMutableList(),
                    orderContent.payCheckedFlag!!,
                    orderContent.payNumber!!,
                    it.orderContent.payTime!!
                )
                lstMaintainStatus.add(maintainStatus)
            }
        }
    }

    private var messageReceiver = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val munuOrder = p1?.getParcelableExtra<USER_MENU_ORDER>("userMenuOrder")
            if(munuOrder!= null) {
                menuOrder = munuOrder.copy()
                checkMaintainStatus()
                if(rcvMaintain.adapter !=null)
                {
                    rcvMaintain.adapter!!.notifyDataSetChanged()
                }
            }
        }
    }




    override fun onClick(sender: String, pos: Int, type: Int) {

        when(sender)
        {
            "edit" -> {
                editOrder(sender,pos,type)
            }

            "detail"->
            {
                showOrderDetail(sender,pos,type)
            }


        }
    }



    private fun editOrder(sender: String, pos: Int, type: Int)
    {
        val alert = AlertDialog.Builder(requireContext())
        var editTextName: EditText? = null

        with(alert) {
            setTitle("請輸入收取金額")
            editTextName = EditText(requireContext())
            editTextName!!.hint = "輸入金額"
            editTextName!!.inputType = InputType.TYPE_CLASS_NUMBER
            setPositiveButton("確定") { dialog, _ ->
                dialog.dismiss()
                val payNumber: String = editTextName!!.text.toString()
                if (!payNumber.isNullOrBlank()) {

                    lstMaintainStatus[pos].payCheckFlag = true
                    lstMaintainStatus[pos].payTime = SimpleDateFormat("yyyyMMddHHmmssSSS").format(Date())
                    lstMaintainStatus[pos].payNumber = payNumber.toInt()
                    rcvMaintain.adapter!!.notifyDataSetChanged()

                    //-------- Update Firebase ------
                    val menuPath = "USER_MENU_ORDER/${menuOrderOwnerID}/${menuOrderNumber}/contentItems"
                    val database = Firebase.database
                    val myRef = database.getReference(menuPath)
                    myRef.addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            dataSnapshot.children.forEach()
                            {
                                val member = it.getValue(ORDER_MEMBER::class.java)
                                if (member!!.memberID == lstMaintainStatus[pos].userUUID) {
                                    member.orderContent.payCheckedFlag = true
                                    member.orderContent.payTime = DATATIMEFORMAT_NORMAL.format(Date())
                                    member.orderContent.payNumber = payNumber.toInt()
                                    it.ref.setValue(member)

                                    refreshMenuOrder()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
            }
            setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = alert.create()
        dialog.setView(editTextName,  50 ,10, 50 , 10)
        dialog.show()

    }


    private fun showOrderDetail(sender: String, pos: Int, type: Int)
    {
        val userproduct = lstMaintainStatus[pos]
        var referenceItemData = ""
        var recipeItems = ""
        userproduct.userContentProduct.forEach {
            recipeItems = ""
            it.menuRecipes?.forEach {recipe->
                recipe.recipeItems?.forEach{recipeItem->
                    if(recipeItem.checkedFlag == true)
                    {
                        recipeItems = recipeItems + recipeItem.recipeName + " "
                    }
                }
            }
            var referenceItems = "${it.itemName}: ${recipeItems} * ${it.itemQuantity}"
            if(it.itemComments != "")
            {
                referenceItems +=  " [ ${it.itemComments} ]"
            }
            referenceItemData += "${referenceItems}\n"
        }

        val notifyAlert = AlertDialog.Builder(requireContext()).create()
        notifyAlert.setTitle("訂單內容")
        notifyAlert.setMessage(referenceItemData)
        notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
        }
        notifyAlert.show()
    }


    private fun refreshMenuOrder()
    {
        ProgressDialogUtil.showProgressDialog(context);
        val userMenuOrderPath = "USER_MENU_ORDER/${FirebaseAuth.getInstance().currentUser!!.uid.toString()}/${_menuorder.orderNumber}"
        val database = Firebase.database
        val myRef = database.getReference(userMenuOrderPath)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val tmpMenuOrder = dataSnapshot.getValue(USER_MENU_ORDER::class.java)
                if (tmpMenuOrder != null) {
                    sendMenuOrderRefresh(tmpMenuOrder)
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

    private fun sendMenuOrderRefresh(userMenuOrder: USER_MENU_ORDER) {
        val intent = Intent("UpdateMessage")
        intent.putExtra("userMenuOrder", userMenuOrder)
        broadcast.sendBroadcast(intent)
    }


}



