package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MenuOrderDeliveryInformation(

    @SerializedName("deliveryType")
    var deliveryType: String = "",
    @SerializedName("deliveryTime")
    var deliveryTime: String = "",
    @SerializedName("deliveryAddress")
    var deliveryAddress: String = "",
    @SerializedName("contactName")
    var contactName: String = "",
    @SerializedName("contactPhoneNumber")
    var contactPhoneNumber: String = "",
    @SerializedName("separatePackageFlag")
    var separatePackageFlag: Boolean? = false

): Parcelable


@Parcelize
data class OrderHistoryRecord(

    @SerializedName("claimTimeStamp")
    var claimTimeStamp: String = "",
    @SerializedName("claimUser")
    var claimUser: String = "",
    @SerializedName("claimStatus")
    var claimStatus: String = ""

): Parcelable
