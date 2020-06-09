package com.iew.fun2order.order
import android.view.View
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ItemsLV_OrderProduct (var itemName: String, var itemPrice:String, var sequenceNumber: String): Parcelable



