package com.dreamreco.joodiary.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dreamreco.joodiary.room.dao.CalendarDateDao
import com.dreamreco.joodiary.room.dao.DiaryBaseDao
import com.dreamreco.joodiary.room.dao.LoadImageSignalDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val database: DiaryBaseDao,
    private val databaseForCalendarDate: CalendarDateDao,
    private val databaseForLoadImageSignal: LoadImageSignalDao,
    application: Application
) : AndroidViewModel(application) {



}