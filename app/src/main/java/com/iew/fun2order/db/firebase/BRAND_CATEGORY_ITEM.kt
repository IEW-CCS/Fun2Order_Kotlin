package com.iew.fun2order.db.firebase

import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class BRAND_CATEGORY_ITEM(
    var brandCategory   : String = "",
    var brandIconImage  : String = "",
    var brandName       : String = "",
    var brandSubCategory: String = "",
    var updateDateTime  : String = ""

){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "brandCategory" to brandCategory,
            "brandIconImage" to brandIconImage,
            "brandName" to brandName,
            "brandSubCategory" to brandSubCategory,
            "updateDateTime" to updateDateTime
        )
    }
}
