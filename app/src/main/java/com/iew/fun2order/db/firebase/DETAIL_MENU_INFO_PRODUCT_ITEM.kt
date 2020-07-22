package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DETAIL_MENU_INFO_PRODUCT_ITEM(
    @SerializedName("priceList")
    var priceList: List<DETAIL_MENU_INFO_PRODUCT_PRICE>,
    @SerializedName("productBasicPrice")
    var productBasicPrice:   Int = 0,
    @SerializedName("productCategory")
    var productCategory:   String = "",
    @SerializedName("productName")
    var productName:   String = "",
    @SerializedName("recipeRelation")
    var recipeRelation:List<DETAIL_MENU_INFO_RECIPE_RELATION>? = listOf<DETAIL_MENU_INFO_RECIPE_RELATION>()
) : Parcelable