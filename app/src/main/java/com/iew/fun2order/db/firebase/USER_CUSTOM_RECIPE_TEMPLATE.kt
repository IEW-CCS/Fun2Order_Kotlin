package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
class USER_CUSTOM_RECIPE_TEMPLATE(
    var menuRecipes: MutableList<RECIPE>? = mutableListOf(),
    var sequenceNumber: Int? = 0,
    var templateName: String? = ""
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "menuRecipes" to menuRecipes,
            "sequenceNumber" to sequenceNumber,
            "templateName" to templateName
        )
    }
}
