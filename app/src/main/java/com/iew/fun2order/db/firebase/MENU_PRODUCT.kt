package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class MENU_PRODUCT (
    var itemComments: String? = "",
    var itemName: String? = "",
    var itemPrice: Int? = 0,
    var itemQuantity:  Int? = 0,
    var menuRecipes: MutableList<RECIPE>? = mutableListOf(),
    var sequenceNumber: Int? = 0
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "itemComments" to itemComments,
            "itemName" to itemName,
            "itemPrice" to itemPrice,
            "itemQuantity" to itemQuantity,
            "menuRecipes" to menuRecipes,
            "sequenceNumber" to sequenceNumber
        )
    }
}