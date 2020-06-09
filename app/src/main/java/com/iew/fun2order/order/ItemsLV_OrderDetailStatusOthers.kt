package com.iew.fun2order.order
import android.view.View
import android.os.Parcelable
import com.iew.fun2order.db.firebase.MENU_PRODUCT
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ItemsLV_OrderDetailStatusOthers (var userUUID: String, var userStatus:String): Parcelable



