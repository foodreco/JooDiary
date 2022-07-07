package com.dreamreco.joodiary.ui.calendar

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.dreamreco.joodiary.MyApplication
import com.dreamreco.joodiary.room.dao.CalendarDateDao
import com.dreamreco.joodiary.room.dao.DiaryBaseDao
import com.dreamreco.joodiary.room.dao.LoadImageSignalDao
import com.dreamreco.joodiary.room.entity.*
import com.dreamreco.joodiary.util.LOAD_NOTHING
import com.dreamreco.joodiary.util.toDateInt
import dagger.hilt.android.lifecycle.HiltViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val database: DiaryBaseDao,
    private val databaseForCalendarDate: CalendarDateDao,
    private val databaseForLoadImageSignal: LoadImageSignalDao,
    application: Application
) : AndroidViewModel(application) {

    private val _getMyDrinkDoneEvent = MutableLiveData<Boolean>()
    val getMyDrinkDoneEvent: LiveData<Boolean> = _getMyDrinkDoneEvent

    private val _listDuplication = MutableLiveData<Boolean>()
    val listDuplication: LiveData<Boolean> = _listDuplication

    private val _insertOrUpdateEventDone = MutableLiveData<Boolean>()
    val insertOrUpdateEventDone: LiveData<Boolean> = _insertOrUpdateEventDone

    private val _diaryBaseImportance = MutableLiveData<Boolean>()
    val diaryBaseImportance: LiveData<Boolean> = _diaryBaseImportance

    private val _dialogDeleteCompleted = MutableLiveData<Boolean>()
    val dialogDeleteCompleted: LiveData<Boolean> = _dialogDeleteCompleted

    private val _changeLoadImageTypeCompleted = MutableLiveData<Boolean>()
    val changeLoadImageTypeCompleted: LiveData<Boolean> = _changeLoadImageTypeCompleted

    private val _calendarFragmentAdapterBaseData = MutableLiveData<List<CalendarAdapterBase>>()
    val calendarFragmentAdapterBaseData: LiveData<List<CalendarAdapterBase>> =
        _calendarFragmentAdapterBaseData

    private val _dotForCalendar = MutableLiveData<DotForCalendar>()
    val dotForCalendar: LiveData<DotForCalendar> = _dotForCalendar


    // spinner 리스트 불러오는 함수 #1
    fun getMyDrink(text1: String, text2: String) {
        viewModelScope.launch {
            // DB 로부터 MyDrink 를 리스트 형태로 받아
            val myDrinkList = database.getDrinkType().distinct()
            val drinkTypeList = mutableListOf<String>()
            val VODList = mutableListOf<String>()
            val POAList = mutableListOf<String>()

            for (each in myDrinkList) {
                if ((each.drinkType != null) && (each.drinkType != "")) {
                    drinkTypeList.add(each.drinkType!!)
                }
                if ((each.VOD != null) && (each.VOD != "")) {
                    VODList.add(each.VOD!!)
                }
                if ((each.POA != null) && (each.POA != "")) {
                    POAList.add(each.POA!!)
                }
            }

            val arrangedDrinkTypeList = drinkTypeList.distinct()
            val arrangedVODList = VODList.distinct()
            val arrangedPOAList = POAList.distinct()

            val jsonListForDrinkType = JSONArray()
            // "", "직접입력"
            jsonListForDrinkType.put(text1)
            jsonListForDrinkType.put(text2)

            val jsonListForVOD = JSONArray()
            // "", "직접입력"
            jsonListForVOD.put(text1)
            jsonListForVOD.put(text2)

            val jsonListForPOA = JSONArray()
            // "", "직접입력"
            jsonListForPOA.put(text1)
            jsonListForPOA.put(text2)

            for (each in arrangedDrinkTypeList) {
                jsonListForDrinkType.put(each)
            }
            for (each in arrangedVODList) {
                jsonListForVOD.put(each)
            }
            for (each in arrangedPOAList) {
                jsonListForPOA.put(each)
            }

            val saveListForDrinkType = jsonListForDrinkType.toString()
            val saveListForVOD = jsonListForVOD.toString()
            val saveListForPOA = jsonListForPOA.toString()

            // SharedPreferences 에 저장
            MyApplication.prefs.setString("drinkType", saveListForDrinkType)
            MyApplication.prefs.setString("POA", saveListForPOA)
            MyApplication.prefs.setString("VOD", saveListForVOD)

            _getMyDrinkDoneEvent.value = true
        }
    }

//    // spinner 리스트 불러오는 함수 #2
//    fun getOnlyPOA(text1: String, text2: String) {
//        viewModelScope.launch {
//            // DB 로부터 그룹 명만 리스트 형태로 받아
//            val groupNameList = database.getPOA().distinct()
//            val jsonList = JSONArray()
//            // "", "직접입력"
//            jsonList.put(text1)
//            jsonList.put(text2)
//            for (i in groupNameList) {
//                if (i.trim() != "") {
//                    jsonList.put(i)
//                }
//            }
//            val saveList = jsonList.toString()
//            // SharedPreferences 에 저장
//            MyApplication.prefs.setString("POA", saveList)
//            _getPOADoneEvent.value = true
//        }
//    }
//
//    // spinner 리스트 불러오는 함수 #3
//    fun getOnlyVOD(text1: String, text2: String) {
//        viewModelScope.launch {
//            // DB 로부터 그룹 명만 리스트 형태로 받아
//            val groupNameList = database.getVOD().distinct()
//            val jsonList = JSONArray()
//            // "", "직접입력"
//            jsonList.put(text1)
//            jsonList.put(text2)
//            for (i in groupNameList) {
//                if (i.trim() != "") {
//                    jsonList.put(i)
//                }
//            }
//            val saveList = jsonList.toString()
//            // SharedPreferences 에 저장
//            MyApplication.prefs.setString("VOD", saveList)
//            _getVODDoneEvent.value = true
//        }
//    }

    // 달력에서 선택된 날짜를 가져오는 함수
    fun getRecentDate(): LiveData<CalendarDate> {
        return databaseForCalendarDate.getRecentDate().asLiveData()
    }

    //
    fun getNotImportantCalendarDayForDecoratorBySuspend() {
        viewModelScope.launch {
            val notImportantList = database.getNotImportantCalendarDayForDecoratorBySuspend()
            val importantList = database.getImportantCalendarDayForDecoratorBySuspend()
            val insert = DotForCalendar(notImportantList, importantList)
            _dotForCalendar.value = insert
        }
    }

    fun changeRecentDate(recentDate: CalendarDay) {
        viewModelScope.launch {
            val insertDate = CalendarDate(
                MyDate(recentDate.year, recentDate.month + 1, recentDate.day),
                recentDate,
                0
            )
            databaseForCalendarDate.insert(insertDate)
        }
    }

    fun getDiaryBaseData(date: MyDate, title: String) {
        viewModelScope.launch {
            val isImportant = database.checkDiaryBaseImportanceByDateAndTitle(date, title)
            _diaryBaseImportance.value = isImportant
        }
    }


    // 신규데이터를 넣거나, 변경하는 코드
    // 타이틀 유무로, insert 와 update 구분하기
    fun insertOrUpdateData(
        newDiaryBase: DiaryBase,
        preDiaryBase: DiaryBase
    ) {
        viewModelScope.launch {
            // 1. 넘어온 title 이 "" 일 때, (신규로 저장할 때)
            if (preDiaryBase.title == "") {
                // 1) 새로 정한 타이틀이 존재하는지 확인
                val titleIsPreExist: String? = database.checkTitle(newDiaryBase.title)
                if (titleIsPreExist == null) {
                    // 존재하지 않으면 insert
                    insertDiaryBase(newDiaryBase)
                } else {
                    val titleAndDateIsPreExist: String? =
                        database.checkTitleAndDate(newDiaryBase.date, newDiaryBase.title)
                    if (titleAndDateIsPreExist == null) {
                        // 존재하더라도, 동일날짜에 동일 타이틀이 없으면 insert
                        insertDiaryBase(newDiaryBase)
                    } else {
                        // 동일날짜, 동일 타이틀이 존재하면
                        _listDuplication.value = true
                    }
                }
            } else {
                // 2.preDataTitle 이 "" 이 아닐 때, (기존 것을 수정할 때,) update
                // 내용만 변경되면 update
                val updateList = DiaryBase(
                    newDiaryBase.image,
                    newDiaryBase.date,
                    newDiaryBase.calendarDay,
                    newDiaryBase.title,
                    newDiaryBase.content,
                    newDiaryBase.myDrink,
                    newDiaryBase.importance,
                    newDiaryBase.calendarDay.toDateInt(),
                    newDiaryBase.bitmapForRecyclerView,
                    preDiaryBase.id
                )
                if ((newDiaryBase.title == preDiaryBase.title) && (newDiaryBase.date == preDiaryBase.date)) {
                    updateDiaryBase(updateList)
                } else {
                    // 해당 날짜에 동일한 title 존재하는지 확인
                    val titleAndDateIsPreExist: String? =
                        database.checkTitleAndDate(newDiaryBase.date, newDiaryBase.title)
                    if (titleAndDateIsPreExist == null) {
                        // 존재하더라도, 동일날짜에 동일 타이틀이 없으면 insert
                        updateDiaryBase(updateList)
                    } else {
                        // 동일날짜, 동일 타이틀이 존재하면
                        _listDuplication.value = true
                    }
                }
            }
        }
    }


    fun deleteData(deleteDate: MyDate, deleteTitle: String) {
        viewModelScope.launch {
            database.deleteByTitleAndDate(deleteDate, deleteTitle)
            _dialogDeleteCompleted.value = true
        }
    }

    private fun insertDiaryBase(diaryBase: DiaryBase) {
        viewModelScope.launch {
            database.insert(diaryBase)
            _insertOrUpdateEventDone.value = true
        }
    }

    private fun updateDiaryBase(diaryBase: DiaryBase) {
        viewModelScope.launch {
            database.update(diaryBase)
            _insertOrUpdateEventDone.value = true
        }
    }

    fun listDuplicationDone() {
        _listDuplication.value = false
    }

    // 특정 날짜의 DiaryBase List 를 중요한 것부터 가져오는 코드
    fun getDiaryBaseFlowInDate(date: MyDate): LiveData<List<DiaryBase>> {
        return database.getDiaryBaseFlowInDate(date).asLiveData()
    }

    fun makeList(diaryBase: List<DiaryBase>) {
        val listItems = diaryBase.toListItems()
        _calendarFragmentAdapterBaseData.postValue(listItems)
    }

    // DB 에서 가져온 리스트 가공 (미리 이름순으로 정렬한 리스트를 가져와야 함)
    private fun List<DiaryBase>.toListItems(): List<CalendarAdapterBase> {
        val result = arrayListOf<CalendarAdapterBase>() // 결과를 리턴할 리스트
        if (this == emptyList<DiaryBase>()) {
            result.add(CalendarAdapterBase.EmptyHeader())
        } else {
            this.forEach { diaryBase ->
                // entity 데이터 추가
                result.add(CalendarAdapterBase.Item(diaryBase))
            }
        }
        return result
    }

    // 앱 시작 시, calendarDate 를 today 로 리셋하는 함수
    fun calendarDateReset() {
        viewModelScope.launch {
            val today = CalendarDay.today()
            val insertDate = CalendarDate(MyDate(today.year, today.month + 1, today.day), today, 0)
            databaseForCalendarDate.insert(insertDate)
        }
    }

    fun setImageLoadType(loadType: Int) {
        viewModelScope.launch {
            val insertDate = LoadImageSignal(loadType, 0)
            databaseForLoadImageSignal.insert(insertDate)
            _changeLoadImageTypeCompleted.value = true
        }
    }

    fun getImageChangeSignal(): LiveData<Int> {
        return databaseForLoadImageSignal.getSignalFlow().asLiveData()
    }

    fun loadImageTypeReset() {
        viewModelScope.launch {
            val insertDate = LoadImageSignal(LOAD_NOTHING, 0)
            databaseForLoadImageSignal.insert(insertDate)
        }
    }


}

data class DotForCalendar(
    var notImportantList: List<CalendarDay>,
    var importantList: List<CalendarDay>
)