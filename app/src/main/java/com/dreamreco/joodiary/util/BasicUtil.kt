package com.dreamreco.joodiary.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
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
import com.dreamreco.joodiary.room.entity.MyDate
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit


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

const val LOAD_NOTHING = 0
const val LOAD_IMAGE_FROM_GALLERY = 1
const val LOAD_IMAGE_FROM_CAMERA = 2
const val IMAGE_DELETE = 3

const val SORT_NORMAL = 0
const val SORT_IMPORTANCE = 1

// 보안관련
const val LOGIN_TYPE = "login_type"
const val LOGIN_WITH_BIO = "login_with_bio"
const val LOGIN_WITH_PASSWORD = "login_with_password"
const val LOGIN_WITH_NOTHING = "login_with_nothing"
// 테마 관련
const val THEME_TYPE = "theme_type"
const val THEME_1 = "theme1"
const val THEME_2 = "theme2"
const val THEME_BASIC = "theme_basic"
// 로그인 상태
const val LOGIN_STATE = "login_state"
const val LOGIN_CLEAR = "login_clear"
const val LOGIN_NOT_CONFIRM = "login_not_confirm"
const val KEY_NAME = "key_name"

// 비밀번호
const val PASSWORD_KEY = "password_key"
const val NO_REGISTERED_PASSWORD = "empty"

//경향 관련
const val ALCOHOL_PER_SOJU = 61.2
const val GRADE1 = "절주 : 술을 절제하는 사람"
const val GRADE2 = "애주가 : 술을 사랑하는 사람"
const val GRADE3 = "술고래 : 주량이 많은 사람"
const val GRADE4 = "술꾼 : 프로의 경지에 이른 사람"
const val GRADE5 = "열반 : 술로 도를 터득한 사람"


// Permissions
val GET_DATA_PERMISSIONS = arrayOf(
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE
)

val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)


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

// uri 를 받아 이미지를 원하는 사이즈 이하로 줄여주는 코드
fun decodeSampledBitmapFromInputStream(
    photoUri : Uri,
    reqWidth: Int,
    reqHeight: Int,
    context: Context
): Bitmap? {

    var fileInputStream: InputStream =
        context.contentResolver.openInputStream(photoUri)!!

    // First decode with inJustDecodeBounds=true to check dimensions
    return BitmapFactory.Options().run {
        inJustDecodeBounds = true
        BitmapFactory.decodeStream(fileInputStream, null, this)

        // Calculate inSampleSize
        // 줄이려는 사이즈 배수(inSampleSize = 2 이면 원래 10MB -> 5MB 로 줄임)
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

        fileInputStream.close()

        fileInputStream = context.contentResolver.openInputStream(photoUri)!!

        // Decode bitmap with inSampleSize set
        inJustDecodeBounds = false

//        BitmapFactory.decodeStream(fileInputStream, null, this)

        // 이미지 회전을 본래대로 반영하는 코드
        val result =  ImageOrientation.modifyOrientation(
            context,
            BitmapFactory.decodeStream(fileInputStream, null, this)!!,
            photoUri
        )

        fileInputStream.close()

        result
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    // 불러온 이미지의 폭, 넓이
    val (height: Int, width: Int) = options.run { outHeight to outWidth }

    var inSampleSize = 1

    // 이미지 크기가 기준 초과하는 경우
    if (height > reqHeight || width > reqWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    // 배수만큼 본 사이즈를 줄임
    return inSampleSize
}


// CalendarDay 를 Int 로 변환하는 함수
// dateForSort 변환용
// yyyymmdd
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

fun intToDateInt(year: Int, month: Int, day: Int): Int {
    val thisYear = year.toString()
    var monthString = ""
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
    return (thisYear + monthString + dayString).toInt()
}


fun CalendarDay.toMyMonth(): MyMonth {
    val year = this.year
    val month = this.month + 1
    return MyMonth(year, month)
}

fun CalendarDay.toRecent3Months(): Int {
    var year = this.year
    var month = this.month + 1
    var monthString = ""

    val day = this.day
    var dayString = ""

    // 3개월 전까지 dateForSort 계산
    month -= 3

    if (month == 0) {
        monthString = "12"
        year -= 1
    } else if (month == -1) {
        monthString = "11"
        year -= 1
    } else if (month == -2) {
        monthString = "10"
        year -= 1
    } else {
        monthString = "0$month"
    }

    if (day < 10) {
        dayString = "0$day"
    } else {
        dayString = day.toString()
    }
    return (year.toString() + monthString + dayString).toInt()
}

fun MyMonth.toMonthString(): String {

    // 00년으로 표기
    val year = this.year.toString().substring(2, 4)

    val month = this.month
    var monthString = ""

    if (month < 10) {
        monthString = "0$month"
    } else {
        monthString = month.toString()
    }

    return "$year.$monthString"
}


// 두 일자 사이의 날짜 수를 계산하는 함수
// 적용 일자는 yyyymmdd 방식으로 표현된 String 이어야 함
// dateForSort 적용
@SuppressLint("SimpleDateFormat")
fun calculateDuration(startDate: String, endDate: String) : Int {
    val sdf = SimpleDateFormat("yyyyMMdd")

    val startDateValue = sdf.parse(startDate)
    val endDateValue = sdf.parse(endDate)

    val diff: Long = endDateValue!!.time - startDateValue!!.time

    return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()+1
}



data class MyMonth(
    var year: Int,
    var month: Int,
)

// 맞춤 차트 그래프 색
val CUSTOM_CHART_COLORS = arrayListOf<Int>(
    Color.rgb(0, 205, 62), Color.rgb(0, 144, 205), Color.rgb(0, 112, 64), Color.rgb(0, 205, 164),
    Color.rgb(0, 94, 152), Color.rgb(9, 0, 197),
    Color.rgb(0, 113, 0),
    Color.rgb(0, 100, 54),
    Color.rgb(0, 168, 0),
    Color.rgb(0, 147, 97),
    Color.rgb(97, 24, 219)
)



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