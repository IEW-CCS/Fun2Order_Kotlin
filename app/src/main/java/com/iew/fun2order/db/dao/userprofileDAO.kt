package com.iew.fun2order.db.dao
import androidx.lifecycle.LiveData
import com.iew.fun2order.db.entity.entityFriend
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iew.fun2order.db.entity.entityGroup
import com.iew.fun2order.db.entity.entityUserProfile

@Dao
interface userprofileDAO {

    @Insert
    fun insertRow(profile: entityUserProfile)

    @Delete
    fun delete(profile: entityUserProfile)

    @Update
    fun updateTodo(vararg profile: entityUserProfile)

    @Query("Select * FROM entityUserProfile WHERE uID = :uid")
    fun getProfileByID(uid:String): entityUserProfile


}