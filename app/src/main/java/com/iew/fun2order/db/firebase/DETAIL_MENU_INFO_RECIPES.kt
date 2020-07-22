package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DETAIL_MENU_INFO_RECIPES(
    @SerializedName("itemCheckedFlag")
    var itemCheckedFlag:  Boolean,
    @SerializedName("itemName")
    var itemName:  String,
    @SerializedName("itemSequence")
    var itemSequence:  Int,
    @SerializedName("optionalPrice")
    var optionalPrice:  Int,
    @SerializedName("itemDisplayFlag")
    var itemDisplayFlag:  Boolean? = true
) : Parcelable
