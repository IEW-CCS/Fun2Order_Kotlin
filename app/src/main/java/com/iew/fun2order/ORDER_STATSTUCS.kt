package com.iew.fun2order


import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class ORDER_STATSTUCS (
    var itemOwner: String? = "",
    var itemComments: String? = "",
    var itemName: String? = "",
    var itemPrice: Int? = 0,
    var itemQuantity:  Int? = 0,
    var itemRecipe : String? = "",
    var uniquetKey : String? = "",
    var sequenceNumber: Int? = 0
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "itemOwner" to itemOwner,
            "itemComments" to itemComments,
            "itemName" to itemName,
            "itemPrice" to itemPrice,
            "itemQuantity" to itemQuantity,
            "itemRecipe" to itemRecipe,
            "uniquetKey" to uniquetKey,
            "sequenceNumber" to sequenceNumber
        )
    }
}