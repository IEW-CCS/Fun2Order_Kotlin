package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class USER_PROFILE (
    var phoneNumber: String = "",
    var photoURL: String = "",
    var tokenID: String = "",
    var userID:  String = "",
    var userName:  String = "" ,
    var gender:  String?= null ,
    var address:  String? = null,
    var birthday:  String? = null,
    var brandCategoryList: MutableList<String>? = mutableListOf(),
    var friendList: MutableList<String>? = mutableListOf()

) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "phoneNumber" to phoneNumber,
            "photoURL" to photoURL,
            "tokenID" to tokenID,
            "userID" to userID,
            "userName" to userName,
            "gender" to gender,
            "address" to address,
            "birthday" to birthday,
            "brandCategoryList" to brandCategoryList,
            "friendList" to friendList
        )
    }
}