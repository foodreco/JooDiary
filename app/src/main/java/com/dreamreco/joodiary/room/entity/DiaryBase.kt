package com.dreamreco.joodiary.room.entity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dreamreco.joodiary.util.toDateInt
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.parcelize.Parcelize
import java.util.function.DoubleConsumer

@Parcelize
@Entity(tableName = "diary_base")
data class DiaryBase(
    var image : Uri?,
    var date : MyDate,
    var calendarDay : CalendarDay,
    var title : String,
    var content : String?,
    var myDrink: MyDrink?,
    var importance : Boolean = false,
    var dateForSort : Int = 0,
    var bitmapForRecyclerView : Bitmap?,
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
) : Parcelable

@Parcelize
data class MyDate (
    var year : Int,
    var month : Int,
    var day : Int
) : Parcelable

@Parcelize
data class MyDrink (
    var drinkType : String?,
    var POA : String?,
    var VOD : String?
) : Parcelable

