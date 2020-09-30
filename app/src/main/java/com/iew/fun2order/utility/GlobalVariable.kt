package com.iew.fun2order.utility

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

const val ACTION_CAMERA_REQUEST_CODE = 100
const val ACTION_ALBUM_REQUEST_CODE  = 101
const val ACTION_CONTACT_REQUEST_CODE  = 102
const val ACTION_LOCATION_REQUEST_CODE  = 103

const val ACTION_ADD_GROUP_REQUEST_CODE = 300
const val ACTION_MODIFY_GROUP_REQUEST_CODE = 301
const val ACTION_ADD_MEMBER_REQUEST_CODE = 400
const val ACTION_ADD_FRIEND_BY_CONTACT_REQUEST_CODE = 401
const val ACTION_NOTIFYACTION_REQUEST_CODE = 500
const val ACTION_JOINORDER_CODE = 600


const val ACTION_ADDPRODUCT_CODE = 1000
const val ACTION_ADDREFERENCE_CODE = 1001
const val ACTION_SHIPPING_CAR_CODE = 1002

const val ACTION_ADDRECIPE_CODE = 2000

const  val  NOTIFICATION_TYPE_MESSAGE_DUETIME = "DUETIME"
const  val  NOTIFICATION_TYPE_MESSAGE_INFORMATION = "INFO"
const  val  NOTIFICATION_TYPE_ACTION_JOIN_ORDER = "JOIN"
const  val  NOTIFICATION_TYPE_SHARE_MENU = "SHARE_MENU"
const  val  NOTIFICATION_TYPE_ACTION_JOIN_NEW_FRIEND = "NEW_FRIEND"
const  val  NOTIFICATION_TYPE_SHIPPING_NOTICE = "SHIPPING"
const  val  NOTIFICATION_TYPE_CHANGE_DUETIME = "CHANGE_DUETIME"
const  val  NOTIFICATION_TYPE_ACTIVITY = "ACTIVITY"
const  val  NOTIFICATION_TYPE_VOTE = "VOTE"
const  val  NOTIFICATION_TYPE_INVESTIGATION = "INVESTIGATION"


const val MENU_ORDER_REPLY_STATUS_WAIT: String = "WAIT"
const val MENU_ORDER_REPLY_STATUS_ACCEPT: String = "ACCEPT"
const val MENU_ORDER_REPLY_STATUS_REJECT: String = "REJECT"
const val MENU_ORDER_REPLY_STATUS_EXPIRE: String = "EXPIRE"

const val MENU_ORDER_REPLY_STATUS_INTERACTIVE: String = "INTERACTIVE"

const val  LOCALBROADCASE_MESSAGE:String ="MESSAGE"
const val  LOCALBROADCASE_JOIN:String ="JOIN_ORDER"
const val  LOCALBROADCASE_FRIEND:String ="ADD_FRIEND"
const val  LOCALBROADCASE_SHAREMENU:String ="SHARE_MENU"
const val  LOCALBROADCASE_CHANGEDUETIME:String ="CHANGE_DUETIME"


const val DELIVERY_TYPE_TAKEOUT: String = "TAKEOUT"
const val DELIVERY_TYPE_DELIVERY: String = "DELIVERY"


@SuppressLint("SimpleDateFormat")
val DATATIMEFORMAT_NORMAL = SimpleDateFormat("yyyyMMddHHmmssSSS")
@SuppressLint("SimpleDateFormat")
val DATATIMEFORMAT_CHINESE_TYPE1 = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")



const val ORDER_STATUS_INIT: String = "INIT"          // Initial and editing state of the order
const val ORDER_STATUS_NEW: String = "NEW"            // User create the real order and send to store
const val ORDER_STATUS_ACCEPT: String = "ACCEPT"      // Store manager confirms to receive the real order
const val ORDER_STATUS_REJECT: String = "REJECT"      // Store manager rejects the real order
const val ORDER_STATUS_INPROCESS: String = "INPR"     // Store starts making the content of the order
const val ORDER_STATUS_PROCESSEND: String = "PREN"        // Store gets the order ready to take out or deliver
const val ORDER_STATUS_DELIVERY: String = "DELIVERY"  // Products in delivery
const val ORDER_STATUS_CLOSE: String = "CLOSE"        // Customer receives products and


const val STORE_NOTIFICATION_TYPE_NEW_ORDER = "NEW_ORDER"
const val STORE_NOTIFICATION_TYPE_BRAND_MESSAGE = "BRAND_MESSAGE"