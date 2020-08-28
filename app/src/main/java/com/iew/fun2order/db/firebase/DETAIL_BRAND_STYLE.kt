package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DETAIL_BRAND_STYLE(
    @SerializedName("backgroundColor")
    var backgroundColor:  List<Double>? = null,
    @SerializedName("tabBarColor")
    var tabBarColor:  List<Double>? = null,
    @SerializedName("textTintColor")
    var textTintColor:  List<Double>? = null

) : Parcelable