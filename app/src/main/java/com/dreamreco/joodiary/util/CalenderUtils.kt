package com.dreamreco.joodiary.util

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.dreamreco.joodiary.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

class TodayDecorator(context: Context): DayViewDecorator {
    private var date = CalendarDay.today()
    val drawable = context.resources.getDrawable(R.drawable.style_only_radius_10)

    // day 가 today()와 같은지 확인 후 true 라면 decorate()로 이동
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day?.equals(date)!!
    }
    override fun decorate(view: DayViewFacade?) {
//        view?.setBackgroundDrawable(drawable)
        view?.addSpan(object: ForegroundColorSpan(Color.DKGRAY){})
    }
}

//weekDay : 어떤 요일인지 판별
//일요일이라면 dacorate() 로 이동
//view?.addSpan() 으로 해당 날짜의 색 변경
class SundayDecorator:DayViewDecorator {
    private val calendar = Calendar.getInstance()
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.copyTo(calendar)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay == Calendar.SUNDAY
    }
    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object: ForegroundColorSpan(Color.RED){})
    }
}

class SaturdayDecorator:DayViewDecorator {
    private val calendar = Calendar.getInstance()
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.copyTo(calendar)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay == Calendar.SATURDAY
    }
    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object:ForegroundColorSpan(Color.BLUE){})
    }
}

//minDay와 maxDay 설정 후 기간 외인지 판별
//기간 외라면 날짜의 색 #d2d2d2로 변경 & 선택 불가능하게 변경
class MinMaxDecorator(min:CalendarDay, max:CalendarDay):DayViewDecorator {
    val maxDay = max
    val minDay = min
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return (day?.month == maxDay.month && day.day > maxDay.day)
                || (day?.month == minDay.month && day.day < minDay.day)
    }
    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object:ForegroundColorSpan(Color.parseColor("#d2d2d2")){})
        view?.setDaysDisabled(true)
    }
}

//minMaxDecorator 와 마찬가지로 기간 설정
//단, 기간이 3달이므로 가운데 껴있는 월(7,8월)도 포함
//날짜의 Style, Size 변경
class BoldDecorator(min:CalendarDay, max:CalendarDay):DayViewDecorator {
    val maxDay = max
    val minDay = min
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return (day?.month == maxDay.month && day.day <= maxDay.day)
                || (day?.month == minDay.month && day.day >= minDay.day)
                || (minDay.month < day?.month!! && day.month < maxDay.month)
    }
    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object: StyleSpan(Typeface.NORMAL){})
        view?.addSpan(object: RelativeSizeSpan(1.4f){})
    }
}