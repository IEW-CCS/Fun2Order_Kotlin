package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DETAIL_STORE_USER_CONTROL(

    @SerializedName("brandName")
    var brandName   : String = "",
    @SerializedName("storeID")
    var storeID   : Int = 0,
    @SerializedName("storeName")
    var storeName   : String = "",
    @SerializedName("userName")
    var userName   : String = "",
    @SerializedName("userEmail")
    var userEmail   : String = "",
    @SerializedName("userPassword")
    var userPassword   : String = "",
    @SerializedName("userType")
    var userType   : String = "",
    @SerializedName("userAccessLevel")
    var userAccessLevel   : Int = 0,
    @SerializedName("userID")
    var userID   : String = "",
    @SerializedName("userToken")
    var userToken   : String = "",
    @SerializedName("loginStatus")
    var loginStatus   : String = ""


) : Parcelable