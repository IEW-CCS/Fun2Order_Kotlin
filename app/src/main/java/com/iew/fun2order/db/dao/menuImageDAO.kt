package com.iew.fun2order.db.dao
import androidx.lifecycle.LiveData
import com.iew.fun2order.db.entity.entityFriend
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iew.fun2order.db.entity.entityGroup
import com.iew.fun2order.db.entity.entityMeunImage

@Dao
interface menuImageDAO {

    @Insert
    fun insertRow(image: entityMeunImage)

    @Delete
    fun delete(image: entityMeunImage)

    @Update
    fun updateTodo(vararg image: entityMeunImage)

    @Query("Select * FROM entityMeunImage WHERE name = :imageName")
    fun getMenuImageByName(imageName:String) : entityMeunImage

}