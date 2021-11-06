package com.akeshishi.tracker.base.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Action::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class ActionDatabase: RoomDatabase() {

    abstract fun getActionDao() : ActionDao
}