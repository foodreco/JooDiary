package com.dreamreco.joodiary.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
}