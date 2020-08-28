package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DETAIL_BRAND_STORE(
    @SerializedName("storeID")
    var storeID   : Int = 0,
    @SerializedName("storeName")
    var storeName  : String = "",
    @SerializedName("storeMenuNumber")
    var storeMenuNumber : String = "",
    @SerializedName("storeCategory")
    var storeCategory: String? = null,
    @SerializedName("storeSubCategory")
    var storeSubCategory: String? = null,
    @SerializedName("storeDescription")
    var storeDescription  : String? = null,
    @SerializedName("storeLongitude")
    var storeLongitude  : String?  = null,
    @SerializedName("storeLatitude")
    var storeLatitude  : String?  = null,
    @SerializedName("storeWebURL")
    var storeWebURL  : String?  = null,
    @SerializedName("storeFacebookURL")
    var storeFacebookURL  : String?  = null,
    @SerializedName("storeInstagramURL")
    var storeInstagramURL  : String?  = null,
    @SerializedName("storeImageURL")
    var storeImageURL  : String?  = null,
    @SerializedName("storeAddress")
    var storeAddress  : String?  = null,
    @SerializedName("storePhoneNumber")
    var storePhoneNumber  : String?  = null,
    @SerializedName("deliveryServiceFlag")
    var deliveryServiceFlag  : Boolean = false

) : Parcelable