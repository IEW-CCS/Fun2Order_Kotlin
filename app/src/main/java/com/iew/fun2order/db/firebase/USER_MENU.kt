package com.iew.fun2order.db.firebase

import android.graphics.Bitmap
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class USER_MENU (
    var brandCategory: String? = "",
    var brandName: String? = "",
    var createTime: String? = "",
    var locations: MutableList<String>? = mutableListOf(),
    var menuDescription: String? = "",
    var menuImageURL: String? = "",
    var menuItems: MutableList<PRODUCT>? = mutableListOf(),
    var menuNumber: String? = "",
    var menuRecipes: MutableList<RECIPE>? = mutableListOf(),
    var multiMenuImageURL: MutableList<String>? = mutableListOf(),
    var needContactInfoFlag: Boolean? = null,
    var storeInfo: STORE_INFO? = null,
    var userID: String? = "",
    var userName: String? = ""
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "brandCategory" to brandCategory,
            "brandName" to brandName,
            "createTime" to createTime,
            "locations" to locations,
            "menuDescription" to menuDescription,
            "menuImageURL" to menuImageURL,
            "menuItems" to menuItems,
            "menuNumber" to menuNumber,
            "menuRecipes" to menuRecipes,
            "multiMenuImageURL" to multiMenuImageURL,
            "needContactInfoFlag" to needContactInfoFlag,
            "storeInfo" to storeInfo,
            "userID" to userID,
            "userName" to userName
        )
    }
}



@IgnoreExtraProperties
class USER_MENU2 (
    var brandCategory: String? = "",
    var brandName: String? = "",
    var createTime: String? = "",
    var locations: MutableList<LOCATION>? = mutableListOf(),
    var menuDescription: String? = "",
    var menuImageURL: String? = "",
    var menuItems: MutableList<PRODUCT>? = mutableListOf(),
    var menuNumber: String? = "",
    var menuRecipes: MutableList<RECIPE>? = mutableListOf(),
    var userID: String? = "",
    var userName: String? = ""
)  {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "brandCategory" to brandCategory,
            "brandName" to brandName,
            "createTime" to createTime,
            "locations" to locations,
            "menuDescription" to menuDescription,
            "menuImageURL" to menuImageURL,
            "menuItems" to menuItems,
            "menuNumber" to menuNumber,
            "menuRecipes" to menuRecipes,
            "userID" to userID,
            "userName" to userName
        )
    }
}

