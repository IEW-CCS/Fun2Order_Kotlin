package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
class STORE_INFO (
    var storeAddress: String? = "",
    var storeName: String? = "",
    var storePhoneNumber: String? = ""
): Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "storeAddress" to storeAddress,
            "storeName" to storeName,
            "storePhoneNumber" to storePhoneNumber
        )
    }
}