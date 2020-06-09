package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class ORDER_CONTENT(
    var createTime: String? = "",
    var payCheckedFlag: Boolean? = false,
    var itemFinalPrice: Int? = 0,
    var itemOwnerID: String? = "",
    var itemOwnerName: String? = "",
    var itemQuantity: Int? = 0,
    var itemSinglePrice: Int? = 0,
    var location: String? = "",
    var menuProductItems: MutableList<MENU_PRODUCT>? = mutableListOf(),
    var orderNumber: String? = "",
    var payNumber: Int? = 0,
    var payTime: String? = "",
    var replyStatus: String? = ""
): Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "createTime" to createTime,
            "payCheckedFlag" to payCheckedFlag,
            "itemFinalPrice" to itemFinalPrice,
            "itemOwnerID" to itemOwnerID,
            "itemOwnerName" to itemOwnerName,
            "itemQuantity" to itemQuantity,
            "itemSinglePrice" to itemSinglePrice,
            "location" to location,
            "orderNumber" to orderNumber,
            "payNumber" to payNumber,
            "payTime" to payTime,
            "replyStatus" to replyStatus
        )
    }
}