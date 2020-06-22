package com.iew.fun2order.ui.home.data

class ProductPriceListData {
    private var item_name: String? = null
    private var item_value: String? = null
    private var item_limit: String? = null


    constructor(item_name: String?, item_value: String?, item_limit: String?) {
        this.item_name = item_name
        this.item_value = item_value
        this.item_limit = item_limit
    }

    fun getItemName(): String? {
        return item_name
    }

    fun setItemName(item_name: String?) {
        this.item_name = item_name
    }

    fun getItemValue(): String? {
        return item_value
    }

    fun setItemValue(item_value: String?) {
        this.item_value = item_value
    }

    fun getItemLimit(): String? {
        return item_limit?: ""
    }

    fun setItemLimit(item_limit: String?) {
        this.item_limit = item_limit
    }

}