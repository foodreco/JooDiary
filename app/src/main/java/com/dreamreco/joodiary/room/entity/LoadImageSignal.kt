package com.dreamreco.joodiary.room.entity

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.parcelize.Parcelize
import java.util.function.DoubleConsumer

// GetImageDialog 의 이미지를 가져오는 방식을 위한 entity
@Parcelize
@Entity(tableName = "load_image_signal")
data class LoadImageSignal(
    var loadType : Int = 0,
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0
) : Parcelable





