package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
class MENU_RECIPE_TEMPLATE (
    var templateName: String? = "",
    var menuRecipes: MutableList<RECIPE>? = mutableListOf(),
    var sequenceNumber: Int? = 0
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "templateName" to templateName,
            "menuRecipes" to menuRecipes,
            "sequenceNumber" to sequenceNumber
        )
    }
}