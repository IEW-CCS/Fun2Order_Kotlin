package com.iew.fun2order.order
import android.view.View
import android.os.Parcelable
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ItemsLV_OrderDetailStatusAccept (var userUUID: String, var userLocation:String, var quantity: String, var userContentProduct:MutableList<MENU_PRODUCT>, var userName:String, var replyTime: String): Parcelable



