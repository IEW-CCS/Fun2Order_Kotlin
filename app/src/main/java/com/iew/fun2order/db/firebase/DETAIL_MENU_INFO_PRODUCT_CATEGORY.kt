package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DETAIL_MENU_INFO_PRODUCT_CATEGORY(
    @SerializedName("categoryName")
    var categoryName:   String = "",
    @SerializedName("priceTemplate")
    var priceTemplate:   DETAIL_MENU_INFO_PRICE_TEMPLATE,
    @SerializedName("productItems")
    var productItems:  List<DETAIL_MENU_INFO_PRODUCT_ITEM>? = listOf<DETAIL_MENU_INFO_PRODUCT_ITEM>()
) : Parcelable
