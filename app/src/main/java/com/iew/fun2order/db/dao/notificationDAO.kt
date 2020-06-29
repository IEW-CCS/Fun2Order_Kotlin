package com.iew.fun2order.db.dao
import androidx.lifecycle.LiveData
import com.iew.fun2order.db.entity.entityFriend
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.iew.fun2order.db.entity.entityNotification

@Dao
interface notificationDAO {

    @Insert
    fun insertRow(notification: entityNotification)

    @Insert
    fun inserAll(notificationList: ArrayList<entityNotification>)

    @Query("Select * FROM entityNotification")
    fun getAllNotify(): LiveData<List<entityNotification>>

    @Query("Select * FROM entityNotification where notificationType = 'JOIN'")
    fun getAllInvite(): LiveData<List<entityNotification>>

    @Query("Select * FROM entityNotification where notificationType = 'SHIPPING'")
    fun getAllShipping(): LiveData<List<entityNotification>>

    @Query("Select * FROM entityNotification where notificationType <> 'JOIN' and notificationType <> 'SHIPPING'")
    fun getAllOthers(): LiveData<List<entityNotification>>

    @Query("Select * FROM entityNotification where isRead = 'N'")
    fun getUnreadNotify(): List<entityNotification>

    @Query("delete FROM entityNotification WHERE `messageID` = :msgid")
    fun deleteNotify(msgid:String)

    @Query("select * FROM entityNotification WHERE `messageID` = :msgid")
    fun getNotifybyMsgID(msgid:String): entityNotification

    @Query("select * FROM entityNotification WHERE `orderNumber` = :orderNo")
    fun getNotifybyOrderNo(orderNo:String): List<entityNotification>

    @Delete
    fun delete(notification: entityNotification)

    @Update
    fun update(vararg notification: entityNotification)

}