package com.dreamreco.joodiary.ui.setting

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.room.PrimaryKey
import com.dreamreco.joodiary.room.dao.BaseDataForCombinedChartData
import com.dreamreco.joodiary.room.dao.DiaryBaseDao
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.room.entity.MyDate
import com.dreamreco.joodiary.room.entity.MyDrink
import com.dreamreco.joodiary.ui.statistics.DrinkTypePieChartList
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val database: DiaryBaseDao,
    application: Application
) : AndroidViewModel(application) {

    private val _numberOfRecordDays = MutableLiveData<Int>()
    val numberOfRecordDays: LiveData<Int> = _numberOfRecordDays

    private val _textForBackUp = MutableLiveData<String>()
    val textForBackUp: LiveData<String> = _textForBackUp

    fun getStartDate(): LiveData<MyDate> {
        return database.getStartDate().asLiveData()
    }

    fun getNumberOfRecord(): LiveData<List<Int>> {
        return database.getRecordDate().asLiveData()
    }

    fun countNumberOfRecords(dateList: List<Int>) {
        val recordDateList = dateList.distinct().count()
        _numberOfRecordDays.value = recordDateList
    }

    fun getBackUpText() {
        viewModelScope.launch {
            var resultText = ""
            val allData = database.getAllDiaryBaseByDateDESCForBackUp()
            if (allData != emptyList<DiaryBase>()) {
                for (each in allData) {
                    if (each.myDrink != null) {
                        val addText =
                            "\n" + "\n" +
                                    "제목 : ${each.title}" +
                                    "\n날짜 : ${each.date.year}년${each.date.month}월${each.date.day}일" +
                                    "\n기록 : ${each.myDrink!!.drinkType}/${each.myDrink!!.POA}%/${each.myDrink!!.VOD}ml" +
                                    "\n내용 : ${each.content?: "없음"}"
                        resultText += addText
                    } else {
                        val addText =
                            "\n" + "\n" +
                                    "제목 : ${each.title}" +
                                    "\n날짜 : ${each.date.year}년${each.date.month}월${each.date.day}일" +
                                    "\n기록 : 없음" +
                                    "\n내용 : ${each.content?: "없음"}"
                        resultText += addText
                    }
                }
            } else {
                resultText = "내용없음"
            }
            _textForBackUp.value = resultText
        }
    }
}