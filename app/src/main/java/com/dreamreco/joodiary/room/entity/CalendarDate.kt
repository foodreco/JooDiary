package com.dreamreco.joodiary.room.entity

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.parcelize.Parcelize
import java.util.function.DoubleConsumer

// CalendarFragment 의 캘린더 유지를 위한 entity
@Parcelize
@Entity(tableName = "calendar_date")
data class CalendarDate(
    var date: MyDate,
    var calendarDate : CalendarDay,
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0
) : Parcelable





