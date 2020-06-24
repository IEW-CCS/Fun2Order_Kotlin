package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class CONTENT_CONTACTINFO(
    var userAddress: String? = "",
    var userName:  String? = "",
    var userPhoneNumber: String? = ""
): Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userAddress" to userAddress,
            "userName" to userName,
            "userPhoneNumber" to userPhoneNumber
        )
    }
}