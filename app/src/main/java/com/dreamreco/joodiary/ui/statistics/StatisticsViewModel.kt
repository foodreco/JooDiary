package com.dreamreco.joodiary.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
}