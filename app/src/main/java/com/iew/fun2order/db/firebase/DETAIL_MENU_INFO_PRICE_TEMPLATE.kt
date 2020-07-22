package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DETAIL_MENU_INFO_PRICE_TEMPLATE(
    @SerializedName("allowMultiSelectionFlag")
    var allowMultiSelectionFlag:  Boolean,
    @SerializedName("mandatoryFlag")
    var mandatoryFlag:  Boolean,
    @SerializedName("recipeList")
    var recipeList:   List<DETAIL_MENU_INFO_RECIPES>?,
    @SerializedName("standAloneProduct")
    var standAloneProduct:  Boolean,
    @SerializedName("templateName")
    var templateName:  String,
    @SerializedName("templateSequence")
    var templateSequence:  Int

) : Parcelable
