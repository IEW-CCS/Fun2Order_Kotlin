package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DETAIL_MENU_INFORMATION(
    @SerializedName("brandName")
    var brandName:   String = "",
    @SerializedName("createTime")
    var createTime  : String = "",
    @SerializedName("menuNumber")
    var menuNumber  : String = "",
    @SerializedName("productCategory")
    var productCategory  : List<DETAIL_MENU_INFO_PRODUCT_CATEGORY>? =  listOf<DETAIL_MENU_INFO_PRODUCT_CATEGORY>(),
    @SerializedName("recipeTemplates")
    var recipeTemplates  : List<DETAIL_MENU_INFO_PRICE_TEMPLATE>? = listOf<DETAIL_MENU_INFO_PRICE_TEMPLATE>()

) : Parcelable