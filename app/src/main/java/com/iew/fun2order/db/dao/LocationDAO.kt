package com.iew.fun2order.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.iew.fun2order.db.entity.Location

@Dao
interface LocationDAO {
    @Insert
    fun insertRow(location: Location)

    @Insert
    fun inserAll(locationList: ArrayList<Location>)

    @Query("Select * FROM Location")
    fun getAllLocation(): LiveData<List<Location>>

    @Delete
    fun delete(location: Location)

    @Update
    fun updateTodo(vararg locations: Location)

    @Query("Select * FROM Location WHERE menu_id = :menu_id")
    fun getLocationByMenuID(menu_id:String):List<Location>

    @Query("Select location FROM Location  WHERE menu_id = :menu_id")
    fun getLocationIDByMenuID(menu_id:String):List<String>
}