package com.iew.fun2order.db.dao
import androidx.lifecycle.LiveData
import com.iew.fun2order.db.entity.entityFriend
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iew.fun2order.db.entity.entityGroup
import com.iew.fun2order.db.entity.entityLocalmage
import com.iew.fun2order.db.entity.entityMeunImage

@Dao
interface localImageDAO {

    @Insert
    fun insertRow(image: entityLocalmage)

    @Delete
    fun delete(image: entityLocalmage)

    @Update
    fun updateTodo(vararg image: entityLocalmage)

    @Query("Select * FROM entityLocalmage WHERE name = :imageName")
    fun getMenuImageByName(imageName:String) : entityLocalmage

    @Query("Select * FROM entityLocalmage")
    fun getall() : List<entityLocalmage>

    @Query("Delete  FROM entityLocalmage")
    fun deleteall()

    @Query("Delete  FROM entityLocalmage WHERE name like :imageName||'%'")
    fun deleteICONImage(imageName:String)

}