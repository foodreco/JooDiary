package com.dreamreco.joodiary.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.room.entity.MyDate
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

    @Query("DELETE FROM diary_base WHERE date = :date AND title =:title")
    suspend fun deleteByTitleAndDate(date: MyDate, title: String)

    // 주종을 모두 리스트 형태로 모두 불러오는 코드
    @Query("SELECT drinkType FROM diary_base WHERE drinkType is not null ORDER BY drinkType ASC")
    suspend fun getDrinkType(): List<String>

    // 주종을 모두 리스트 형태로 모두 불러오는 코드
    @Query("SELECT POA FROM diary_base WHERE POA is not null ORDER BY POA ASC")
    suspend fun getPOA(): List<String>

    // 주종을 모두 리스트 형태로 모두 불러오는 코드
    @Query("SELECT VOD FROM diary_base WHERE VOD is not null ORDER BY VOD ASC")
    suspend fun getVOD(): List<String>

    // 제목을 받아, 정보를 반환하는 함수
    @Query("SELECT title FROM diary_base WHERE title = :title ORDER BY title ASC LIMIT 1")
    suspend fun checkTitle(title: String): String?

    // 제목을 받아, 정보를 반환하는 함수
    @Query("SELECT title FROM diary_base WHERE date = :date AND title =:title ORDER BY date ASC LIMIT 1")
    suspend fun checkTitleAndDate(date: MyDate, title: String): String?

    // 특정 DiaryBase 를 가져오는 함수
    @Query("SELECT importance FROM diary_base WHERE date = :date AND title =:title LIMIT 1")
    suspend fun checkDiaryBaseImportanceByDateAndTitle(date: MyDate, title: String) : Boolean



    // 특정 DiaryBase 를 가져오는 함수
    @Query("SELECT * FROM diary_base WHERE date = :date AND title =:title LIMIT 1")
    fun getDiaryBaseByDateAndTitleFlow(date: MyDate, title: String): Flow<DiaryBase>

    // 모든 DiaryBase 를 날짜 오름차순으로 가져오는 함수
    @Query("SELECT * FROM diary_base ORDER BY date ASC")
    fun getAllDiaryBaseByDateASC(): Flow<List<DiaryBase>>

    // 모든 DiaryBase 를 날짜 내람차순으로 가져오는 함수
    @Query("SELECT * FROM diary_base ORDER BY date DESC")
    fun getAllDiaryBaseByDateDESC(): Flow<List<DiaryBase>>

    // 특정 날짜의 DiaryBase 를 가져오는 함수
    @Query("SELECT * FROM diary_base WHERE date = :date ORDER BY title ASC")
    fun getDiaryBaseFlowInDate(date: MyDate): Flow<List<DiaryBase>>

}