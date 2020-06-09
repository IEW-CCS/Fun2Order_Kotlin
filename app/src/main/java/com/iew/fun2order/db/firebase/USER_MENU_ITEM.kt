package com.iew.fun2order.db.firebase

import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class USER_MENU_ITEM(
    var itemName: String = "",
    var itemPrice: Int = 0,
    var sequenceNumber: Int =0

){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "itemName" to itemName,
            "itemPrice" to itemPrice,
            "sequenceNumber" to sequenceNumber

        )
    }
}
