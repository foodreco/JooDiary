package com.dreamreco.joodiary.room.entity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.function.DoubleConsumer

@Parcelize
@Entity(tableName = "diary_base")
data class DiaryBase(
    var image : Uri?,
    var date : MyDate,
    var title : String,
    var content : String?,
    var drinkType : String?,
    var POA : String?,
    var VOD : String?,
    var importance : Boolean = false,
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
) : Parcelable

@Parcelize
data class MyDate (
    var year : Int,
    var month : Int,
    var day : Int
) : Parcelable





