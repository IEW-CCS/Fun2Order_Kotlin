package com.iew.fun2order.db.dao
import androidx.lifecycle.LiveData
import com.iew.fun2order.db.entity.entityFriend
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iew.fun2order.db.entity.entityGroup_detail

@Dao
interface group_detailDAO {

    @Insert
    fun insertRow(group_detail: entityGroup_detail)

    @Insert
    fun inserAll(groupdetailList: ArrayList<entityGroup_detail>)

    @Query("Select * FROM entityGroup_detail")
    fun getAllGroup(): LiveData<List<entityGroup_detail>>

    @Delete
    fun delete(group_detail: entityGroup_detail)

    @Query("delete FROM entityGroup_detail WHERE `groupid` = :groupid AND `friend` = :friend")
    fun deleteGruopMember(groupid:String, friend: String)

    @Query("delete FROM entityGroup_detail WHERE `groupid` = :groupid")
    fun deleteGruop(groupid:String)

    @Query("delete FROM entityGroup_detail WHERE `friend` = :friend")
    fun deleteFriend(friend:String)

    @Update
    fun updateTodo(vararg groups: entityGroup_detail)

    @Query("Select friend FROM entityGroup_detail WHERE `groupid` = :groupid")
    fun getMemberByGroupID(groupid:String):List<String>


}