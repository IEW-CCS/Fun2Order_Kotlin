package com.iew.fun2order.db.dao
import androidx.lifecycle.LiveData
import com.iew.fun2order.db.entity.entityFriend
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface friendDAO {

    @Insert
    fun insertRow(friend: entityFriend)

    @Insert
    fun inserAll(friendList: ArrayList<entityFriend>)

    @Query("Select * FROM entityFriend")
    fun getAllFriend(): LiveData<List<entityFriend>>

    @Query("Select friend FROM entityFriend")
    fun getFriendslist(): List<String>

    @Query("Delete  FROM entityFriend")
    fun deleteall()

    @Delete
    fun delete(friend: entityFriend)

    @Update
    fun updateTodo(vararg friends: entityFriend)

    @Query("Select * FROM entityFriend WHERE friend = :friend")
    fun getFriendByName(friend:String):List<entityFriend>

}