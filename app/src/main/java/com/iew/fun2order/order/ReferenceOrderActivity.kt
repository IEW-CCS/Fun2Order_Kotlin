package com.iew.fun2order.order

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.R
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.ui.my_setup.IAdapterOnClick
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_ACCEPT
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_REJECT
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_WAIT
import kotlinx.android.synthetic.main.row_history_invite.view.*
import java.text.SimpleDateFormat


class ReferenceOrderActivity : AppCompatActivity(), IAdapterOnClick {

    private lateinit var txtOrderBrand: TextView
    private lateinit var txtOrderOwner: TextView
    private lateinit var txtOrderStartTime: TextView
    private lateinit var txtOrderEndTime: TextView
    private lateinit var txtOrderExpected: TextView
    private lateinit var txtOrderNotReply: TextView
    private lateinit var txtOrderJoined: TextView
    private lateinit var txtOrderNotJoin: TextView

    private lateinit var rcvOrderReference: RecyclerView
    private lateinit var txtFollow: TextView

    private val  lstReferenceOrderList: MutableList<ItemsLV_ReferenceOrder> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reference_order)
        supportActionBar?.hide()

        txtOrderBrand = findViewById(R.id.OrderBrandName)
        txtOrderOwner = findViewById(R.id.orderOwner)
        txtOrderStartTime = findViewById(R.id.orderStartTime)
        txtOrderEndTime = findViewById(R.id.orderDueTime)
        txtOrderExpected = findViewById(R.id.orderExpectedCount)
        txtOrderNotReply = findViewById(R.id.orderNotReply)
        txtOrderJoined = findViewById(R.id.orderJoinedCount)
        txtOrderNotJoin = findViewById(R.id.orderNotJoin)
        rcvOrderReference = findViewById(R.id.RecycleViewReference)
        txtFollow = findViewById(R.id.OrderFollow)

        intent?.extras?.let {
            val menuOrderOwnerID = it.getString("MenuOrderOwnerID")
            val menuOrderNumber = it.getString("MenuOrderNumber")
            val mFirebaseUserMenu = it.get("MenuOrder") as USER_MENU_ORDER

            txtOrderBrand.text = mFirebaseUserMenu?.brandName
            txtOrderOwner.text = mFirebaseUserMenu?.orderOwnerName

            val sdfDecode = SimpleDateFormat("yyyyMMddHHmmssSSS")
            val sdfEncode = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")

            if (mFirebaseUserMenu?.createTime != "") {
                val startDateTime = sdfDecode.parse(mFirebaseUserMenu?.createTime)
                val formatStartDatetime = sdfEncode.format(startDateTime).toString()
                txtOrderStartTime.text = formatStartDatetime
            } else {
                txtOrderStartTime.text = ""
            }

            if (mFirebaseUserMenu?.dueTime != "") {
                val dueDateTime = sdfDecode.parse(mFirebaseUserMenu?.dueTime)
                val formatDueDatetime = sdfEncode.format(dueDateTime).toString()
                txtOrderEndTime.text = formatDueDatetime
            } else {
                txtOrderEndTime.text = "無逾期時間"
            }

            val orderExpect = mFirebaseUserMenu?.contentItems?.count() ?: 0
            val orderWait =
                mFirebaseUserMenu?.contentItems?.filter { it -> it.orderContent!!.replyStatus == MENU_ORDER_REPLY_STATUS_WAIT }
                    ?.count() ?: 0
            val orderJoin =
                mFirebaseUserMenu?.contentItems?.filter { it -> it.orderContent!!.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT }
                    ?.count() ?: 0
            val orderReject =
                mFirebaseUserMenu?.contentItems?.filter { it -> it.orderContent!!.replyStatus == MENU_ORDER_REPLY_STATUS_REJECT }
                    ?.count() ?: 0

            txtOrderExpected.text = orderExpect.toString()
            txtOrderNotReply.text = orderWait.toString()
            txtOrderJoined.text = orderJoin.toString()
            txtOrderNotJoin.text = orderReject.toString()

            mFirebaseUserMenu?.contentItems?.forEach { orderMember ->
                if (orderMember.memberID != FirebaseAuth.getInstance().currentUser!!.uid.toString() && orderMember.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT) {
                    val refOwner = orderMember.orderContent.itemOwnerName ?: ""
                    val refProductItems = orderMember.orderContent.menuProductItems?.toMutableList()
                        ?: mutableListOf()
                    if (refOwner != "") {
                        lstReferenceOrderList.add(
                            ItemsLV_ReferenceOrder(
                                refOwner,
                                refProductItems,
                                false
                            )
                        )
                    }
                }
            }
        }

        rcvOrderReference.layoutManager = LinearLayoutManager(this)
        rcvOrderReference.adapter =
            AdapterRC_ReferenceOrder(
                this,
                lstReferenceOrderList,
                this
            )
        txtFollow.setOnClickListener {

            val referenceList =
                lstReferenceOrderList.filter { followed -> followed.followone }.toMutableList()
            val referenceProducts = mutableListOf<MENU_PRODUCT>()

            if (referenceList.count() != 0) {
                referenceList.forEach { productlist ->
                    productlist.referenceProduct.toMutableList().forEach()
                    { it ->
                        referenceProducts.add(it.copy())
                    }
                }

            }

            if (referenceProducts.count() != 0) {
                val bundle = Bundle()
                bundle.putParcelableArrayList("referenceProducts", ArrayList(referenceProducts))
                val intent = Intent().putExtras(bundle)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onClick(sender: String, pos: Int, type: Int) {
        when(type) {
            0 -> {
                when(sender){
                    "followOne" ->
                    {
                        lstReferenceOrderList[pos].followone = lstReferenceOrderList[pos].followone == false
                        rcvOrderReference.adapter!!.notifyDataSetChanged()
                    }

                    //查看訂單細節
                    "SelectProduct" ->
                    {
                        val user = lstReferenceOrderList[pos].referenceOwner
                        var referenceItemData = ""
                        var recipeItems = ""
                        lstReferenceOrderList[pos].referenceProduct.forEach {
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


                        val notifyAlert = AlertDialog.Builder(this).create()
                        notifyAlert.setTitle("${user} 的訂單內容")
                        notifyAlert.setMessage(referenceItemData)
                        notifyAlert.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { _, i ->
                       }
                       notifyAlert.show()
                    }
                }
            }
        }
    }
}


