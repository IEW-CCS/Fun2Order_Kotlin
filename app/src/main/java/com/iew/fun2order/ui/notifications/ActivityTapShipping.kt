package com.iew.fun2order.ui.notifications

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.R
import com.iew.fun2order.db.entity.entityNotification
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_ACCEPT
import com.iew.fun2order.utility.MENU_ORDER_REPLY_STATUS_REJECT
import kotlinx.android.synthetic.main.activity_check_notification.*
import kotlinx.android.synthetic.main.activity_tap_message.*
import kotlinx.android.synthetic.main.activity_tap_message.groupbuy_Title
import kotlinx.android.synthetic.main.activity_tap_message.txtAttendance
import kotlinx.android.synthetic.main.activity_tap_message.txtBrand
import kotlinx.android.synthetic.main.activity_tap_message.txtDescription
import kotlinx.android.synthetic.main.activity_tap_message.txtDrafter
import kotlinx.android.synthetic.main.activity_tap_message.txtEndtime
import kotlinx.android.synthetic.main.activity_tap_message.txtStarttime
import kotlinx.android.synthetic.main.activity_tap_shipping.*
import kotlinx.android.synthetic.main.row_selectedproduct.view.*
import java.text.ParseException
import java.text.SimpleDateFormat

class ActivityTapShipping : AppCompatActivity() {


    private var mFirebaseUserOrder: USER_MENU_ORDER? = null
    private val lstSelectedProduct: MutableList<MENU_PRODUCT> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tap_shipping)

        supportActionBar?.hide()
        groupbuy_Title.text = ""
        txtBrand.text = ""
        txtDrafter.text = ""
        txtArrivalTime.text = ""
        txtDeliveryPlace.text = ""
        txtDescription.text = ""
        txtStatus.text= ""
        txtSelectItemTitle.text = ""

        intent?.extras?.let {

            val values = it.getParcelable("Notification") as entityNotification
            groupbuy_Title.text = "到貨通知"
            txtBrand.text = values.brandName ?: ""
            txtDrafter.text = values.orderOwnerName ?: ""
            txtDescription.text = values.messageDetail ?: ""
            txtDeliveryPlace.text = values.shippingLocation ?: ""
            txtArrivalTime.text = values.shippingDate ?: ""

            lstSelectedProduct.clear()
            loadFireBaseMenuOrder(values.orderOwnerID, values.orderNumber, values.orderOwnerName)
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

                    val notifyAlert = AlertDialog.Builder(this@ActivityTapShipping).create()
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

                val notifyAlert = AlertDialog.Builder(this@ActivityTapShipping).create()
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
            val sdfDecode = SimpleDateFormat("yyyyMMddHHmmssSSS")
            val sdfEncode = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")
            mFirebaseUserOrder!!.contentItems?.forEach { orderMember ->
                if (orderMember.memberID == FirebaseAuth.getInstance().currentUser!!.uid.toString() && orderMember.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT) {
                    val refProductItems = orderMember.orderContent.menuProductItems?.toMutableList() ?: mutableListOf()
                    refProductItems.forEach()
                    {
                        txtSelectItemTitle.text = "已訂購的產品列表"
                        lstSelectedProduct.add(it)
                    }

                    if ( orderMember.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_ACCEPT) {
                        val replyStatus = "參加"
                        val replyDateTime = sdfDecode.parse(orderMember.orderContent.createTime)
                        val formatReplyTime = sdfEncode.format(replyDateTime).toString()
                        txtStatus.text = "已於 $formatReplyTime 回覆 $replyStatus "
                        txtDescription.setTextColor(Color.BLUE)
                    } else if ( orderMember.orderContent.replyStatus == MENU_ORDER_REPLY_STATUS_REJECT) {
                        val replyStatus = "不參加"
                        val replyDateTime = sdfDecode.parse(orderMember.orderContent.createTime)
                        val formatReplyTime = sdfEncode.format(replyDateTime).toString()
                        txtStatus.text = "已於 $formatReplyTime 回覆 $replyStatus "
                        txtDescription.setTextColor(Color.RED)
                    }

                    lstSelectedProduct.forEach()
                    { menuProduct ->
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
                        layoutSelectItems.addView(itemView)
                    }
                }
            }
        }
    }
}
