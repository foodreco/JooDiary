package com.dreamreco.joodiary.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dreamreco.joodiary.room.dao.CalendarDateDao
import com.dreamreco.joodiary.room.dao.DiaryBaseDao
import com.dreamreco.joodiary.room.dao.LoadImageSignalDao
import com.dreamreco.joodiary.room.entity.CalendarDate
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.room.entity.LoadImageSignal


@Database(
    entities = [DiaryBase::class, CalendarDate::class, LoadImageSignal::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(value = [Converters::class, MyDateTypeConverter::class, MyDrinkTypeConverter::class, CalendarDayTypeConverter::class])
abstract class Database : RoomDatabase() {

    abstract val diaryDao: DiaryBaseDao
    abstract val calendarDateDao: CalendarDateDao
    abstract val loadImageSignalDao: LoadImageSignalDao

}