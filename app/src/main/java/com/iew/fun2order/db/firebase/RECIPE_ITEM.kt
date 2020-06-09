package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
class RECIPE_ITEM (
    var checkedFlag: Boolean? = true,
    var recipeName: String? = "",
    var sequenceNumber: Int? = 0
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "checkedFlag" to checkedFlag,
            "recipeName" to recipeName,
            "sequenceNumber" to sequenceNumber
        )
    }
}