package com.iew.fun2order.db.dao
import androidx.lifecycle.LiveData
import com.iew.fun2order.db.entity.entityFriend
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iew.fun2order.db.entity.entityGroup

@Dao
interface groupDAO {

    @Insert
    fun insertRow(group: entityGroup)

    @Insert
    fun inserAll(groupList: ArrayList<entityGroup>)

    @Query("Select * FROM entityGroup")
    fun getAllGroup(): LiveData<List<entityGroup>>

    @Delete
    fun delete(group: entityGroup)

    @Update
    fun updateTodo(vararg groups: entityGroup)

    @Query("Select * FROM entityGroup WHERE groupid = :groupid")
    fun getGroupByID(groupid:String): entityGroup

    @Query("Select * FROM entityGroup WHERE name = :groupname")
    fun getGroupByName(groupname:String): entityGroup

}