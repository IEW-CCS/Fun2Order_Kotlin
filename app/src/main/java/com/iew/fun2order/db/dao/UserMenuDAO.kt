package com.iew.fun2order.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.iew.fun2order.db.entity.UserMenu

@Dao
interface UserMenuDAO {
    @Insert
    fun insertRow(usermenu: UserMenu)

    @Insert
    fun inserAll(usermenuList: ArrayList<UserMenu>)

    @Query("Select * FROM UserMenu")
    fun getAllMenu(): LiveData<List<UserMenu>>

    @Query("Select menu_id FROM UserMenu")
    fun getMenuslist(): List<String>

    @Delete
    fun delete(usermenu: UserMenu)

    @Update
    fun updateTodo(vararg friends: UserMenu)

    @Query("Select * FROM UserMenu WHERE menu_id = :menu_id")
    fun getMenuByID(menu_id:String):UserMenu

    @Query("Select * FROM UserMenu  WHERE menu_type = :menu_type")
    fun getMenusByType(menu_type:String): LiveData<List<UserMenu>>
}