package com.dreamreco.joodiary.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

}