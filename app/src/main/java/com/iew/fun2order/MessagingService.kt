package com.iew.fun2order

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.iew.fun2order.db.database.AppDatabase
import com.iew.fun2order.db.entity.entityNotification
import com.iew.fun2order.utility.*


class MessagingService : FirebaseMessagingService() {

    lateinit var broadcast: LocalBroadcastManager
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null) {
            val queryPath = "USER_PROFILE/" + mAuth.currentUser!!.uid.toString()
            val database = Firebase.database
            val myRef = database.getReference(queryPath)
            myRef.child("tokenID").setValue(token.toString());
        }
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        super.onMessageReceived(msg)

        var contentText = ""
        broadcast = LocalBroadcastManager.getInstance(this!!)

        Log.e("Firebase message", "OnMessageReceive From: ${msg.from}")

        msg.data.isNotEmpty().let {
            Log.d("Firebase", "Message data payload: " + msg.data)
            if (msg.data.keys.contains("messageTitle")) {
                try {

                    val notification: entityNotification = entityNotification()
                    notification.messageID        = msg.messageId.toString()
                    notification.messageTitle     = msg.data["messageTitle"]!!.toString()
                    notification.messageBody      = msg.data["messageBody"]!!.toString()
                    notification.notificationType = msg.data["notificationType"]!!.toString()
                    notification.receiveTime      = msg.data["receiveTime"]!!.toString()
                    notification.orderOwnerID     = msg.data["orderOwnerID"]!!.toString()
                    notification.orderOwnerName   = msg.data["orderOwnerName"]!!.toString()
                    notification.menuNumber       = msg.data["menuNumber"]!!.toString()
                    notification.orderNumber      = msg.data["orderNumber"]!!.toString()
                    notification.dueTime          = msg.data["dueTime"]!!.toString()
                    notification.brandName        = msg.data["brandName"]!!.toString()
                    notification.attendedMemberCount = msg.data["attendedMemberCount"]!!.toString()
                    notification.messageDetail    = msg.data["messageDetail"]!!.toString()
                    notification.isRead           = msg.data["isRead"]!!.toString()
                    notification.replyStatus      = MENU_ORDER_REPLY_STATUS_WAIT

                    notification.shippingDate = msg.data["shippingDate"]?.toString()
                    notification.shippingLocation = msg.data["shippingLocation"]?.toString()

                    //------ 發送System Tray 通知 -------
                    sendNotificationchannel( notification.messageTitle,notification.messageBody)

                    val notificationDB = AppDatabase(this).notificationdao()


                    if(notification.notificationType == NOTIFICATION_TYPE_ACTION_JOIN_NEW_FRIEND)
                    {
                        addFriend(notification)
                        notification.replyStatus = MENU_ORDER_REPLY_STATUS_INTERACTIVE
                        notificationDB.insertRow(notification)
                    }
                    else if(notification.notificationType == NOTIFICATION_TYPE_SHARE_MENU)
                    {
                        shareMenu(notification)
                        notification.replyStatus = MENU_ORDER_REPLY_STATUS_INTERACTIVE
                        notificationDB.insertRow(notification)

                    }
                    else if (notification.notificationType == NOTIFICATION_TYPE_ACTION_JOIN_ORDER ||
                             notification.notificationType == NOTIFICATION_TYPE_MESSAGE_DUETIME||
                             notification.notificationType == NOTIFICATION_TYPE_MESSAGE_INFORMATION||
                             notification.notificationType == NOTIFICATION_TYPE_SHIPPING_NOTICE) {

                        if ((notification.orderOwnerID == FirebaseAuth.getInstance().currentUser!!.uid) &&
                            notification.notificationType == NOTIFICATION_TYPE_ACTION_JOIN_ORDER ) {
                            joinOrder(notification)
                            notification.isRead = "Y"
                        } else {
                            showAlert(contentText)
                        }
                        notificationDB.insertRow(notification)
                    }
                    else if(notification.notificationType == NOTIFICATION_TYPE_CHANGE_DUETIME)
                    {
                        //----Change Due Time 直接處理掉就好 -----
                        changeDueTime(notification)
                        notification.replyStatus = MENU_ORDER_REPLY_STATUS_INTERACTIVE
                        notificationDB.insertRow(notification)
                    }

                  } catch (e: Exception) {
                      Log.d("Firebase", "Insert Notification Failed. ${e.localizedMessage}")
                }
            }
        }


    }


    private fun showAlert(message: String) {
        val intent = Intent(LOCALBROADCASE_MESSAGE)
        intent.putExtra("fcmMessage", message)
        broadcast.sendBroadcast(intent)
    }

    private fun joinOrder(notify: entityNotification) {
        val intent = Intent(LOCALBROADCASE_JOIN)
        intent.putExtra("joinOrderMessage", notify)
        broadcast.sendBroadcast(intent)
    }

    private fun addFriend(notify: entityNotification) {
        val intent = Intent(LOCALBROADCASE_FRIEND)
        intent.putExtra("AddFriendMessage", notify)
        broadcast.sendBroadcast(intent)
    }

    private fun shareMenu(notify: entityNotification) {
        val intent = Intent(LOCALBROADCASE_SHAREMENU)
        intent.putExtra("ShareMenuMessage", notify)
        broadcast.sendBroadcast(intent)
    }

    private fun changeDueTime(notify: entityNotification) {
        val intent = Intent(LOCALBROADCASE_CHANGEDUETIME)
        intent.putExtra("ChangeDueTime", notify)
        broadcast.sendBroadcast(intent)
    }

    // Backup Resource
    private fun sendNotification(message: String) {
        val notification = NotificationCompat.Builder(this, "channel id test")
            .setSmallIcon(R.drawable.icon_cup)
            .setContentTitle("Notification from Firebase")
            .setContentText(message)
            .build()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private fun sendNotificationchannel(messageTitle: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setSmallIcon(R.drawable.icon_notify_menu)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}