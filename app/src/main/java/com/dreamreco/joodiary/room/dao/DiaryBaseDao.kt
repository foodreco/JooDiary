package com.dreamreco.joodiary.room.dao

import androidx.room.*
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.room.entity.MyDate
import com.dreamreco.joodiary.room.entity.MyDrink
import com.dreamreco.joodiary.ui.statistics.AllVODListBase
import com.prolificinteractive.materialcalendarview.CalendarDay
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

    // 모든 DiaryBase 를 날짜 내람차순으로 가져오는 함수
    @Query("SELECT * FROM diary_base ORDER BY dateForSort DESC")
    suspend fun getAllDiaryBaseByDateDESCForBackUp(): List<DiaryBase>

    // 주종을 모두 리스트 형태로 모두 불러오는 코드
    @Query("SELECT myDrink FROM diary_base WHERE myDrink NOT LIKE :filter ORDER BY myDrink ASC")
    suspend fun getDrinkType(filter:MyDrink? = null): List<MyDrink>

    // 제목을 받아, 정보를 반환하는 함수
    @Query("SELECT title FROM diary_base WHERE title = :title ORDER BY title ASC LIMIT 1")
    suspend fun checkTitle(title: String): String?

    // 제목을 받아, 정보를 반환하는 함수
    @Query("SELECT title FROM diary_base WHERE date = :date AND title =:title ORDER BY date ASC LIMIT 1")
    suspend fun checkTitleAndDate(date: MyDate, title: String): String?

    // 특정 DiaryBase 를 가져오는 함수
    @Query("SELECT importance FROM diary_base WHERE date = :date AND title =:title LIMIT 1")
    suspend fun checkDiaryBaseImportanceByDateAndTitle(date: MyDate, title: String) : Boolean

    // 기록된 것 중 중요표시 안된 날짜를 List 로 가져오는 함수
    @Query("SELECT calendarDay FROM diary_base WHERE importance = :important ORDER BY calendarDay ASC")
    suspend fun getNotImportantCalendarDayForDecoratorBySuspend(important : Boolean = false): List<CalendarDay>

    // 기록된 것 중 중요한 날짜를 List 로 가져오는 함수
    @Query("SELECT calendarDay FROM diary_base WHERE importance = :important  ORDER BY calendarDay ASC")
    suspend fun getImportantCalendarDayForDecoratorBySuspend(important : Boolean = true): List<CalendarDay>

    // 통계관련 함수
    // myDrink 가 존재하는 날짜 리스트
    @Query("SELECT dateForSort FROM diary_base WHERE myDrink NOT LIKE :filter ORDER BY dateForSort ASC")
    suspend fun getMyDrinkExistedDate(filter:MyDrink? = null): List<Int>
    // 특정 날짜 까지만 리스트를 가져오는 함수
    @Query("SELECT dateForSort FROM diary_base WHERE myDrink NOT LIKE :filter AND dateForSort > :recentDate ORDER BY dateForSort ASC")
    suspend fun getMyDrinkExistedDateRecent3Months(recentDate : Int, filter:MyDrink? = null): List<Int>

    // myDrink 가 존재하는 날짜 중 가장 오래된 날짜
    @Query("SELECT dateForSort FROM diary_base ORDER BY dateForSort ASC LIMIT 1")
    suspend fun getMyDrinkStartDate(): Int

    // myDrink 가 존재하는 날짜 중 가장 오래된 날짜
    @Query("SELECT dateForSort FROM diary_base ORDER BY dateForSort DESC LIMIT 1")
    suspend fun getMyDrinkRecentDate(): Int

    // myDrink 가 존재하는 모든 VOD 를 가져오는 함수
    @Query("SELECT dateForSort, myDrink FROM diary_base WHERE myDrink NOT LIKE :filter ORDER BY dateForSort ASC")
    suspend fun getAllVODByDateForSort(filter:MyDrink? = null): List<AllVODListBase>
    // myDrink 가 존재하는 최근 3개월 VOD 를 가져오는 함수
    @Query("SELECT dateForSort, myDrink FROM diary_base WHERE myDrink NOT LIKE :filter AND dateForSort > :recentDate ORDER BY dateForSort ASC")
    suspend fun getRecent3MonthsVODByDateForSort(recentDate : Int, filter:MyDrink? = null): List<AllVODListBase>

    // 모든 myDrink 을 가져오는 함수
    @Query("SELECT myDrink FROM diary_base WHERE myDrink NOT LIKE :filter")
    suspend fun getAllMyDrinkBySuspend(filter:MyDrink? = null): List<MyDrink>




    // 특정 DiaryBase 를 가져오는 함수
    @Query("SELECT * FROM diary_base WHERE date = :date AND title =:title LIMIT 1")
    fun getDiaryBaseByDateAndTitleFlow(date: MyDate, title: String): Flow<DiaryBase>

    // 모든 DiaryBase 를 날짜 오름차순으로 가져오는 함수
    @Query("SELECT * FROM diary_base ORDER BY dateForSort ASC")
    fun getAllDiaryBaseByDateASC(): Flow<List<DiaryBase>>

    // 모든 DiaryBase 를 날짜 내람차순으로 가져오는 함수
    @Query("SELECT * FROM diary_base ORDER BY dateForSort DESC")
    fun getAllDiaryBaseByDateDESC(): Flow<List<DiaryBase>>

    // 모든 myDrink 을 가져오는 함수
    @Query("SELECT myDrink FROM diary_base WHERE myDrink NOT LIKE :filter")
    fun getAllMyDrink(filter:MyDrink? = null): Flow<List<MyDrink>>

    // 주량이 기록된 데이터를 BaseDataForCombinedChartData 형식으로 가져오는 함수
    @Query("SELECT calendarDay, myDrink FROM diary_base WHERE myDrink NOT LIKE :filter ORDER BY dateForSort ASC")
    fun getBaseDataForCombinedChartData(filter:MyDrink? = null): Flow<List<BaseDataForCombinedChartData>>

    // 중요 표시된 DiaryBase 를 날짜 내람차순으로 가져오는 함수
    @Query("SELECT * FROM diary_base WHERE importance = :important ORDER BY dateForSort DESC")
    fun getDiaryBaseByImportance(important : Boolean = true): Flow<List<DiaryBase>>

    // 특정 날짜의 DiaryBase 를 중요한 것부터 가져오는 함수
    @Query("SELECT * FROM diary_base WHERE date = :date ORDER BY importance DESC")
    fun getDiaryBaseFlowInDate(date: MyDate): Flow<List<DiaryBase>>

    // 기록 중 가장 오래된 날짜를 가져오는 함수
    @Query("SELECT date FROM diary_base ORDER BY dateForSort ASC LIMIT 1")
    fun getStartDate(): Flow<MyDate>

    // 기록 중 기록 일수만 모두 가져오는 함수
    @Query("SELECT dateForSort FROM diary_base ORDER BY dateForSort ASC")
    fun getRecordDate(): Flow<List<Int>>

}

data class BaseDataForCombinedChartData(
    var calendarDay : CalendarDay,
    var myDrink: MyDrink?
)