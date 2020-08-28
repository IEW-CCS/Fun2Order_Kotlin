package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DETAIL_BRAND_EVENT(
    @SerializedName("eventTitle")
    var eventTitle   : String = "",
    @SerializedName("eventSubTitle")
    var eventSubTitle  : String? = null,
    @SerializedName("eventType")
    var eventType       : String? = null,
    @SerializedName("eventImageURL")
    var eventImageURL: String? = null,
    @SerializedName("eventContentURL")
    var eventContentURL: String? = null,
    @SerializedName("eventContent")
    var eventContent  : String? = null,
    @SerializedName("publishDate")
    var publishDate  : String = ""

) : Parcelable