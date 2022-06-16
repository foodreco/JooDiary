package com.dreamreco.joodiary.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.dreamreco.joodiary.room.dao.DiaryBaseDao
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.room.entity.MyDate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val database : DiaryBaseDao,
    application: Application
) : AndroidViewModel(application) {

    private val _listFragmentDiaryData = MutableLiveData<List<ListFragmentAdapterBase>>()
    val listFragmentDiaryData: LiveData<List<ListFragmentAdapterBase>> = _listFragmentDiaryData


    fun getAllDataDESC(): LiveData<List<DiaryBase>> {
        return database.getAllDiaryBaseByDateDESC().asLiveData()
    }

    fun getAllDataASC(): LiveData<List<DiaryBase>> {
        return database.getAllDiaryBaseByDateASC().asLiveData()
    }

    fun makeList(callLogData: List<DiaryBase>) {
        val listItems = callLogData.toListItems()
        _listFragmentDiaryData.postValue(listItems)
    }

    // DB 에서 가져온 리스트 가공 (미리 날짜별로 정렬한 리스트를 가져와야 함)
    private fun List<DiaryBase>.toListItems(): List<ListFragmentAdapterBase> {
        val result = arrayListOf<ListFragmentAdapterBase>() // 결과를 리턴할 리스트
        if (this == emptyList<DiaryBase>()) {
            result.add(ListFragmentAdapterBase.EmptyHeader())
        } else {
            var groupHeaderDate : MyDate = MyDate(0,0,0) // 빈 그룹날짜
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

}