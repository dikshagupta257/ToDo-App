package com.codingblocksmodules.todoapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

const val DB_NAME = "Todo.db"
@Database(entities = [TodoModel::class] , version = 1)
abstract class AppDatabase:RoomDatabase(){
    abstract fun todoDao():TodoDao
    companion object{
        @Volatile
        private var INSTANCE:AppDatabase?=null
        fun getDatabase(context: Context):AppDatabase{
            val tempInstance = INSTANCE
            if(tempInstance!=null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext , AppDatabase::class.java , DB_NAME).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}