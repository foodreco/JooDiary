package com.dreamreco.joodiary.ui.statistics

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.dreamreco.joodiary.room.dao.BaseDataForCombinedChartData
import com.dreamreco.joodiary.room.dao.CalendarDateDao
import com.dreamreco.joodiary.room.dao.DiaryBaseDao
import com.dreamreco.joodiary.room.dao.LoadImageSignalDao
import com.dreamreco.joodiary.room.entity.MyDrink
import com.dreamreco.joodiary.util.MyMonth
import com.dreamreco.joodiary.util.toMyMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

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
    val horizontalProgressbarData: LiveData<DrinkHorizontalProgressbarList?> = _horizontalProgressbarData


    fun getMyDrinkListFlow(): LiveData<List<MyDrink>> {
        return database.getAllMyDrink().asLiveData()
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

            // groupDataMap 을 순회하면서 name : 그룹명, numbers : 사람수
            for ((drinkType, times) in drinkTypeDataMap) {
                val list = DrinkTypePieChartList(drinkType, times.toString())
                drinkTypeData.add(list)
            }

            // 사람수가 많은 그룹부터 정렬하여 최종 출력한다.
            val result = drinkTypeData.sortedByDescending { it.drinkTimes.toInt() }
            _drinkTypeChartData.postValue(result)
        }
    }

    fun makeHorizontalProgressbarList(myDrinkList: List<MyDrink>) {
        if (myDrinkList == emptyList<MyDrink>()) {
            _horizontalProgressbarData.postValue(null)
        } else {
            val lowMyDrinkList = mutableListOf<MyDrink>()
            val highMyDrinkList = mutableListOf<MyDrink>()

            for (each in myDrinkList) {
                if ((each.POA == null) || (each.POA.toString() == "")) {
                    continue
                } else {
                    if (each.POA!!.toFloat() < 20) {
                        // 도수가 20도 보다 작으면 저도주
                        lowMyDrinkList.add(each)
                    } else {
                        highMyDrinkList.add(each)
                    }
                }
            }
            var lowDrinkTimes: Float = 0f
            var highDrinkTimes: Float = 0f
            var lowDrinkVOD: Float = 0f
            var highDrinkVOD: Float = 0f
            var lowDrinkPVOA: Float = 0f
            var highDrinkPVOA: Float = 0f

            for (each in lowMyDrinkList) {
                lowDrinkTimes += 1f
                lowDrinkVOD += each.VOD!!.toFloat()
                lowDrinkPVOA += (each.VOD!!.toFloat() * each.POA!!.toFloat() / 100)
            }

            for (each in highMyDrinkList) {
                highDrinkTimes += 1f
                highDrinkVOD += each.VOD!!.toFloat()
                highDrinkPVOA += (each.VOD!!.toFloat() * each.POA!!.toFloat() / 100)
            }

            val result = DrinkHorizontalProgressbarList(
                lowDrinkTimes, highDrinkTimes, lowDrinkVOD, highDrinkVOD, lowDrinkPVOA, highDrinkPVOA
            )
            _horizontalProgressbarData.postValue(result)
        }
    }

    fun convertToCombinedChartData(baseList: List<BaseDataForCombinedChartData>) {
        val convertedList = baseList.toCombinedChartData()
        _combinedChartData.value = convertedList
    }

    private fun List<BaseDataForCombinedChartData>.toCombinedChartData() : List<CombinedChartData> {

        val result = arrayListOf<CombinedChartData>() // 결과를 리턴할 리스트

        if (this == emptyList<BaseDataForCombinedChartData>()) {
            // empty 일 때...
        } else {

            val dataVODMap = mutableMapOf<MyMonth, Float>()
            val drinkTimesMap = mutableMapOf<MyMonth, Float>()

            for (eachDrinkTimes in this) {
                // VOD 관련 기록이 없다면, 반복 skip
                if ((eachDrinkTimes.myDrink.VOD == null)||eachDrinkTimes.myDrink.VOD == "") {
                    continue
                }

                if (dataVODMap.contains(eachDrinkTimes.calendarDay.toMyMonth())) {
                    val preValue = dataVODMap[eachDrinkTimes.calendarDay.toMyMonth()]
                    if (preValue != null) {
                        // 기존 값이 있으면 1을 더하고
                        dataVODMap[eachDrinkTimes.calendarDay.toMyMonth()] = preValue + eachDrinkTimes.myDrink.VOD!!.toFloat()
                    }
                } else {
                    // 없으면 1로 시작한다.
                    dataVODMap[eachDrinkTimes.calendarDay.toMyMonth()] = eachDrinkTimes.myDrink.VOD!!.toFloat()
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


            for ((myMonth, dataVOD) in dataVODMap) {
                val drinkTimes = drinkTimesMap[myMonth]
                val addList = drinkTimes?.let { CombinedChartData(myMonth, dataVOD, it) }
                if (addList != null) {
                    result.add(addList)
                }
            }
        }
        return result
    }

}


data class DrinkTypePieChartList(
    var drinkType: String,
    var drinkTimes: String,
)

data class CombinedChartData(
    var month : MyMonth,
    var VOD : Float,
    var drinkTimes : Float
)

data class DrinkHorizontalProgressbarList(
    var lowDrinkTimes: Float,
    var highDrinkTimes: Float,
    var lowDrinkVOD: Float,
    var highDrinkVOD: Float,
    var lowDrinkPVOA: Float,
    var highDrinkPVOA: Float,
)