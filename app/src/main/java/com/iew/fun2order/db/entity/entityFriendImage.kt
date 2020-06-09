package com.iew.fun2order.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = arrayOf(Index(value = ["name"], unique = true)))
data class entityFriendImage (
    @PrimaryKey(autoGenerate = true)
    val id:Long?= null,
    @ColumnInfo(name = "name")
    var name:String,
    @ColumnInfo(name = "displayname")
    var displayname:String,
    @ColumnInfo(name = "image")
    var image:ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as entityFriendImage

        if (id != other.id) return false
        if (name != other.name) return false
        if (displayname != other.displayname) return false
        if (!image.contentEquals(other.image)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + displayname.hashCode()
        result = 31 * result + image.contentHashCode()
        return result
    }
}
