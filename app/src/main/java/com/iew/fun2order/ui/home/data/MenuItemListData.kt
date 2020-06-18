package com.iew.fun2order.ui.home.data

import android.graphics.Bitmap
import com.iew.fun2order.db.firebase.USER_MENU
import com.iew.fun2order.db.firebase.USER_PROFILE
import java.io.File

class MenuItemListData {
    private var item_name: String? = null
    private var item_desc: String? = null
    private var item_image: Bitmap? = null
    private var item_image_path: String? = ""
    private var user_menu: USER_MENU? = null
    private var user_profile: USER_PROFILE? = null


    constructor(item_name: String?, item_desc: String?, item_image: Bitmap?, item_image_path: String?, user_menu: USER_MENU?, user_profile: USER_PROFILE?) {
        this.item_name = item_name
        this.item_desc = item_desc
        this.item_image = item_image
        this.item_image_path= item_image_path
        this.user_menu = user_menu
        this.user_profile = user_profile

    }

    fun getItemName(): String? {
        return item_name
    }

    fun setItemName(item_name: String?) {
        this.item_name = item_name
    }

    fun getItemValue(): String? {
        return item_desc
    }

    fun setItemValue(item_desc: String?) {
        this.item_desc = item_desc
    }

    fun getItemImage(): Bitmap? {
        return item_image
    }

    fun setItemImage(item_image: Bitmap?) {
        this.item_image = item_image
    }

    fun getItemImagePath(): String? {
        return item_image_path
    }

    fun setItemImagePath(item_image_path: String?) {
        this.item_image_path = item_image_path
    }

    fun getUserMenu(): USER_MENU? {
        return user_menu
    }

    fun setUserMenu(user_menu: USER_MENU?) {
        this.user_menu = user_menu
    }

    fun getUserProfile(): USER_PROFILE? {
        return user_profile
    }

    fun setUserProfile(user_profile: USER_PROFILE?) {
        this.user_profile = user_profile
    }

}