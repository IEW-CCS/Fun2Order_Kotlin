package com.iew.fun2order.order
import com.iew.fun2order.db.firebase.MENU_PRODUCT

data class ItemsLV_ReferenceOrder (var referenceOwner: String, var referenceProduct:MutableList<MENU_PRODUCT>, var followone: Boolean)
