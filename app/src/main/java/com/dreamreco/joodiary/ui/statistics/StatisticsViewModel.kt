package com.dreamreco.joodiary.ui.statistics

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.dreamreco.joodiary.room.dao.BaseDataForCombinedChartData
import com.dreamreco.joodiary.room.dao.DiaryBaseDao
import com.dreamreco.joodiary.room.entity.CalendarDate
import com.dreamreco.joodiary.room.entity.MyDate
import com.dreamreco.joodiary.room.entity.MyDrink
import com.dreamreco.joodiary.util.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt


@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val database: DiaryBaseDao,
    application: Application
) : AndroidViewModel(application) {

    private val _drinkTypeChartData = MutableLiveData<List<DrinkTypePieChartList>>()
    val drinkTypeChartData: LiveData<List<DrinkTypePieChartList>> = _drinkTypeChartData

    private val _combinedChartData = MutableLiveData<List<CombinedChartData>>()
    val combinedChartData: LiveData<List<CombinedChartData>> = _combinedChartData

    private val _horizontalProgressbarData = MutableLiveData<DrinkHorizontalProgressbarList?>()
    val horizontalProgressbarData: LiveData<DrinkHorizontalProgressbarList?> =
        _horizontalProgressbarData

    private val _drinkTendencyResult = MutableLiveData<DrinkTendency?>()
    val drinkTendencyResult: LiveData<DrinkTendency?> = _drinkTendencyResult

    private val _drinkRecent3MonthsTendencyResult = MutableLiveData<DrinkTendency?>()
    val drinkRecent3MonthsTendencyResult: LiveData<DrinkTendency?> = _drinkRecent3MonthsTendencyResult


    fun getMyDrinkListFlow(): LiveData<List<MyDrink>> {
        return database.getAllMyDrink().asLiveData()
    }

    fun getProgressBarBaseData(targetDrinkType : String){
        viewModelScope.launch {
            val allBaseDate : List<MyDrink> = database.getAllMyDrinkBySuspend()

            if (allBaseDate == emptyList<MyDrink>()) {
                _horizontalProgressbarData.postValue(null)
            } else {

                var allDrinkTimes: Float = 0f
                var allDrinkVOD: Float = 0f
                var allDrinkPVOA: Float = 0f

                var targetDrinkTimes: Float = 0f
                var targetDrinkVOD: Float = 0f
                var targetDrinkPVOA: Float = 0f

                for (each in allBaseDate) {

                    // 전체 데이터 도출 코드
                    allDrinkTimes += 1f
                    allDrinkVOD += each.VOD!!.toFloat()
                    allDrinkPVOA += (each.VOD!!.toFloat() * each.POA!!.toFloat() / 100)

                    // 타겟 데이터 토출 코드
                    if (each.drinkType == targetDrinkType) {
                        targetDrinkTimes += 1f
                        targetDrinkVOD += each.VOD!!.toFloat()
                        targetDrinkPVOA += (each.VOD!!.toFloat() * each.POA!!.toFloat() / 100)
                    }
                }

                val result = DrinkHorizontalProgressbarList(
                    allDrinkTimes,
                    targetDrinkTimes,
                    allDrinkVOD,
                    targetDrinkVOD,
                    allDrinkPVOA,
                    targetDrinkPVOA,
                    targetDrinkType
                )
                _horizontalProgressbarData.postValue(result)
            }
        }
    }

    fun getBaseDataForCombinedChartData(): LiveData<List<BaseDataForCombinedChartData>> {
        return database.getBaseDataForCombinedChartData().asLiveData()
    }

    fun makePieChartList(myDrinkList: List<MyDrink>) {
        val empty = mutableListOf<String>()
        for (eachMyDrink in myDrinkList) {
            if ((eachMyDrink.drinkType != null)) {
                if (eachMyDrink.drinkType!!.trim() != "") {
                    empty.add(eachMyDrink.drinkType!!)
                }
            } else {
                continue
            }
        }

        val data: List<String> = empty
        val drinkTypeData = mutableListOf<DrinkTypePieChartList>()
        // 맵 : 키-주종, 값-횟수
        val drinkTypeDataMap = mutableMapOf<String, Int>()

        // 주종을 리스트 형태로 모아서, 그룹당 기록 횟수를 출력하는 코드
        // 만약 data 가 empty 상태라면
        if (data == emptyList<String>()) {
            _drinkTypeChartData.postValue(drinkTypeData)
        } else {
            // data 가 empty 가 아니라면
            // data 를 순회하면서
            for (drinkType in data) {
                // 주종이 blank 처리되어있으면 카운트 하지 않는다.
                if (drinkType == "") {
                    continue
                }
                if (drinkTypeDataMap.contains(drinkType)) {
                    val preValue = drinkTypeDataMap[drinkType]
                    if (preValue != null) {
                        // 기존 값이 있으면 1을 더하고
                        drinkTypeDataMap[drinkType] = preValue + 1
                    }
                } else {
                    // 없으면 1로 시작한다.
                    drinkTypeDataMap[drinkType] = 1
                }
            }

            // groupDataMap 을 순회하면서 주종과 횟수를 모음
            for ((drinkType, times) in drinkTypeDataMap) {
                val list = DrinkTypePieChartList(drinkType, times.toString())
                drinkTypeData.add(list)
            }

            // 음주 횟수가 많은 주종부터 정렬하여 최종 출력한다.
            val result = drinkTypeData.sortedByDescending { it.drinkTimes.toInt() }
            _drinkTypeChartData.postValue(result)
        }
    }

    fun convertToCombinedChartData(baseList: List<BaseDataForCombinedChartData>) {
        viewModelScope.launch {
            val result = arrayListOf<CombinedChartData>() // 결과를 리턴할 리스트

            // empty 일 때...
            if (baseList == emptyList<BaseDataForCombinedChartData>()) {
                _combinedChartData.value = result
            } else {
                val dataPAODMap = mutableMapOf<MyMonth, Float>()
                val drinkTimesMap = mutableMapOf<MyMonth, Float>()

                for (eachDrinkTimes in baseList) {
                    // VOD 관련 기록이 없다면, 반복 skip
                    if ((eachDrinkTimes.myDrink?.VOD == null) || eachDrinkTimes.myDrink?.VOD == "") {
                        continue
                    }

                    if (dataPAODMap.contains(eachDrinkTimes.calendarDay.toMyMonth())) {
                        val preValue = dataPAODMap[eachDrinkTimes.calendarDay.toMyMonth()]
                        if (preValue != null) {
                            // 기존 값이 있으면 추가 주량을 더하고,
                            dataPAODMap[eachDrinkTimes.calendarDay.toMyMonth()] =
                                preValue + ((eachDrinkTimes.myDrink?.VOD!!.toFloat())*(eachDrinkTimes.myDrink?.POA!!.toFloat()))/100
                        }
                    } else {
                        // 없으면 해당 값으로 시작한다.
                        dataPAODMap[eachDrinkTimes.calendarDay.toMyMonth()] =
                            ((eachDrinkTimes.myDrink?.VOD!!.toFloat())*(eachDrinkTimes.myDrink?.POA!!.toFloat()))/100
                    }

                    if (drinkTimesMap.contains(eachDrinkTimes.calendarDay.toMyMonth())) {
                        val preValue = drinkTimesMap[eachDrinkTimes.calendarDay.toMyMonth()]
                        if (preValue != null) {
                            // 기존 값이 있으면 1을 더하고
                            drinkTimesMap[eachDrinkTimes.calendarDay.toMyMonth()] = preValue + 1f
                        }
                    } else {
                        // 없으면 1로 시작한다.
                        drinkTimesMap[eachDrinkTimes.calendarDay.toMyMonth()] = 1f
                    }
                }

                for ((myMonth, dataPAOD) in dataPAODMap) {
                    val drinkTimes = drinkTimesMap[myMonth]
                    val addList = drinkTimes?.let { CombinedChartData(myMonth, dataPAOD, it) }
                    if (addList != null) {
                        result.add(addList)
                    }
                }
            }
            _combinedChartData.value = result
        }
    }


    // 성향 기준 데이터를 업데이트하는 함수
    fun getTendencyBaseData() {
        viewModelScope.launch {
            //1. MyDrink 가 존재하는 날짜 수
            val myDrinkExistedDate = database.getMyDrinkExistedDate().distinct().count()
            Log.e("스테틱 뷰모델","전체 : $myDrinkExistedDate")
            // MyDrink 가 존재하는 날짜 수가 0개라면 데이터 부족으로 null 반환
            if (myDrinkExistedDate == 0) {
                _drinkTendencyResult.value = null
            } else {
                //3. 모든 주량 리스트
                //날짜순으로 정렬된 리스트를 가져와야 함
                val allVODListBase: List<AllVODListBase> = database.getAllVODByDateForSort()
                val allVODList = mutableListOf<AllVODList>()

                for (each in allVODListBase) {
                    if (each.myDrink != null) {
                        val addList = AllVODList(
                            each.dateForSort,
                            each.myDrink!!.VOD!!.toFloat(),
                            each.myDrink!!.POA!!.toFloat()
                        )
                        allVODList.add(addList)
                    } else {
                        continue
                    }
                }

                // 맵 : 키-dateForSort, 값-VOD
                // 일자별 주량 합계(한 날짜에 복수 기록이 있을 경우 통합하는 코드)
                val VODWithDateForSort = mutableMapOf<Int, Float>()

                // 일자별 알콜 섭취량 합계(한 날짜에 복수 기록이 있을 경우 통합하는 코드)
                val PAODWithDateForSort = mutableMapOf<Int, Float>()

                for (each in allVODList) {
                    if (VODWithDateForSort.contains(each.dateForSort)) {
                        val preValue = VODWithDateForSort[each.dateForSort]
                        if (preValue != null) {
                            // 기존 값이 있으면 1을 더하고
                            VODWithDateForSort[each.dateForSort] = preValue + each.VOD
                        }
                    } else {
                        // 없으면 추가한다.
                        VODWithDateForSort[each.dateForSort] = each.VOD
                    }

                    if (PAODWithDateForSort.contains(each.dateForSort)) {
                        val preValue = PAODWithDateForSort[each.dateForSort]
                        if (preValue != null) {
                            // 기존 값이 있으면 1을 더하고
                            PAODWithDateForSort[each.dateForSort] = preValue + (each.VOD*each.POA)/100
                        }
                    } else {
                        // 없으면 추가한다.
                        PAODWithDateForSort[each.dateForSort] = (each.VOD*each.POA)/100
                    }

                }

                //3-1. 총 평균 주량
                var totalVOD = 0f
                var totalMonthNumber = 0
                var baseMonth = ""

                //3-2. 총 평균 알콜섭취량
                var totalPAOD = 0f


                for ((dateForSort, VOD) in VODWithDateForSort) {
                    totalVOD += VOD
                    val month = dateForSort.toString().substring(0, 6)
                    if (baseMonth != month) {
                        baseMonth = month
                        totalMonthNumber += 1
                    }
                }

                for ((dateForSort, PAOD) in PAODWithDateForSort) {
                    totalPAOD += PAOD
                }


                // 월 평균 빈도
                val drinkFrequencyDayNumber: Int = kotlin.math.ceil(myDrinkExistedDate.toDouble() / totalMonthNumber.toDouble())
                    .toInt()

                // 총 평균 주량
                val averageVOD = (totalVOD / myDrinkExistedDate.toFloat()).roundToInt()

                // 총 평균 알콜섭취량
                val averagePAOD = (totalPAOD / myDrinkExistedDate.toFloat()).roundToInt()

                var drinkTendencyText = ""

                // 경향 5종(절주/애주가/술고래/술꾼/열반)
                // 빈도 + 알콜섭취량 기준한다.
                // 빈도 : 4회미만/4회~15회/4회~15회/15회이상/22회이상
                // 평균 주량 : 360/360~1080/1080~/1080~/1080~
                // 알콜 섭취량 : 61.2ml = 소주 1병

                if (drinkFrequencyDayNumber < 4) {
                    // 절주
                    drinkTendencyText = GRADE1
                } else {
                    if (drinkFrequencyDayNumber < 15) {
                        // 애주가 or 술고래
                        if (averagePAOD < ALCOHOL_PER_SOJU*3) {
                            drinkTendencyText = GRADE2
                        } else {
                            drinkTendencyText = GRADE3
                        }
                    } else {
                        if (drinkFrequencyDayNumber < 22) {
                            if (averagePAOD < ALCOHOL_PER_SOJU * 3) {
                                // 술꾼
                                drinkTendencyText = GRADE4
                            } else {
                                // 열반
                                drinkTendencyText = GRADE5
                            }
                        } else {
                            // 22회 이상이어도, 소주 1병 이하면
                            if (averageVOD < ALCOHOL_PER_SOJU) {
                                // 술꾼
                                drinkTendencyText =  GRADE4
                            } else {
                                // 열반
                                drinkTendencyText = GRADE5
                            }
                        }
                    }
                }

                val result = DrinkTendency(drinkFrequencyDayNumber,averageVOD, averagePAOD, drinkTendencyText)
                _drinkTendencyResult.value = result
            }
        }
    }

    // 최근 3개월 성향 기준 데이터를 업데이트하는 함수
    fun getTendencyBaseDataRecent3Months() {
        viewModelScope.launch {
            //1. 최근 3개월 내 MyDrink dateForSort
            val recent3MonthDateForSort = CalendarDay.today().toRecent3Months()
            val myDrinkExistedDate = database.getMyDrinkExistedDateRecent3Months(recent3MonthDateForSort).distinct().count()
            Log.e("스테틱 뷰모델","최근 3개월 : $myDrinkExistedDate")
            // MyDrink 가 존재하는 날짜 수가 0개라면 데이터 부족으로 null 반환
            if (myDrinkExistedDate == 0) {
                _drinkRecent3MonthsTendencyResult.value = null
            } else {
                //3. 최근 3개월 주량 리스트
                // 날짜순으로 정렬된 리스트를 가져와야 함
                val allVODListBase: List<AllVODListBase> = database.getRecent3MonthsVODByDateForSort(recent3MonthDateForSort)
                val allVODList = mutableListOf<AllVODList>()

                for (each in allVODListBase) {
                    if (each.myDrink != null) {
                        val addList = AllVODList(
                            each.dateForSort,
                            each.myDrink!!.VOD!!.toFloat(),
                            each.myDrink!!.POA!!.toFloat()
                        )
                        allVODList.add(addList)
                    } else {
                        continue
                    }
                }

                // 맵 : 키-dateForSort, 값-VOD
                // 일자별 주량 합계(한 날짜에 복수 기록이 있을 경우 통합하는 코드)
                val VODWithDateForSort = mutableMapOf<Int, Float>()

                // 일자별 알콜 섭취량 합계(한 날짜에 복수 기록이 있을 경우 통합하는 코드)
                val PAODWithDateForSort = mutableMapOf<Int, Float>()

                for (each in allVODList) {
                    if (VODWithDateForSort.contains(each.dateForSort)) {
                        val preValue = VODWithDateForSort[each.dateForSort]
                        if (preValue != null) {
                            // 기존 값이 있으면 1을 더하고
                            VODWithDateForSort[each.dateForSort] = preValue + each.VOD
                        }
                    } else {
                        // 없으면 추가한다.
                        VODWithDateForSort[each.dateForSort] = each.VOD
                    }

                    if (PAODWithDateForSort.contains(each.dateForSort)) {
                        val preValue = PAODWithDateForSort[each.dateForSort]
                        if (preValue != null) {
                            // 기존 값이 있으면 1을 더하고
                            PAODWithDateForSort[each.dateForSort] = preValue + (each.VOD*each.POA)/100
                        }
                    } else {
                        // 없으면 추가한다.
                        PAODWithDateForSort[each.dateForSort] = (each.VOD*each.POA)/100
                    }

                }

                //3-1. 총 평균 주량
                var totalVOD = 0f
                var totalMonthNumber = 0
                var baseMonth = ""

                //3-2. 총 평균 알콜섭취량
                var totalPAOD = 0f


                for ((dateForSort, VOD) in VODWithDateForSort) {
                    totalVOD += VOD
                    val month = dateForSort.toString().substring(0, 6)
                    if (baseMonth != month) {
                        baseMonth = month
                        totalMonthNumber += 1
                    }
                }

                for ((dateForSort, PAOD) in PAODWithDateForSort) {
                    totalPAOD += PAOD
                }


                // 월 평균 빈도
                val drinkFrequencyDayNumber: Int = kotlin.math.ceil(myDrinkExistedDate.toDouble() / totalMonthNumber.toDouble())
                    .toInt()

                // 총 평균 주량
                val averageVOD = (totalVOD / myDrinkExistedDate.toFloat()).roundToInt()

                // 총 평균 알콜섭취량
                val averagePAOD = (totalPAOD / myDrinkExistedDate.toFloat()).roundToInt()

                var drinkTendencyText = ""

                // 경향 5종(절주/애주가/술고래/술꾼/열반)
                // 빈도 + 알콜섭취량 기준한다.
                // 빈도 : 4회미만/4회~15회/4회~15회/15회이상/22회이상
                // 평균 주량 : 360/360~1080/1080~/1080~/1080~
                // 알콜 섭취량 : 61.2ml = 소주 1병

                if (drinkFrequencyDayNumber < 4) {
                    // 절주
                    drinkTendencyText = GRADE1
                } else {
                    if (drinkFrequencyDayNumber < 15) {
                        // 애주가 or 술고래
                        if (averagePAOD < ALCOHOL_PER_SOJU*3) {
                            drinkTendencyText = GRADE2
                        } else {
                            drinkTendencyText = GRADE3
                        }
                    } else {
                        if (drinkFrequencyDayNumber < 22) {
                            if (averagePAOD < ALCOHOL_PER_SOJU * 3) {
                                // 술꾼
                                drinkTendencyText = GRADE4
                            } else {
                                // 열반
                                drinkTendencyText = GRADE5
                            }
                        } else {
                            // 22회 이상이어도, 소주 1병 이하면
                            if (averageVOD < ALCOHOL_PER_SOJU) {
                                // 술꾼
                                drinkTendencyText =  GRADE4
                            } else {
                                // 열반
                                drinkTendencyText = GRADE5
                            }
                        }
                    }
                }

                val result = DrinkTendency(drinkFrequencyDayNumber,averageVOD, averagePAOD, drinkTendencyText)
                _drinkRecent3MonthsTendencyResult.value = result
            }
        }
    }
}

data class DrinkTypePieChartList(
    var drinkType: String,
    var drinkTimes: String,
)

data class CombinedChartData(
    var month: MyMonth,
    var PAOD: Float,
    var drinkTimes: Float
)

data class DrinkHorizontalProgressbarList(
    var lowDrinkTimes: Float,
    var highDrinkTimes: Float,
    var lowDrinkVOD: Float,
    var highDrinkVOD: Float,
    var lowDrinkPVOA: Float,
    var highDrinkPVOA: Float,
    var targetDrinkType: String
)

data class AllVODListBase(
    var dateForSort: Int,
    var myDrink: MyDrink?
)

data class AllVODList(
    var dateForSort: Int,
    var VOD: Float,
    var POA: Float
)

data class DrinkTendency(
    var drinkFrequencyDayNumber : Int,
    var averageVOD : Int,
    var averagePAOD : Int,
    var drinkTendency : String
)