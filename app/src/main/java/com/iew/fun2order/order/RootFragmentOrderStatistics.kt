package com.iew.fun2order.order

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iew.fun2order.OrderNoteBookActivity
import com.iew.fun2order.R
import com.iew.fun2order.db.entity.entityNotification
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.ui.notifications.ActivityTapMessage
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_ACCEPT
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_EXPIRE
import info.hoang8f.android.segmented.SegmentedGroup


class RootFragmentOrderStatistics(var _menuorder: USER_MENU_ORDER) : Fragment(), IAdapterOnClick {

    private lateinit var menuOrder: USER_MENU_ORDER
    private lateinit var rcvStatistics: RecyclerView
    private lateinit var mSegmentedGroupLocation: SegmentedGroup
    private lateinit var noteBook:ImageView

    private var lstAccept: MutableList<ItemsLV_OrderDetailStatusAccept> = mutableListOf()
    private var lstOthers: MutableList<ItemsLV_OrderDetailStatusOthers> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater!!.inflate(R.layout.fragment_orderstatistics, container, false)
    }


    private var messageReceiver = object: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val munuOrder = p1?.getParcelableExtra<USER_MENU_ORDER>("userMenuOrder")

            if(munuOrder!= null) {
                menuOrder = munuOrder.copy()

                if (mSegmentedGroupLocation.childCount > 0) {
                    val default = mSegmentedGroupLocation.getChildAt(0) as RadioButton
                    default.isChecked = true
                    setupAccept()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(messageReceiver, IntentFilter("UpdateMessage"))

    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(messageReceiver)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menuOrder = _menuorder.copy()


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let {
            rcvStatistics = it.findViewById<RecyclerView>(R.id.RecycleViewStatistics)
            mSegmentedGroupLocation = it.findViewById(R.id.segmentedGroupMenuType)
            noteBook = it.findViewById<ImageView>(R.id.imageNoteBook)
        }


        rcvStatistics.layoutManager = LinearLayoutManager(activity)


        if (mSegmentedGroupLocation.childCount > 0) {
            val default = mSegmentedGroupLocation.getChildAt(0) as RadioButton
            default.isChecked = true
            setupAccept()
        }

        //---- User Select Location  -----
        mSegmentedGroupLocation.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radioButton: RadioButton = group.findViewById<RadioButton>(checkedId)
            when (radioButton.id) {
                R.id.segmentedGroupAccept -> {
                    setupAccept()
                }
                R.id.segmentedGroupOther -> {
                    setupOthers()
                }
            }
        })

        noteBook.setOnClickListener()
       {
            val bundle = Bundle()
            bundle.putParcelable("USER_MENU_ORDER", menuOrder)
            val intent = Intent(context, OrderNoteBookActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }


    override fun onClick(sender: String, pos: Int, type: Int) {
        if (sender == "ACCEPTProdList" && type == 0) {

            val userproduct = lstAccept[pos]
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
            notifyAlert.setTitle("${userproduct.userName} 的訂單內容")
            notifyAlert.setMessage(referenceItemData)
            notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
            }
            notifyAlert.show()
          }

    }

    private fun setupAccept()
    {
        val orderJoin = menuOrder.contentItems!!.filter { it.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT }
        lstAccept.clear()
        orderJoin.forEach()
        {
            val userProduct =
                it.orderContent.menuProductItems?.toMutableList() ?: mutableListOf()
            val tmpAccept =
                ItemsLV_OrderDetailStatusAccept(
                    it.orderContent.itemOwnerID!!,
                    it.orderContent.location!!,
                    it.orderContent.itemQuantity!!.toString(),
                    userProduct,
                    it.orderContent.itemOwnerName!!
                )
            lstAccept.add(tmpAccept)
        }
        rcvStatistics.adapter = AdapterRC_OrderDetailStatus(requireContext(), lstAccept, this)
    }

    private fun setupOthers()
    {
        val orderOthers = menuOrder.contentItems!!.filter { it.orderContent.replyStatus != MENU_ORDER_REPLY_STATUS_ACCEPT  }
        lstOthers.clear()
        orderOthers.forEach()
        {

            val tmpOthers =
                ItemsLV_OrderDetailStatusOthers(
                    it.orderContent.itemOwnerID!!,
                    it.orderContent.replyStatus!!
                )
            lstOthers.add(tmpOthers)
        }
        rcvStatistics.adapter = AdapterRC_OrderDetailStatus(requireContext(), lstOthers, this)
    }
}



