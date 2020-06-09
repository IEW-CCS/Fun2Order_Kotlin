package com.iew.fun2order.db.dao
import androidx.lifecycle.LiveData
import com.iew.fun2order.db.entity.entityFriend
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iew.fun2order.db.entity.entityFriendImage
import com.iew.fun2order.db.entity.entityGroup
import com.iew.fun2order.db.entity.entityMeunImage

@Dao
interface friendImageDAO {

    @Insert
    fun insertRow(image: entityFriendImage)

    @Delete
    fun delete(image: entityFriendImage)

    @Update
    fun updateTodo(vararg image: entityFriendImage)

    @Query("Select * FROM entityFriendImage WHERE name = :imageName")
    fun getFriendImageByName(imageName:String) : entityFriendImage

    @Query("Delete  FROM entityFriendImage")
    fun deleteall()

}