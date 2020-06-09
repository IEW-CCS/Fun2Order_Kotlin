package com.iew.fun2order.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.iew.fun2order.db.entity.MenuType

@Dao
interface MenuTypeDAO {
    @Insert
    fun insertRow(menutype: MenuType)

    @Insert
    fun inserAll(menutypeList: ArrayList<MenuType>)

    @Query("Select * FROM MenuType")
    fun getAllMenuType(): LiveData<List<MenuType>>

    @Query("Select menu_type FROM MenuType")
    fun getMenuTypeslist(): List<String>

    @Delete
    fun delete(menutype: MenuType)

    @Update
    fun updateTodo(vararg friends: MenuType)

    @Query("Select * FROM MenuType WHERE menu_type = :menutype")
    fun getMenuTypeByName(menutype:String):List<MenuType>
}