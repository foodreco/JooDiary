package com.dreamreco.joodiary.room.dao

import androidx.room.*
import com.dreamreco.joodiary.room.entity.CalendarDate
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.room.entity.MyDate
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(calendarDate: CalendarDate)

    @Delete
    suspend fun delete(calendarDate: CalendarDate)

    @Update
    suspend fun update(calendarDate: CalendarDate)

    @Query("DELETE FROM calendar_date")
    suspend fun clear()


    // 모든 DiaryBase 를 날짜 오름차순으로 가져오는 함수
    @Query("SELECT * FROM calendar_date ORDER BY id ASC LIMIT 1")
    fun getRecentDate(): Flow<CalendarDate>

}