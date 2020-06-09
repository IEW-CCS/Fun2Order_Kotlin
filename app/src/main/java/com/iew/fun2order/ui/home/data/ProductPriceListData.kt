package com.iew.fun2order.ui.home.data

class ProductPriceListData {
    private var item_name: String? = null
    private var item_value: String? = null


    constructor(item_name: String?, item_value: String?) {
        this.item_name = item_name
        this.item_value = item_value
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

}