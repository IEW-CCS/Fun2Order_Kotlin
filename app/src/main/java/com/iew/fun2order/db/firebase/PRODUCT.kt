package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
class PRODUCT (
    var itemName: String? = "",
    var itemPrice: Int? = 0,
    var sequenceNumber: Int? = 0
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "itemName" to itemName,
            "itemPrice" to itemPrice,
            "sequenceNumber" to sequenceNumber
        )
    }
}