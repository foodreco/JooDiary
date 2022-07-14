package com.dreamreco.joodiary.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import com.dreamreco.joodiary.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.util.*


class TodayDecorator(context: Context) : DayViewDecorator {
    private val mContext = context
    private var date = CalendarDay.today()

    @SuppressLint("UseCompatLoadingForDrawables")
    val drawable = context.resources.getDrawable(R.drawable.style_only_radius_10, null)

    // day 가 today()와 같은지 확인 후 true 라면 decorate()로 이동
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day?.equals(date)!!
    }

    override fun decorate(view: DayViewFacade?) {
//        view?.setBackgroundDrawable(drawable)
//        view?.addSpan(object: ForegroundColorSpan(Color.GREEN){})
        view?.addSpan(object : ForegroundColorSpan(getColor(mContext, R.color.teal_200)) {})
        view?.addSpan(object : StyleSpan(Typeface.BOLD) {})
        view?.addSpan(object : RelativeSizeSpan(1.3f) {})

    }
}

//weekDay : 어떤 요일인지 판별
//일요일이라면 dacorate() 로 이동
//view?.addSpan() 으로 해당 날짜의 색 변경
class SundayDecorator : DayViewDecorator {
    private val calendar = Calendar.getInstance()
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.copyTo(calendar)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay == Calendar.SUNDAY
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object : ForegroundColorSpan(Color.RED) {})
    }
}

class SundayDecoratorForDark(context: Context) : DayViewDecorator {
    private val calendar = Calendar.getInstance()
    private val mContext = context
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.copyTo(calendar)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay == Calendar.SUNDAY
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object : ForegroundColorSpan(getColor(mContext, R.color.sunday_for_dark)){})
    }
}



class SaturdayDecorator : DayViewDecorator {
    private val calendar = Calendar.getInstance()
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.copyTo(calendar)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay == Calendar.SATURDAY
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object : ForegroundColorSpan(Color.BLUE) {})
    }
}

class SaturdayDecoratorForDark(context: Context) : DayViewDecorator {
    private val calendar = Calendar.getInstance()
    private val mContext = context
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.copyTo(calendar)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay == Calendar.SATURDAY
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object : ForegroundColorSpan(getColor(mContext, R.color.saturday_for_dark)) {})
    }
}

//minDay와 maxDay 설정 후 기간 외인지 판별
//기간 외라면 날짜의 색 #d2d2d2로 변경 & 선택 불가능하게 변경
class MinMaxDecorator(min: CalendarDay, max: CalendarDay) : DayViewDecorator {
    val maxDay = max
    val minDay = min
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return (day?.month == maxDay.month && day.day > maxDay.day)
                || (day?.month == minDay.month && day.day < minDay.day)
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object : ForegroundColorSpan(Color.parseColor("#d2d2d2")) {})
        view?.setDaysDisabled(true)
    }
}

//minMaxDecorator 와 마찬가지로 기간 설정
//단, 기간이 3달이므로 가운데 껴있는 월(7,8월)도 포함
//날짜의 Style, Size 변경
class BoldDecorator(min: CalendarDay, max: CalendarDay) : DayViewDecorator {
    val maxDay = max
    val minDay = min
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return (day?.month == maxDay.month && day.day <= maxDay.day)
                || (day?.month == minDay.month && day.day >= minDay.day)
                || (minDay.month < day?.month!! && day.month < maxDay.month)
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(object : StyleSpan(Typeface.NORMAL) {})
        view?.addSpan(object : RelativeSizeSpan(1.4f) {})
    }
}

// 특정 날짜에 점을 표시하는 Decorator
class EventDecoratorForImportantData(context: Context, dates: Collection<CalendarDay>) : DayViewDecorator {
    private val mContext = context
    var dates: HashSet<CalendarDay> = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(DotSpan(5F, mContext.getColor(R.color.calendar_dot_important)))
    }
}

// 특정 날짜에 점을 표시하는 Decorator 2
// LineBackgroundSpan 으로 적용되기 때문에, 글자색도 영향 받음
class EventDecorator(context: Context, dates: Collection<CalendarDay>) :
    DayViewDecorator {
    private val mContext = context
    var dates: HashSet<CalendarDay> = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(CustomMultipleDotSpan(5f, mContext))
    }
}

class EventDecoratorForDark(context: Context, dates: Collection<CalendarDay>) :
    DayViewDecorator {
    private val mContext = context
    var dates: HashSet<CalendarDay> = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(CustomMultipleDotSpanForDark(5f, mContext))
    }
}

class CustomMultipleDotSpan(private val radius: Float, context: Context) : LineBackgroundSpan {
    private val mContext = context

    // dot 의 크기, 위치, 색상
    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        p7: CharSequence,
        start: Int,
        end: Int,
        lineNum: Int
    ) {
        paint.color = getColor(mContext, R.color.calendar_dot_normal)
        canvas.drawCircle(
            ((left + right) / 2).toFloat(),
            bottom + 25f,
            radius,
            paint
        )
    }
}

class CustomMultipleDotSpanForDark(private val radius: Float, context: Context) : LineBackgroundSpan {
    private val mContext = context

    // dot 의 크기, 위치, 색상
    override fun drawBackground(
        canvas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        p7: CharSequence,
        start: Int,
        end: Int,
        lineNum: Int
    ) {
        paint.color = getColor(mContext, R.color.white)
        canvas.drawCircle(
            ((left + right) / 2).toFloat(),
            bottom + 25f,
            radius,
            paint
        )
    }
}