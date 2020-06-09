package com.iew.fun2order.ui.home.data

import android.graphics.Bitmap
import android.widget.Button

class RecipeItemListData {
    private var item_name: String? = null
    private var item_data_list :MutableList<String> = mutableListOf()
    private var item_data_select_list :MutableList<Boolean> = mutableListOf()
    private var allow_multi: Boolean? = false

    constructor(item_name: String?, item_data_list: MutableList<String>, item_data_select_list: MutableList<Boolean>, allow_multi: Boolean?) {
        this.item_name = item_name
        this.item_data_list=item_data_list
        this.item_data_select_list = item_data_select_list
        this.allow_multi =allow_multi
    }

    fun getItemName(): String? {
        return item_name
    }

    fun setItemName(item_name: String?) {
        this.item_name = item_name
    }

    fun getItemDataList(): MutableList<String> {
        return item_data_list
    }

    fun setItemDataList(item_data_list: MutableList<String>) {
        this.item_data_list = item_data_list
    }

    fun getItemDataSelectList(): MutableList<Boolean> {
        return item_data_select_list
    }

    fun setItemDataSelectList(item_data_select_list: MutableList<Boolean>) {
        this.item_data_select_list = item_data_select_list
    }

    fun getAllowMulti(): Boolean? {
        return allow_multi
    }

    fun setAllowMulti(allow_multi: Boolean?) {
        this.allow_multi = allow_multi
    }
}