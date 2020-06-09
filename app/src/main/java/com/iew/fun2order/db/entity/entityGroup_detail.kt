package com.iew.fun2order.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = arrayOf(Index(value = ["groupid","friend"], unique = true)))
data class entityGroup_detail (
    @PrimaryKey(autoGenerate = true)
    val id:Long?= null,
    @ColumnInfo(name = "groupid")
    val groupid:String,
    @ColumnInfo(name = "friend")
    val friend:String
)