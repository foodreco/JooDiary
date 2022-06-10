package com.dreamreco.joodiary.room.dao

import androidx.room.*
import com.dreamreco.joodiary.room.entity.DiaryBase
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryBaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diaryBase: DiaryBase)

    @Delete
    suspend fun delete(diaryBase: DiaryBase)

    @Update
    suspend fun update(diaryBase: DiaryBase)

    @Query("DELETE FROM diary_base")
    suspend fun clear()

}