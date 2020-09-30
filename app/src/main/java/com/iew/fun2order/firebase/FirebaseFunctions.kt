package com.iew.fun2order.firebase

import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.entity.entityFriend
import com.iew.fun2order.db.firebase.DETAIL_STORE_USER_CONTROL
import com.iew.fun2order.db.firebase.SOTRE_NOTIFY_DATA
import com.iew.fun2order.db.firebase.USER_MENU_ORDER
import com.iew.fun2order.utility.NOTIFICATION_TYPE_ACTION_JOIN_ORDER
import com.iew.fun2order.utility.ORDER_STATUS_INIT
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

fun functionTest(abc : String, callback: (Any?)->Unit) {
    val detailBrandEvent = "/DETAIL_BRAND_EVENT/上宇林"
    val database = Firebase.database
    val myRef = database.getReference(detailBrandEvent)
    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            callback(null)
        }
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            callback(dataSnapshot)
        }
    })
}


fun getFBMenuOrder(orderOwnerID : String, orderNumber : String, callback: (Any?)->Unit) {

    val menuOrderPath = "USER_MENU_ORDER/${orderOwnerID}/${orderNumber}"
    val database = Firebase.database
    val myRef = database.getReference(menuOrderPath)
    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            callback(null)
        }
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val mFirebaseUserMenuOrder = dataSnapshot.getValue(USER_MENU_ORDER::class.java)
            callback(mFirebaseUserMenuOrder)
        }
    })
}

fun getFBStoreToken(BrandName : String, StoreName : String, callback: (String?)->Unit) {

    val menuOrderPath = "STORE_USER_CONTROL/${BrandName}/"
    val database = Firebase.database
    val myRef = database.getReference(menuOrderPath)
    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {
            callback(null)
        }
        override fun onDataChange(dataSnapshot: DataSnapshot) {

            for (snapshot in dataSnapshot.children) {
                val storeInfo = snapshot.getValue(DETAIL_STORE_USER_CONTROL::class.java)
                try {

                    if(storeInfo?.storeName == StoreName)
                    {
                        callback(storeInfo?.userToken)
                    }
                } catch (e: Exception) {

                    callback(null)
                }
            }

            callback(null)
        }
    })
}



fun sendMsgToBrandStore (notifyToken : String, storeNotifyData: SOTRE_NOTIFY_DATA)
{
    val notification = JSONObject()
    val notificationHeader = JSONObject()
    val notificationBody = JSONObject()

    var title = "團購邀請"
    var body = ""

    val self = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    notificationHeader.put("title", title)
    notificationHeader.put("body", body)   //Enter your notification message

    notificationBody.put("messageID", "")      //Enter
    notificationBody.put("messageTitle", title)   //Enter
    notificationBody.put("messageBody", body)    //Enter
    notificationBody.put("orderOwnerID", storeNotifyData.orderOwnerID )   //Enter
    notificationBody.put("orderOwnerName", storeNotifyData.orderOwnerName)   //Enter
    notificationBody.put("orderOwnerToken", storeNotifyData.orderOwnerToken)   //Enter
    notificationBody.put("orderNumber", storeNotifyData.orderNumber)   //Enter
    notificationBody.put("notificationType", storeNotifyData.notificationType)   //Enter
    notificationBody.put("createTime", storeNotifyData.createTime)   //Enter

    // your notification message
    notification.put("to", notifyToken)
    notification.put("notification", notificationHeader)
    notification.put("data", notificationBody)

    Thread.sleep(100)
    com.iew.fun2order.MainActivity.sendFirebaseNotificationMulti(notification)
}
