package com.dreamreco.joodiary.ui.calendar

import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.FragmentCalendarBinding
import com.dreamreco.joodiary.room.entity.CalendarDate
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.room.entity.MyDate
import com.dreamreco.joodiary.room.entity.MyDrink
import com.dreamreco.joodiary.util.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private val binding by lazy { FragmentCalendarBinding.inflate(layoutInflater) }
    private val calendarViewModel by viewModels<CalendarViewModel>()
    lateinit var recentDate: CalendarDate
    private val mAdapter by lazy { CalenderAdapter(requireContext(), childFragmentManager) }

    // 뒤로 가기 연속 클릭 대기 시간
    var mBackWait: Long = 0

    // 뒤로 가기 처리를 위한 콜백 변수
    private lateinit var callback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 캘린더 기본 설정
        setCalender()

        // 리싸이클러뷰 설정
        setRecyclerView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        with(binding) {
            btnAdd.setOnClickListener {
                listAdd()
            }

            // selectedDate event 처리 코드
            // 날짜 선택 시, date 이용
            calenderView.setOnDateChangedListener { widget, date, selected -> // 선택 시, recentDate 업데이트
                calendarViewModel.changeRecentDate(date)
            }
        }


        // ※ 주의 : CalendarDay month 는 1월이 0 부터 시작함.
        // ex) 2022-06-24 -> 2022-05-24 로 표현됨
        // 캘린더 날짜를 출력하는 코드
        with(calendarViewModel) {
            // 캘린더 선택 날짜 옵저버
            // CalendarDate Room 호출
            getRecentDate().observe(viewLifecycleOwner) { calendarDate ->
                if (calendarDate != null) {

                    // listAdd 를 위한 변수
                    recentDate = calendarDate

                    with(binding) {
                        // recyclerView text
                        textDate.text = getString(
                            R.string.diary_date,
                            calendarDate.date.year,
                            calendarDate.date.month,
                            calendarDate.date.day
                        )
                        // 캘린더 선택 유지
                        calenderView.selectedDate = calendarDate.calendarDate // 날짜
                        calenderView.currentDate = calendarDate.calendarDate // month
                    }

                    // recyclerView 데이터 대입
                    // setOnDateChangedListener 가 아닌, 여기에 위치하는 이유?
                    // : 최초 빌드 시, recyclerView 발동 유무 때문임
                    val myDate = MyDate(
                        calendarDate.date.year,
                        calendarDate.date.month,
                        calendarDate.date.day
                    )

                    getDiaryBaseFlowInDate(myDate).observe(viewLifecycleOwner) {
                        makeList(it)
                    }
                }
            }

            // recyclerView 출력
            calendarFragmentAdapterBaseData.observe(viewLifecycleOwner) {
                mAdapter.submitList(it)
            }
        }

        // calendar 에 dot 를 표시하는 코드
        setDotDecorate()

        // 1. 툴바 관련 코드
        with(binding.calenderToolbar) {
            title = getString(R.string.calendar_fragment_toolbar_title)
//            setOnMenuItemClickListener {
//                when (it.itemId) {
//                    R.id.menu_search -> {
//                        val search = menu.findItem(R.id.menu_search)
//                        val searchView = search?.actionView as? SearchView
//                        searchView?.isSubmitButtonEnabled = true
//                        searchView?.setOnQueryTextListener(this@ListFragment)
//                        true
//                    }
//                    R.id.sort_by_call -> {
//                        showProgress(true)
//                        sortNumber.postValue(SORT_BY_IMPORTANCE)
//                        true
//                    }
//                    R.id.sort_all -> {
//                        showProgress(true)
//                        sortNumber.postValue(SORT_NORMAL_STATE)
//                        true
//                    }
//                    R.id.sort_by_recent -> {
//                        showProgress(true)
//                        sortNumber.postValue(SORT_BY_REGISTERED)
//                        true
//                    }
//                    R.id.delete_all -> {
//                        deleteDataAll()
//                        true
//                    }
//                    R.id.delete_part -> {
//                        deletePart()
//                        true
//                    }
//                    else -> false
//                }
//            }
        }

        // 종료 백버튼 콜백
        setBackButtonToFinishTheApp()

        return binding.root
    }

    private fun setDotDecorate() {
        with(calendarViewModel) {
            dotForCalendar.observe(viewLifecycleOwner){ list ->
                basicDecoratorReset()
                with(binding.calenderView) {
                    addDecorator(EventDecorator(requireContext(), list.notImportantList))
                    addDecorator(EventDecoratorForImportantData(requireContext(), list.importantList))
                }
            }

//            //  일반 기록 날짜에 dot 표시를 하는 코드
//            getNotImportantCalendarDayForDecorator().observe(viewLifecycleOwner){ listOfCalendarDay ->
//                if (listOfCalendarDay != emptyList<CalendarDay>()) {
//                    binding.calenderView.addDecorator(EventDecorator(requireContext(), listOfCalendarDay))
//                }
//            }
//
//            // 중요한 기록 날짜에 dot 표시를 하는 코드
//            getImportantCalendarDayForDecorator().observe(viewLifecycleOwner){ listOfImportantCalendarDay ->
//                if (listOfImportantCalendarDay != emptyList<CalendarDay>()) {
//                    binding.calenderView.addDecorator(EventDecoratorForImportantData(requireContext(), listOfImportantCalendarDay))
//                }
//            }
        }
    }

    private fun basicDecoratorReset() {
        with(binding.calenderView) {
            // 데코 전체 삭제
            removeDecorators()

            // 데코 다시 설정
            val sundayDecorator = SundayDecorator()
            val saturdayDecorator = SaturdayDecorator()
            val todayDecorator = TodayDecorator(requireContext())
            addDecorators(
                sundayDecorator,
                saturdayDecorator,
                todayDecorator
            )
        }
    }


    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간
    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간
    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간

    // 리스트 신규 추가 코드
    private fun listAdd() {
        val date = MyDate(recentDate.date.year, recentDate.date.month, recentDate.date.day)
        val item = DiaryBase(null, date, recentDate.calendarDate, "", null, null, false,0,null)
        val action = CalendarFragmentDirections.actionCalenderFragmentToDiaryDetailDialog(
            item
        )

        findNavController().navigate(action)

//        val bundle = bundleOf()
//        bundle.putParcelable("DiaryBase", item)
//        val dialog = DiaryDetailDialog()
//        dialog.arguments = bundle
//        dialog.show(childFragmentManager, "DiaryDetailDialog")
    }

    // 캘린더 기본 설정 코드
    private fun setCalender() {
        val startTimeCalendar = Calendar.getInstance()
        val endTimeCalendar = Calendar.getInstance()

        // 현재 시간 설정
        val currentYear = startTimeCalendar.get(Calendar.YEAR)
        val currentMonth = startTimeCalendar.get(Calendar.MONTH)
        val currentDate = startTimeCalendar.get(Calendar.DATE)

        // 마지막 시간 설정
        // 현재 시간 +3 개월
        endTimeCalendar.set(Calendar.MONTH, currentMonth + 3)

        binding.calenderView.state().edit()
            // 맨 앞자리 요일 설정
            .setFirstDayOfWeek(Calendar.SUNDAY)
            // 최소 표시 일자 (현재시간 -3년)
            .setMinimumDate(CalendarDay.from(currentYear - 1, currentMonth, 1))
            // 최대 표시 일자 (현재시간 +3개월)
            .setMaximumDate(
                CalendarDay.from(
                    currentYear,
                    currentMonth + 3,
                    endTimeCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
            )
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()

//        val stCalendarDay = CalendarDay.from(currentYear-1, currentMonth, currentDate)
//        val enCalendarDay = CalendarDay.from(
//            endTimeCalendar.get(Calendar.YEAR),
//            endTimeCalendar.get(Calendar.MONTH),
//            endTimeCalendar.get(Calendar.DATE)
//        )

        // 꾸밈 표시 설정
        val sundayDecorator = SundayDecorator()
        val saturdayDecorator = SaturdayDecorator()
//        val minMaxDecorator = MinMaxDecorator(stCalendarDay, enCalendarDay)
//        val boldDecorator = BoldDecorator(stCalendarDay, enCalendarDay)
        val todayDecorator = TodayDecorator(requireContext())

        binding.calenderView.addDecorators(
            sundayDecorator,
            saturdayDecorator,
//            boldDecorator,
//            minMaxDecorator,
            todayDecorator
        )
    }

    private fun setRecyclerView() {
        with(binding) {
            with(calenderRecyclerView) {
                adapter = mAdapter
                setHasFixedSize(true)
                setItemViewCacheSize(5)
            }

            // recyclerView 갱신 시, 깜빡임 방지
            val animator = calenderRecyclerView.itemAnimator
            if (animator is SimpleItemAnimator) {
                animator.supportsChangeAnimations = false
            }
        }
    }

    private fun setBackButtonToFinishTheApp() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //뒤로 가기 시 특정 코드 작동
                if (System.currentTimeMillis() - mBackWait >= 2000) {
                    mBackWait = System.currentTimeMillis()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.exit_callback_toast),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    //액티비티 종료
                    requireActivity().finishAndRemoveTask()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onResume() {
        super.onResume()
        // dot 를 계속 갱신하는 코드
        // 두가지 리스트를 한 옵저버에 처리해야 하므로, Room 에서 Live 로 바로 가지고 오지 못함
        calendarViewModel.getNotImportantCalendarDayForDecoratorBySuspend()
    }
}