package com.iew.fun2order.ui.home.data

class ProductItemListData {
    private var item_product_name: String? = null
    private var item_product_price: String? = null

    constructor(item_product_name: String?, item_product_price: String?) {
        this.item_product_name = item_product_name
        this.item_product_price = item_product_price
    }

    fun getItemProductName(): String? {
        return item_product_name
    }

    fun setItemProductName(item_product_name: String?) {
        this.item_product_name = item_product_name
    }

    fun getItemProductPrice(): String? {
        return item_product_price
    }

    fun setItemProductPrice(item_product_price: String?) {
        this.item_product_price = item_product_price
    }


}