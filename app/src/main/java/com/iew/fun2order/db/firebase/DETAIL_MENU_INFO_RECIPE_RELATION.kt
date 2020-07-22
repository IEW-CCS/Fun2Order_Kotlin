package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DETAIL_MENU_INFO_RECIPE_RELATION(
    @SerializedName("itemRelation")
    var itemRelation:  List<Boolean>,
    @SerializedName("templateSequence")
    var templateSequence:  Int

) : Parcelable
