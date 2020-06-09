package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
class ORDER_MEMBER (
    var memberID: String? = "",
    var memberTokenID: String? = "",
    var orderContent: ORDER_CONTENT = ORDER_CONTENT(),
    var orderOwnerID: String? = ""
): Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "memberID" to memberID,
            "memberTokenID" to memberTokenID,
            "orderContent" to orderContent,
            "orderOwnerID" to orderOwnerID
        )
    }
}