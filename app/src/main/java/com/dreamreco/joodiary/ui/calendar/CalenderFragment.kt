package com.dreamreco.joodiary.ui.calendar

import android.R
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dreamreco.joodiary.databinding.FragmentCalendarBinding
import com.dreamreco.joodiary.util.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.prolificinteractive.materialcalendarview.format.MonthArrayTitleFormatter
import com.prolificinteractive.materialcalendarview.format.TitleFormatter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CalenderFragment : Fragment() {

    private val binding by lazy { FragmentCalendarBinding.inflate(layoutInflater) }
    private val calendarViewModel by viewModels<CalendarViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e("작동","onCreate")


        var startTimeCalendar = Calendar.getInstance()
        var endTimeCalendar = Calendar.getInstance()

        // 현재 시간 설정
        val currentYear = startTimeCalendar.get(Calendar.YEAR)
        val currentMonth = startTimeCalendar.get(Calendar.MONTH)
        val currentDate = startTimeCalendar.get(Calendar.DATE)

        // 마지막 시간 설정
        // 현재 시간 +3 개월
        endTimeCalendar.set(Calendar.MONTH, currentMonth+3)

        binding.calenderView.state().edit()
            // 맨 앞자리 요일 설정
            .setFirstDayOfWeek(Calendar.SUNDAY)
            .setMinimumDate(CalendarDay.from(currentYear-1, currentMonth, 1))
            .setMaximumDate(CalendarDay.from(currentYear, currentMonth+3, endTimeCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)))
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()

        val stCalendarDay = CalendarDay.from(currentYear, currentMonth, currentDate)
        val enCalendarDay = CalendarDay.from(endTimeCalendar.get(Calendar.YEAR), endTimeCalendar.get(Calendar.MONTH), endTimeCalendar.get(Calendar.DATE))

        val sundayDecorator = SundayDecorator()
        val saturdayDecorator = SaturdayDecorator()
        val minMaxDecorator = MinMaxDecorator(stCalendarDay, enCalendarDay)
        val boldDecorator = BoldDecorator(stCalendarDay, enCalendarDay)
        val todayDecorator = TodayDecorator(requireContext())


        binding.calenderView.addDecorators(sundayDecorator, saturdayDecorator, boldDecorator, minMaxDecorator, todayDecorator)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        Log.e("작동","onCreateView")


        return binding.root
    }

}