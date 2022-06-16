package com.dreamreco.joodiary.room.dao

import androidx.room.*
import com.dreamreco.joodiary.room.entity.CalendarDate
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.room.entity.LoadImageSignal
import com.dreamreco.joodiary.room.entity.MyDate
import kotlinx.coroutines.flow.Flow

@Dao
interface LoadImageSignalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loadImageSignal: LoadImageSignal)

    @Delete
    suspend fun delete(loadImageSignal: LoadImageSignal)

    @Update
    suspend fun update(loadImageSignal: LoadImageSignal)

    @Query("DELETE FROM load_image_signal")
    suspend fun clear()


    // 소환 방식을 가져오는 함수
    @Query("SELECT loadType FROM load_image_signal ORDER BY id ASC LIMIT 1")
    fun getSignalFlow(): Flow<Int>

}