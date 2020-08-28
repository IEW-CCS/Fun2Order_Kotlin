package com.iew.fun2order.brand.product

import android.graphics.Bitmap
import com.iew.fun2order.db.firebase.DETAIL_MENU_INFO_PRODUCT_PRICE

data class ItemsLV_Products_withExtra(var Name: String, var ItemPrices:List<DETAIL_MENU_INFO_PRODUCT_PRICE>, var StandAlone: Boolean, var ItemDesc: String, var ItemDescURL:String?)

