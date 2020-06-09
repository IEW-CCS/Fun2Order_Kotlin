package com.iew.fun2order.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = arrayOf(Index(value = ["groupid"], unique = true)))
data class entityGroup (
    @PrimaryKey(autoGenerate = true)
    val id:Long?= null,
    @ColumnInfo(name = "groupid")
    val groupid:String,
    @ColumnInfo(name = "name")
    var name:String,
    @ColumnInfo(name = "desc")
    var desc:String,
    @ColumnInfo(name = "image")
    var image:ByteArray

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as entityGroup

        if (id != other.id) return false
        if (groupid != other.groupid) return false
        if (!image.contentEquals(other.image)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + groupid.hashCode()
        result = 31 * result + image.contentHashCode()
        return result
    }
}