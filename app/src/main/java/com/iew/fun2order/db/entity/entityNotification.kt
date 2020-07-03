package com.iew.fun2order.db.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(indices = arrayOf(Index(value = ["messageID"], unique = true)))
data class entityNotification (
    @PrimaryKey(autoGenerate = true)
    val id:Long?= null,
    @ColumnInfo(name = "messageID")
    var messageID:String = "",
    @ColumnInfo(name = "messageTitle")
    var messageTitle:String = "",
    @ColumnInfo(name = "messageBody")
    var messageBody:String = "",
    @ColumnInfo(name = "notificationType")
    var notificationType:String = "",
    @ColumnInfo(name = "receiveTime")
    var receiveTime:String = "",
    @ColumnInfo(name = "orderOwnerID")
    var orderOwnerID:String = "",
    @ColumnInfo(name = "orderOwnerName")
    var orderOwnerName:String = "",
    @ColumnInfo(name = "menuNumber")
    var menuNumber:String = "",
    @ColumnInfo(name = "orderNumber")
    var orderNumber:String = "",
    @ColumnInfo(name = "dueTime")
    var dueTime:String = "",
    @ColumnInfo(name = "brandName")
    var brandName:String = "",
    @ColumnInfo(name = "attendedMemberCount")
    var attendedMemberCount:String = "",
    @ColumnInfo(name = "messageDetail")
    var messageDetail:String = "",
    @ColumnInfo(name = "isRead")
    var isRead:String = "",
    @ColumnInfo(name = "replyStatus")
    var replyStatus:String = "",
    @ColumnInfo(name = "replyTime")
    var replyTime:String = "",
    @ColumnInfo(name = "shippingDate")
    var shippingDate: String? = null,
    @ColumnInfo(name = "shippingLocation")
    var shippingLocation: String? = null

): Parcelable