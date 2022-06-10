package com.dreamreco.joodiary.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dreamreco.joodiary.room.dao.DiaryBaseDao
import com.dreamreco.joodiary.room.entity.DiaryBase


@Database(entities = [DiaryBase::class], version = 1, exportSchema = false )
abstract class Database : RoomDatabase() {

    abstract val diaryDao : DiaryBaseDao

}