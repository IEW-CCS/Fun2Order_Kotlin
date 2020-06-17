package com.iew.fun2order.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.iew.fun2order.db.dao.friendDAO
import com.iew.fun2order.db.dao.friendImageDAO
import com.iew.fun2order.db.dao.menuImageDAO
import com.iew.fun2order.db.entity.entityFriend
import com.iew.fun2order.db.entity.entityFriendImage
import com.iew.fun2order.db.entity.entityMeunImage

/*
@Database(
    entities = [TodoEntity::class, TaskEntry::class],
    version = 1
)*/


@Database(
    entities = [entityFriend::class, entityMeunImage::class, entityFriendImage::class], version = 4
)
abstract class MemoryDatabase : RoomDatabase(){
    abstract fun frienddao(): friendDAO
    abstract fun menuImagedao(): menuImageDAO
    abstract fun friendImagedao(): friendImageDAO

    companion object {
        @Volatile private var instance: MemoryDatabase? = null
        private val LOCK = Any()
        operator fun invoke(context: Context)= instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.inMemoryDatabaseBuilder(context,
            MemoryDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
}