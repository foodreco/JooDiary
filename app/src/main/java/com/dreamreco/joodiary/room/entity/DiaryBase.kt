package com.dreamreco.joodiary.room.entity

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "diary_base")
data class DiaryBase(
    var date : String,
    var title : String,
    var content : String?,
    var liquidType : String?,
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
) : Parcelable


