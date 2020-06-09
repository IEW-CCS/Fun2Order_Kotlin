package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
class RECIPE (
    var allowedMultiFlag: Boolean? = true,
    var recipeCategory: String? = "",
    var recipeItems: MutableList<RECIPE_ITEM>? = mutableListOf(),
    var sequenceNumber: Int? = 0
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "allowedMultiFlag" to allowedMultiFlag,
            "recipeCategory" to recipeCategory,
            "recipeItems" to recipeItems,
            "sequenceNumber" to sequenceNumber
        )
    }
}