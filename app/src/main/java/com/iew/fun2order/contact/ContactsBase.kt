package com.iew.fun2order.contact

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.iew.fun2order.db.firebase.DETAIL_MENU_INFO_PRICE_TEMPLATE
import com.iew.fun2order.db.firebase.DETAIL_MENU_INFO_PRODUCT_CATEGORY
import kotlinx.android.parcel.Parcelize

/**
 * Created by Administrator on 2019/6/21.
 */
class ContactsBase {
    var name: String? = null
    var phone: MutableList<String> = mutableListOf()
    var note: String? = null
}

@Parcelize
data class PhoneBase(
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("phone")
    var phone: String? = null,
    @SerializedName("firebaseUUID")
    var firebaseUUID: String? = null,
    @SerializedName("firebaseTokenID")
    var firebaseTokenID: String? = null,
    @SerializedName("firebaseDisplayName")
    var firebaseDisplayName: String? = null,
    @SerializedName("firebaseImagePath")
    var firebaseImagePath: String? = null

) : Parcelable