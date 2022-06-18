package com.dreamreco.joodiary.ui.list

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.dreamreco.joodiary.room.dao.DiaryBaseDao
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.room.entity.MyDate
import com.dreamreco.joodiary.ui.statistics.MyMonth
import com.dreamreco.joodiary.ui.statistics.PagerOutData
import com.dreamreco.joodiary.util.intToMyMonth
import com.dreamreco.joodiary.util.toMonthInt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val database: DiaryBaseDao,
    application: Application
) : AndroidViewModel(application) {

    private val _listFragmentDiaryData = MutableLiveData<List<ListFragmentAdapterBase>>()
    val listFragmentDiaryData: LiveData<List<ListFragmentAdapterBase>> = _listFragmentDiaryData

    private val _listFragmentPagerOutData = MutableLiveData<List<PagerOutData>>()
    val listFragmentPagerOutData: LiveData<List<PagerOutData>> =
        _listFragmentPagerOutData


    fun getAllDataDESC(): LiveData<List<DiaryBase>> {
        return database.getAllDiaryBaseByDateDESC().asLiveData()
    }

    fun getAllDataASC(): LiveData<List<DiaryBase>> {
        return database.getAllDiaryBaseByDateASC().asLiveData()
    }

    fun makeList(diaryData: List<DiaryBase>) {
        val listItems = diaryData.toListItems()
        _listFragmentDiaryData.postValue(listItems)
    }

    // DB 에서 가져온 리스트 가공 (미리 날짜별로 정렬한 리스트를 가져와야 함)
    private fun List<DiaryBase>.toListItems(): List<ListFragmentAdapterBase> {
        val result = arrayListOf<ListFragmentAdapterBase>() // 결과를 리턴할 리스트
        if (this == emptyList<DiaryBase>()) {
            result.add(ListFragmentAdapterBase.EmptyHeader())
        } else {
            var groupHeaderDate: MyDate = MyDate(0, 0, 0) // 빈 그룹날짜
            this.forEach { diaryBase ->
                // 날짜가 달라지면 그룹헤더를 추가.
                if (groupHeaderDate != diaryBase.date) {
                    result.add(ListFragmentAdapterBase.DateHeader(diaryBase))
                }

                // 그때의 item 추가.
                result.add(ListFragmentAdapterBase.Item(diaryBase))

                // 그룹날짜를 바로 이전 날짜로 설정.
                groupHeaderDate = diaryBase.date
            }
        }
        return result
    }

    fun getPagerOutData(): LiveData<List<DiaryBase>> {
        return database.getAllDiaryBaseByDateASC().asLiveData()
    }

    fun makePagerOutDataList(diaryData: List<DiaryBase>) {
//        val listItems = diaryData.toPagerOutListItems()
//        _listFragmentPagerOutData.postValue(listItems)
        
        toPagerOutDate(diaryData)
    }

    private fun toPagerOutDate(diaryData: List<DiaryBase>) {
        viewModelScope.launch {
            if (diaryData == emptyList<DiaryBase>()) {
                // 빈 리스트 헤더 만들기
                val listItems = emptyList<PagerOutData>()
                _listFragmentPagerOutData.postValue(listItems)
            } else {

                val result = mutableListOf<PagerOutData>()

//                var baseDate: MyMonth = diaryData.first().toMyMonth()

                var baseDate = diaryData.first().toMonthInt()

                Log.e("ListViewModel", "최초 baseDate : ${baseDate}")

                val pagerOutDataInnerList = mutableListOf<DiaryBase>()

                var idForPagerOutData = 1

                for (eachDiaryBase in diaryData) {

//                    반복하면서 pagerInnerData 가 마지막 것으로 리셋되어버리는 문제...
//                    data class 내부에 list 를 넣은 방법을 다시 확인해 봐야함..?

                    if (baseDate != eachDiaryBase.toMonthInt()) {

                        val addList = PagerOutData(idForPagerOutData, baseDate.intToMyMonth(), pagerOutDataInnerList)

                        result.add(addList)

                        Log.e("ListViewModel", "idForPagerOutData : ${idForPagerOutData}")
                        Log.e("ListViewModel", "baseDate : ${baseDate}")
                        Log.e(
                            "ListViewModel",
                            "pagerOutDataInnerList 갯수 : ${pagerOutDataInnerList.size}"
                        )
                        Log.e(
                            "ListViewModel",
                            "result 반복 중 : ${result[idForPagerOutData - 1].pagerInnerData.size}"
                        )

                        for (item in result) {
                            Log.e("ListViewModel", " 처음 result id : ${item.id}")
                            Log.e("ListViewModel", " 처음 result 각 갯수 : ${item.pagerInnerData.size}")
                            for (i2 in item.pagerInnerData) {
                                Log.e("ListViewModel", " 처음 result dateForSort : ${i2.dateForSort}")
                            }
                        }


//                        여기까진 result 에 정상적으로 들어가는 것 같다.

                        ////////////////// 이 사이에서 result 갯수가 1개로 고정되버림

//                        변수 정의 및 설정부터 다시??

                        pagerOutDataInnerList.clear()
                        idForPagerOutData += 1

                        baseDate = eachDiaryBase.toMonthInt()
                        pagerOutDataInnerList.add(eachDiaryBase)

                        //////////////////

                        Log.e(
                            "ListViewModel",
                            "pagerOutDataInnerList 갯수22 : ${pagerOutDataInnerList.size}"
                        )
                        Log.e(
                            "ListViewModel",
                            "result 반복 중22 : ${result[idForPagerOutData - 2].pagerInnerData.size}"
                        )

                        for (item in result) {
                            Log.e("ListViewModel", " 중간 result id : ${item.id}")
                            Log.e("ListViewModel", " 중간 result 각 갯수 : ${item.pagerInnerData.size}")
                            for (i2 in item.pagerInnerData) {
                                Log.e("ListViewModel", " 중간 result dateForSort : ${i2.dateForSort}")
                            }
                        }

                    } else {
//                        baseDate = diaryBaseList.toMyMonth()
                        pagerOutDataInnerList.add(eachDiaryBase)
                    }
                }

                for (item in result) {
                    Log.e("ListViewModel", " 마지막 result id : ${item.id}")
                    Log.e("ListViewModel", " 마지막 result 각 갯수 : ${item.pagerInnerData.size}")
                    for (i2 in item.pagerInnerData) {
                        Log.e("ListViewModel", " 마지막 result dateForSort : ${i2.dateForSort}")
                    }
                }

                _listFragmentPagerOutData.postValue(result)
            }
        }
    }


//    // DB 에서 가져온 리스트 가공 (미리 날짜별로 정렬한 리스트를 가져와야 함)
//    private fun List<DiaryBase>.toPagerOutListItems(): List<PagerOutData> {
//        val result = arrayListOf<PagerOutData>() // 결과를 리턴할 리스트
//        if (this == emptyList<DiaryBase>()) {
////            result.add(ListFragmentAdapterBase.EmptyHeader())
//            // 아무것도 없을 때 처리해야함
//        } else {
//
//            Log.e("ListViewModel","최초 데이터 갯수 : ${this.size}")
//
//            for (i in this) {
//                Log.e("ListViewModel","전체 데이터 순서대로 나열 : ${i.dateForSort}")
//            }
//
//            var innerHeaderDate = this[0].toMyMonth() // 최초 Month 는 첫번째 날짜로 선정
//            Log.e("ListViewModel","최초 innerHeaderDate : $innerHeaderDate")
//
//            var pagerOutDataId = 1
//            val pagerInnerData = mutableListOf<DiaryBase>()
//
//            this.forEach { diaryBase ->
//
//                Log.e("ListViewModel","반복 diaryBase.toMyMonth() : ${diaryBase.toMyMonth()}")
//
//
//                // 날짜가 달라지면 PagerOutData 추가.
//                if (innerHeaderDate != diaryBase.toMyMonth()) {
//                    val addList = PagerOutData(pagerOutDataId, innerHeaderDate, pagerInnerData)
//                    Log.e("ListViewModel","반복문 내 addList id :${addList.id} innerHeaderDate : ${addList.myMonth}")
//                    result.add(addList)
//                    Log.e("ListViewModel","반복문 내 result : $innerHeaderDate 에 ${pagerInnerData.size} 개")
//                    pagerOutDataId += 1
//                    pagerInnerData.clear()
//                }
//
//                // 그때의 item 추가.
//                pagerInnerData.add(diaryBase)
//
//                // 그룹날짜를 바로 이전 날짜로 설정.
//                innerHeaderDate = diaryBase.toMyMonth()
//
////                마지막 개월이 추가되지 않고 (데이터만 추가됨),
////                이너 리싸이클러뷰 갱신 안됨 sub 로 전환 다시 하자
//
//            }
//        }
//
//        Log.e("ListViewModel","마지막 result : ${result.size} 개 요소 있음")
//
//        for (item in result) {
//            Log.e("ListViewModel"," result id : ${item.id}")
//            Log.e("ListViewModel"," result 각 갯수 : ${item.pagerInnerData.size}")
//            for (i2 in item.pagerInnerData) {
//                Log.e("ListViewModel"," result dateForSort : ${i2.dateForSort}")
//            }
//        }
//
//        return result
//    }


//    // 튜닝 2
//    // DB 에서 가져온 리스트 가공 (미리 날짜별로 정렬한 리스트를 가져와야 함)
//    private fun List<DiaryBase>.toPagerOutListItems(): List<PagerOutData> {
//
//        if (this == emptyList<DiaryBase>()) {
//            // 빈 리스트 헤더 만들기
//            return mutableListOf()
//        } else {
//
//            val result = mutableListOf<PagerOutData>()
//
//            var baseDate: MyMonth = this.first().toMyMonth()
//            Log.e("ListViewModel", "최초 baseDate : ${baseDate}")
//
//            val pagerOutDataInnerList = mutableListOf<DiaryBase>()
//
//            var idForPagerOutData = 1
//
//            for (diaryBaseList in this) {
//                if (baseDate != diaryBaseList.toMyMonth()) {
//                    val addList = PagerOutData(idForPagerOutData, baseDate, pagerOutDataInnerList)
//                    result.add(addList)
//                    Log.e("ListViewModel", "idForPagerOutData : ${idForPagerOutData}")
//                    Log.e("ListViewModel", "baseDate : ${baseDate}")
//                    Log.e(
//                        "ListViewModel",
//                        "pagerOutDataInnerList 갯수 : ${pagerOutDataInnerList.size}"
//                    )
//                    Log.e(
//                        "ListViewModel",
//                        "result 반복 중 : ${result[idForPagerOutData - 1].pagerInnerData.size}"
//                    )
//
//                    pagerOutDataInnerList.clear()
//                    idForPagerOutData += 1
//
//                    baseDate = diaryBaseList.toMyMonth()
//                    pagerOutDataInnerList.add(diaryBaseList)
//                } else {
//                    baseDate = diaryBaseList.toMyMonth()
//                    pagerOutDataInnerList.add(diaryBaseList)
//                }
//            }
//
//            for (item in result) {
//                Log.e("ListViewModel", " result id : ${item.id}")
//                Log.e("ListViewModel", " result 각 갯수 : ${item.pagerInnerData.size}")
//                for (i2 in item.pagerInnerData) {
//                    Log.e("ListViewModel", " result dateForSort : ${i2.dateForSort}")
//                }
//            }
//
//            return result
//        }
//
//
////        val result = arrayListOf<PagerOutData>() // 결과를 리턴할 리스트
////        if (this == emptyList<DiaryBase>()) {
//////            result.add(ListFragmentAdapterBase.EmptyHeader())
////            // 아무것도 없을 때 처리해야함
////        } else {
////
////            Log.e("ListViewModel","최초 데이터 갯수 : ${this.size}")
////
////            for (i in this) {
////                Log.e("ListViewModel","전체 데이터 순서대로 나열 : ${i.dateForSort}")
////            }
////
////            var innerHeaderDate = this[0].toMyMonth() // 최초 Month 는 첫번째 날짜로 선정
////            Log.e("ListViewModel","최초 innerHeaderDate : $innerHeaderDate")
////
////            var pagerOutDataId = 1
////            val pagerInnerData = mutableListOf<DiaryBase>()
////
////            this.forEach { diaryBase ->
////
////                Log.e("ListViewModel","반복 diaryBase.toMyMonth() : ${diaryBase.toMyMonth()}")
////
////
////                // 날짜가 달라지면 PagerOutData 추가.
////                if (innerHeaderDate != diaryBase.toMyMonth()) {
////                    val addList = PagerOutData(pagerOutDataId, innerHeaderDate, pagerInnerData)
////                    Log.e("ListViewModel","반복문 내 addList id :${addList.id} innerHeaderDate : ${addList.myMonth}")
////                    result.add(addList)
////                    Log.e("ListViewModel","반복문 내 result : $innerHeaderDate 에 ${pagerInnerData.size} 개")
////                    pagerOutDataId += 1
////                    pagerInnerData.clear()
////                }
////
////                // 그때의 item 추가.
////                pagerInnerData.add(diaryBase)
////
////                // 그룹날짜를 바로 이전 날짜로 설정.
////                innerHeaderDate = diaryBase.toMyMonth()
////
//////                마지막 개월이 추가되지 않고 (데이터만 추가됨),
//////                이너 리싸이클러뷰 갱신 안됨 sub 로 전환 다시 하자
////
////            }
////        }
////
////        Log.e("ListViewModel","마지막 result : ${result.size} 개 요소 있음")
////
////        for (item in result) {
////            Log.e("ListViewModel"," result id : ${item.id}")
////            Log.e("ListViewModel"," result 각 갯수 : ${item.pagerInnerData.size}")
////            for (i2 in item.pagerInnerData) {
////                Log.e("ListViewModel"," result dateForSort : ${i2.dateForSort}")
////            }
////        }
////
////        return result
//    }

    private fun DiaryBase.toMyMonth(): MyMonth {
        val year = this.date.year
        val month = this.date.month
        return MyMonth(year, month)
    }


}