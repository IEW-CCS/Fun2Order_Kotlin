package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SUGGEST_MENU_INFO(
    @SerializedName("brandImageURL")
    var brandImageURL:  String,
    @SerializedName("brandName")
    var brandName:  String,
    @SerializedName("suggestedDateTime")
    var suggestedDateTime:  String,
    @SerializedName("suggestedUserID")
    var suggestedUserID:  String

) : Parcelable
