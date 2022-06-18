package com.dreamreco.joodiary.util

import android.Manifest
import android.content.Context
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.ui.statistics.MyMonth
import com.prolificinteractive.materialcalendarview.CalendarDay


fun EditText.setFocusAndShowKeyboard(context: Context) {
    this.requestFocus()
    setSelection(this.text.length)
    this.postDelayed({
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED)
    }, 100)
}

fun EditText.clearFocusAndHideKeyboard(context: Context) {
    this.clearFocus()
    this.postDelayed({
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
    }, 30)
//    val inputMethodManager =
//        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//    inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
}

fun Button.clearFocusAndHideKeyboard(context: Context) {
    this.clearFocus()
    this.postDelayed({
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
    }, 30)
}

// 필요없음??
//fun calenderDateToString(date: CalendarDay): String {
//    val year = date.year
//    val month = date.month + 1
//    val day = date.day
//    return "$year$month$day" //20220405
//}

const val CALENDAR_FRAGMENT = 0
const val LIST_FRAGMENT = 1

const val LOAD_NOTHING = 0
const val LOAD_IMAGE_FROM_GALLERY = 1
const val LOAD_IMAGE_FROM_CAMERA = 2


// Permisisons
val GET_DATA_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE
)

val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)

// Request Code
const val BUTTON1 = 100
const val BUTTON2 = 200
const val BUTTON3 = 300
const val BUTTON4 = 400
const val BUTTON5 = 500

// windowSoftInputMode 를 제어하는 코드
// manifest 지정 모드 : adjustPan

fun Window?.getSoftInputMode(): Int {
    return this?.attributes?.softInputMode ?: SOFT_INPUT_ADJUST_PAN
}

class InputModeLifecycleHelper(
    private var window: Window?,
    private val mode: Mode = Mode.ADJUST_RESIZE
) : LifecycleObserver {

    private var originalMode: Int = SOFT_INPUT_ADJUST_PAN

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun setNewSoftInputMode() {
        window?.let {
            originalMode = it.getSoftInputMode()

            it.setSoftInputMode(
                when (mode) {
                    Mode.ADJUST_RESIZE -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    Mode.ADJUST_PAN -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                }
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun restoreOriginalSoftInputMode() {
        if (originalMode != SOFT_INPUT_ADJUST_UNSPECIFIED) {
            window?.setSoftInputMode(originalMode)
        }
        window = null
    }

    enum class Mode {
        ADJUST_RESIZE, ADJUST_PAN
    }
}

// CalendarDay 를 Int 로 변환하는 함수
fun CalendarDay.toDateInt(): Int {

    val year = this.year.toString()

    val month = this.month + 1
    var monthString = ""

    val day = this.day
    var dayString = ""

    if (month < 10) {
        monthString = "0$month"
    } else {
        monthString = month.toString()
    }

    if (day < 10) {
        dayString = "0$day"
    } else {
        dayString = day.toString()
    }

    return (year + monthString + dayString).toInt()
}

fun DiaryBase.toMonthInt(): Int {

    val year = this.date.year.toString()

    val month = this.date.month
    var monthString = ""


    if (month < 10) {
        monthString = "0$month"
    } else {
        monthString = month.toString()
    }

    return (year + monthString).toInt()
}

fun Int.intToMyMonth(): MyMonth {

    val stringInt = this.toString()

    val year = stringInt.substring(0, 4).toInt()

    val month = stringInt.substring(4, 6).toInt()

    return MyMonth(year, month)
}



//////////////////////////////////////////////////////////////////////////////////////////////////////
// // windowSoftInputMode 를 제어하는 코드 deprecated 대비형
//fun Window?.getSoftInputMode(): Int {
//    return this?.attributes?.softInputMode ?: SOFT_INPUT_ADJUST_PAN
//}
//
//class InputModeLifecycleHelper(
//    private var window: Window?,
//    private val mode: Mode = Mode.ADJUST_RESIZE
//) : LifecycleObserver {
//
//    private var originalMode: Int = SOFT_INPUT_ADJUST_PAN
//
//    private val lifecycleEventObserver = LifecycleEventObserver { source, event ->
//        if (event == Lifecycle.Event.ON_START) {
//                window?.let {
//                    originalMode = it.getSoftInputMode()
//
//                    it.setSoftInputMode(
//                        when (mode) {
//                            Mode.ADJUST_RESIZE -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
//                            Mode.ADJUST_PAN -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
//                        }
//                    )
//                }
//        } else if (event == Lifecycle.Event.ON_STOP) {
//                if (originalMode != SOFT_INPUT_ADJUST_PAN) {
//                    window?.setSoftInputMode(originalMode)
//                }
//                window = null
//        }
//    }
//
//    enum class Mode {
//        ADJUST_RESIZE, ADJUST_PAN
//    }
//}