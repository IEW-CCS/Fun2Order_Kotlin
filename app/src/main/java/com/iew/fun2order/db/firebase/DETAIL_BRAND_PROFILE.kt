package com.iew.fun2order.db.firebase

import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName

data class DETAIL_BRAND_PROFILE(
    @SerializedName("brandCategory")
    var brandCategory   : String = "",
    @SerializedName("brandIconImage")
    var brandIconImage  : String = "",
    @SerializedName("brandName")
    var brandName       : String = "",
    @SerializedName("brandSubCategory")
    var brandSubCategory: String = "",
    @SerializedName("menuNumber")
    var menuNumber: String = "",
    @SerializedName("updateDateTime")
    var updateDateTime  : String = ""

)
