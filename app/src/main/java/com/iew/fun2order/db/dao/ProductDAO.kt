package com.iew.fun2order.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.iew.fun2order.db.entity.Product

@Dao
interface ProductDAO {
    @Insert
    fun insertRow(product: Product)

    @Insert
    fun inserAll(productList: ArrayList<Product>)

    @Query("Select * FROM Product")
    fun getAllProduct(): LiveData<List<Product>>

    @Delete
    fun delete(product: Product)

    @Update
    fun updateTodo(vararg products: Product)

    @Query("Select * FROM Product WHERE menu_id = :menu_id")
    fun getProductByMenuID(menu_id:String):List<Product>

}