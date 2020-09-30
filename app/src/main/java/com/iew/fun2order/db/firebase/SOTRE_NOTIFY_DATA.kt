package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SOTRE_NOTIFY_DATA(

    @SerializedName("orderOwnerID")
    var orderOwnerID   : String,
    @SerializedName("orderOwnerName")
    var orderOwnerName  : String,
    @SerializedName("orderOwnerToken")
    var orderOwnerToken : String,
    @SerializedName("orderNumber")
    var orderNumber: String,
    @SerializedName("notificationType")
    var notificationType: String,
    @SerializedName("createTime")
    var createTime  : String

) : Parcelable