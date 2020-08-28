package com.iew.fun2order.db.firebase

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
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
    var updateDateTime  : String = "",
    @SerializedName("imageDownloadUrl")
    var imageDownloadUrl  : String? = null,
    @SerializedName("brandEventBannerURL")
    var brandEventBannerURL  : String? = null,
    @SerializedName("brandMenuBannerURL")
    var brandMenuBannerURL  : String? = null,
    @SerializedName("brandStoryURL")
    var brandStoryURL  : String? = null,
    @SerializedName("coworkBrandFlag")
    var coworkBrandFlag  : Boolean? = null,
    @SerializedName("brandStyle")
    var brandStyle  : DETAIL_BRAND_STYLE? = null

) : Parcelable
