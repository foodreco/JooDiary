package com.dreamreco.joodiary.ui.statistics

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.FragmentStatisticsBinding
import com.dreamreco.joodiary.util.ALCOHOL_PER_SOJU
import com.dreamreco.joodiary.util.CUSTOM_CHART_COLORS
import com.dreamreco.joodiary.util.toMonthString
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private val statisticsViewModel by viewModels<StatisticsViewModel>()
    private val binding by lazy { FragmentStatisticsBinding.inflate(layoutInflater) }
    private var tendencyLayoutState = true

    // 최근 경향 비교를 위한 변수
    private var averageFrequency = 0
    private var averageVOD = 0
    private var averagePAOD = 0
    private var averageFrequencyAll = 0
    private var averageVODAll = 0
    private var averagePAODAll = 0

//    그래프 no data Gone 처리하기

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 기본 성향 로딩
        showDrinkTendencyProgressbar(true)

        // 주종 파이 차트 로딩
        showDrinkTypePieChartProgressbar(true)

        // 컴바인 차트 로딩
        showCombinedChartLayoutProgress(true)

        // 성향 기준 데이터를 가져와서 출력하는 함수
        setDrinkTendency()

        // 주종 파이 차트 기준 데이터를 가져오는 함수
        getPieChartBaseData()

        // 주종 파이 차트 유지 코드
        setDrinkTypePieChart()

        // 컴바인 차트 유지 코드
        setVODCombinedChart()

        // 가로막대바 유지 코드
        setHorizontalProgressbar()

        // 1. 툴바 관련 코드
        with(binding.statisticsToolbar) {
            title = getString(R.string.statistics_fragment_toolbar_title)
        }

        return binding.root
    }

    // 성향을 가져오고 출력하는 코드
    private fun setDrinkTendency() {
        with(statisticsViewModel) {
            showDrinkTendencyProgressbar(true)
            // 전체 기록
            getTendencyBaseData()
            // 최근 3개월 기록
            getTendencyBaseDataRecent3Months()

            with(binding) {
                drinkRecent3MonthsTendencyResult.observe(viewLifecycleOwner) { drinkTendency ->
                    if (drinkTendency == null) {
                        Toast.makeText(requireContext(), "경향 Null", Toast.LENGTH_SHORT).show()
                        summaryText.text = getString(R.string.tendency_null_state_summary_text)

                        textAverageFrequency.text = getString(R.string.tendency_null_state)
                        averageFrequency = 0

                        textAverageVOD.text = getString(R.string.tendency_null_state)
                        averageVOD = 0

                        textAveragePAOD.text = getString(R.string.tendency_null_state)
                        averagePAOD = 0

                        showDrinkTendencyProgressbar(false)
                    } else {
                        textAverageFrequency.text = getString(R.string.textAverageFrequency, drinkTendency.drinkFrequencyDayNumber)
                        averageFrequency = drinkTendency.drinkFrequencyDayNumber

                        textAverageVOD.text = getString(R.string.textAverageVOD, drinkTendency.averageVOD)
                        val convertToSoju = ((drinkTendency.averagePAOD.toFloat()/ALCOHOL_PER_SOJU.toFloat()*10).roundToInt().toFloat()/10).toString()
                        averageVOD = drinkTendency.averageVOD

                        textAveragePAOD.text = getString(R.string.textMaxVODPerMonth, drinkTendency.averagePAOD, convertToSoju)
                        averagePAOD = drinkTendency.averagePAOD

                        summaryText.text = getString(R.string.drink_tendency_summary, drinkTendency.drinkTendency)
                        showDrinkTendencyProgressbar(false)
                    }
                }
                drinkTendencyResult.observe(viewLifecycleOwner) { allDurationDrinkTendency ->
                    if (allDurationDrinkTendency == null) {
                        Toast.makeText(requireContext(), "경향 Null", Toast.LENGTH_SHORT).show()
                        summaryText2.text = getString(R.string.tendency_null_state_summary_text)

                        textAverageFrequency2.text = getString(R.string.tendency_null_state)
                        averageFrequencyAll = 0

                        textAverageVOD2.text = getString(R.string.tendency_null_state)
                        averageVODAll = 0

                        textAveragePAOD2.text = getString(R.string.tendency_null_state)
                        averagePAOD = 0

                        showDrinkTendencyProgressbar(false)
                    } else {
                        textAverageFrequency2.text = getString(R.string.textAverageFrequency, allDurationDrinkTendency.drinkFrequencyDayNumber)
                        averageFrequencyAll = allDurationDrinkTendency.drinkFrequencyDayNumber

                        textAverageVOD2.text = getString(R.string.textAverageVOD, allDurationDrinkTendency.averageVOD)
                        averageVODAll = allDurationDrinkTendency.averageVOD
                        val convertToSoju = ((allDurationDrinkTendency.averagePAOD.toFloat()/ALCOHOL_PER_SOJU.toFloat()*10).roundToInt().toFloat()/10).toString()

                        textAveragePAOD2.text = getString(R.string.textMaxVODPerMonth, allDurationDrinkTendency.averagePAOD, convertToSoju)
                        averagePAODAll = allDurationDrinkTendency.averagePAOD

                        summaryText2.text = getString(R.string.drink_tendency_summary, allDurationDrinkTendency.drinkTendency)
                        showDrinkTendencyProgressbar(false)
                    }
                }
            }
        }

        // 전체 기간 성향을 표시하고, 지수 상하향 마크를 나타내는 코드
        with(binding) {
            tendencyViewAll.setOnClickListener {
                if (tendencyLayoutState) {
                    val image = ContextCompat.getDrawable(requireContext(),R.drawable.ic_arrow_up)
                    tendencyViewAll.setCompoundDrawablesWithIntrinsicBounds(null,null, image,null)
                    tendencyViewAllLayout.visibility = View.VISIBLE
                    tendencyLayoutState = !tendencyLayoutState

                    upDownFrequency.visibility = View.VISIBLE
                    upDownVOD.visibility = View.VISIBLE
                    upDownPAOD.visibility = View.VISIBLE

                    if (averageFrequency > averageFrequencyAll) {
                        upDownFrequency.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_upward))
                        upDownFrequency.setColorFilter(ContextCompat.getColor(requireContext(),R.color.red))
                    } else if (averageFrequency < averageFrequencyAll) {
                        upDownFrequency.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_downward))
                        upDownFrequency.setColorFilter(ContextCompat.getColor(requireContext(),R.color.blue))
                    } else {
                        upDownFrequency.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_hypen))
                        upDownFrequency.setColorFilter(ContextCompat.getColor(requireContext(),R.color.black))
                    }

                    if (averageVOD > averageVODAll) {
                        upDownVOD.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_upward))
                        upDownVOD.setColorFilter(ContextCompat.getColor(requireContext(),R.color.red))
                    } else if (averageVOD < averageVODAll) {
                        upDownVOD.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_downward))
                        upDownVOD.setColorFilter(ContextCompat.getColor(requireContext(),R.color.blue))
                    } else {
                        upDownVOD.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_hypen))
                        upDownVOD.setColorFilter(ContextCompat.getColor(requireContext(),R.color.black))
                    }

                    if (averagePAOD > averagePAODAll) {
                        upDownPAOD.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_upward))
                        upDownPAOD.setColorFilter(ContextCompat.getColor(requireContext(),R.color.red))
                    } else if (averagePAOD < averagePAODAll) {
                        upDownPAOD.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_downward))
                        upDownPAOD.setColorFilter(ContextCompat.getColor(requireContext(),R.color.blue))
                    } else {
                        upDownPAOD.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_hypen))
                        upDownPAOD.setColorFilter(ContextCompat.getColor(requireContext(),R.color.black))
                    }

                } else {
                    val image = ContextCompat.getDrawable(requireContext(),R.drawable.ic_arrow_down)
                    tendencyViewAll.setCompoundDrawablesWithIntrinsicBounds(null,null, image,null)
                    tendencyViewAllLayout.visibility = View.GONE
                    tendencyLayoutState = !tendencyLayoutState

                    upDownFrequency.visibility = View.GONE
                    upDownVOD.visibility = View.GONE
                    upDownPAOD.visibility = View.GONE
                }
            }
        }
    }

    // 가로막대바 유지 코드
    private fun setHorizontalProgressbar() {
        with(statisticsViewModel) {
            horizontalProgressbarData.observe(viewLifecycleOwner) { data ->
                with(binding) {
                    if (data == null) {
                        // null 일 경우, 그래프 숨김 작동
                        progressHorizontalLayout.visibility = View.GONE
                    } else {
                        progressHorizontalLayout.visibility = View.VISIBLE
                        activateHorizontalProgressbar(data)
                    }
                }
            }
        }
    }

    // 주종 파이 차트 기준 데이터를 가져오는 함수
    private fun getPieChartBaseData() {
        with(statisticsViewModel) {
            getMyDrinkListFlow().observe(viewLifecycleOwner) {
                makePieChartList(it)
            }
        }
    }

    // 주종 파이 차트 유지 코드
    private fun setDrinkTypePieChart() {
        with(statisticsViewModel) {
            // PieChart 데이터 출력 코드
            drinkTypeChartData.observe(viewLifecycleOwner) {
                // null 이 아니면 empty list 도 아님
                if (it == null) {
                    with(binding.drinkTypePieChart) {
                        setNoDataText("관련 기록이 없습니다.")
                        setNoDataTextTypeface(Typeface.DEFAULT_BOLD)
                        setNoDataTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.pieChart_no_data_text_color
                            )
                        )
                    }
                }
                activateDrinkTypePieChart(it)
                showDrinkTypePieChartProgressbar(false)
            }
        }
    }

    // 많은 수대로 정렬해서 list 넘겨야 함
    private fun activateDrinkTypePieChart(drinkTypePieChartList: List<DrinkTypePieChartList>) {
        // MP 차트 관련 코드
        with(binding.drinkTypePieChart) {
            // 데이터 없을 때, 텍스트 생략
            setNoDataText("")
            // 퍼센트값 적용안함 -> 직접 퍼센트값 넣어주기
            setUsePercentValues(false)
            description.isEnabled = false
            isRotationEnabled = false
//            setExtraOffsets(5f, 10f, 5f, 5f)
            setExtraOffsets(0f, 0f, 0f, 0f)

            // 그래프 아이템 표현여부
            setDrawEntryLabels(false)
            // 그래프 아이템 이름색상
            setEntryLabelColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            // 그래프 아이템 이름 크기, 글자유형
            setEntryLabelTextSize(12f)
            setEntryLabelTypeface(Typeface.DEFAULT_BOLD)

            // 가운데 구멍 생성 여부
            isDrawHoleEnabled = true
            holeRadius = 55f
            // 가운데 구멍 색상
            setHoleColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            // 중앙 투명 써클
            setTransparentCircleColor(ContextCompat.getColor(
                requireContext(),
                R.color.white
            ))
            // 투명 써클 투명도 0-255
            setTransparentCircleAlpha(100)
            transparentCircleRadius = 60f

            // 터치 시 강조 작동
            isHighlightPerTapEnabled = true

            // 차트 범례 표현 여부
            legend.isEnabled = false
//            val l = legend
//            l.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
//            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
//            l.orientation = Legend.LegendOrientation.VERTICAL
//            l.setDrawInside(false)
//            l.textSize = 16f
//            l.xEntrySpace = 8f
//            l.yEntrySpace = 0f
//            l.yOffset = 0f

            // 중앙 텍스트 및 크기, 색상, 글자유형
            centerText = "비율\n(%)"
            setCenterTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pieChart_center_text
                )
            )
            setCenterTextSize(25f)
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)

            // 그래프 애니메이션 (https://superkts.com/jquery/@easingEffects)
            // ★ 이게 있어야 실시간으로 업데이트 됨
            animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInCubic)

            // data set - 데이터 넣어주기
            val entries = ArrayList<PieEntry>()
            if (drinkTypePieChartList == emptyList<DrinkTypePieChartList>()) {
                entries.add(PieEntry(100f, getString(R.string.get_empty_DrinkTypePieChartList)))
            } else {
                // 전체 합계를 미리 계산
                val totalTimes = drinkTypePieChartList.sumOf{it.drinkTimes.toInt()}
                // % 값을 넣어줌
                for (list in drinkTypePieChartList) {
                    val addValue = (((list.drinkTimes.toFloat().div(totalTimes.toFloat()))*1000).roundToInt()).toFloat()/10
                    entries.add(PieEntry(addValue, list.drinkType))
                }
            }

            // 데이터 관련 옵션 정하기
            val dataSet = PieDataSet(entries, "")
            with(dataSet) {
                // 그래프 사이 간격
                sliceSpace = 2f
                // 색깔구분 잘가게 해주자
                // 갯수 많아지니깐 헷갈림
                colors = CUSTOM_CHART_COLORS
            }

            // 차트 제목 텍스트?
            val pieData = PieData(dataSet)
            with(pieData) {
                // value 표현 여부
                setDrawValues(false)
                // 그래프 value(수치) 크기
//                setValueTextSize(12f)
//                // value 색상
//                setValueTextColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.white
//                    )
//                )
            }

            data = pieData

            // 파이차트 터치 시 콜백 코드
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e != null) {
                        if (h != null) {
                            val targetDrinkType = entries[h.x.toInt()].label
                            val targetPercent = entries[h.x.toInt()].value.toString()
                            if (targetDrinkType != getString(R.string.get_empty_DrinkTypePieChartList)) {
                                statisticsViewModel.getProgressBarBaseData(targetDrinkType)
                                centerText = "$targetPercent%\n$targetDrinkType"
                            }
                        }
                    }
                }

                override fun onNothingSelected() {
                    val emptyTarget = ""
                    statisticsViewModel.getProgressBarBaseData(emptyTarget)
                    centerText = getString(R.string.pie_chart_center_text_empty)
                }
            })

            // 첫번째 인자 highlight 처리
            val high = Highlight(0f,0,0)
            high.dataIndex = 0
            highlightValue(high)
            val firstHighLightValue = entries[0].label
            val firstPercent = entries[0].value.toString()
            statisticsViewModel.getProgressBarBaseData(firstHighLightValue)
            centerText = "$firstPercent%\n$firstHighLightValue"

        }

        showDrinkTypePieChartProgressbar(false)
    }

    // 컴바인 차트 유지 코드
    private fun setVODCombinedChart() {
        with(statisticsViewModel) {
            getBaseDataForCombinedChartData().observe(viewLifecycleOwner) {
                convertToCombinedChartData(it)
            }
            combinedChartData.observe(viewLifecycleOwner) { it ->
                activateVODCombinedChart(it)
            }
        }
    }

    // CombinedChart 세팅 코드
    // CombinedChartData 를 받아 CombinedChart 를 표시하는 코드
    private fun activateVODCombinedChart(applyList: List<CombinedChartData>) {
        if (applyList != emptyList<CombinedChartData>()) {
            val chart = binding.combinedChart
            chart.description.isEnabled = false
            chart.setBackgroundColor(Color.WHITE)
            chart.setDrawGridBackground(false)
            chart.setDrawBarShadow(false)
            chart.isHighlightFullBarEnabled = false

            // 그림 덮어쓰는 순서에 영향 미침
            // draw bars behind lines
            chart.drawOrder = arrayOf(
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
            )

            val l = chart.legend
            l.isWordWrapEnabled = true
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.setDrawInside(false)


            val rightAxis = chart.axisRight
            rightAxis.setDrawGridLines(false)
            rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)


            val leftAxis = chart.axisLeft
            leftAxis.setDrawGridLines(false)
            leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

            // X 축에 표시할 변수를 생성하는 코드
            val months = mutableListOf<String>()
            for (xAxisMonth in applyList) {
                val monthToString =
                    xAxisMonth.month.toMonthString()
                months.add(monthToString)
            }

            // 만약 month 가 empty 라면 차트 자체를 숨기거나 변경할 것 (visibility 사용해서)

            val xAxis = chart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.axisMinimum = 0f
            xAxis.granularity = 1f // 그래프 확대 시 x 축 최소 간격, data 의 BarEntry X 값 간격과 일치해야 함!
            xAxis.setDrawLabels(true) // x축 라벨표시여부
            xAxis.valueFormatter = IndexAxisValueFormatter(months)

            val data = CombinedData()

            data.setData(generateBarData(applyList))
            data.setData(generateLineData(applyList))
            data.setValueTypeface(Typeface.DEFAULT_BOLD)

            xAxis.axisMaximum = data.xMax + 0.5f // x 축 뒤쪽 이격 거리
            xAxis.axisMinimum = data.xMin - 0.5f // x 축 앞쪽 이격 거리

            chart.data = data
            showCombinedChartLayoutProgress(false)

            with(binding) {

                val mostPAODMonth = applyList.sortedByDescending { it.PAOD }[0]
                val mostTimesMonth = applyList.sortedByDescending { it.drinkTimes }[0]

                textComReview.visibility = View.VISIBLE
                textComReviewSub.visibility = View.VISIBLE
                textComReview2.visibility = View.VISIBLE
                textComReview2Sub.visibility = View.VISIBLE

                // 요약 문구 로직 설계하기
                textComReview.text = getString(R.string.activateVODCombinedChart_text1, mostTimesMonth.month.year, mostTimesMonth.month.month)
                textComReviewSub.text = getString(R.string.activateVODCombinedChart_text1_sub, mostTimesMonth.drinkTimes.toInt())
                textComReview2.text = getString(R.string.activateVODCombinedChart_text2, mostPAODMonth.month.year,mostPAODMonth.month.month)
                val convertToSoju = (((mostPAODMonth.PAOD / ALCOHOL_PER_SOJU.toFloat())*10).roundToInt().toFloat()/10).toString()
                textComReview2Sub.text = getString(R.string.activateVODCombinedChart_text2_sub, mostPAODMonth.PAOD.toInt(), convertToSoju)
            }
        } else {
            // 데이터가 없는 경우,
            with(binding) {
                with(combinedChart) {
                    setNoDataText("관련 기록이 없습니다.")
                    setNoDataTextTypeface(Typeface.DEFAULT_BOLD)
                    setNoDataTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.pieChart_no_data_text_color
                        )
                    )

                    textComReview.visibility = View.GONE
                    textComReviewSub.visibility = View.GONE
                    textComReview2.visibility = View.GONE
                    textComReview2Sub.visibility = View.GONE

                    showCombinedChartLayoutProgress(false)
                }
            }


        }
    }

    // Combined Chart 중 바 차트
    private fun generateBarData(applyList: List<CombinedChartData>): BarData? {
        val entries1: ArrayList<BarEntry> = ArrayList()
        for (index in 0 until applyList.lastIndex + 1) {
            entries1.add(BarEntry(index + 0f, applyList[index].PAOD))
        }
        val set1 = BarDataSet(entries1, getString(R.string.generateBarData))
        set1.color = Color.rgb(60, 220, 78)
        set1.valueTextColor = Color.rgb(60, 220, 78)
        set1.valueTextSize = 10f
        set1.axisDependency = YAxis.AxisDependency.LEFT

        val barWidth = 0.5f // x2 dataset

        val d = BarData(set1)
        d.barWidth = barWidth

        return d
    }

    // Combined Chart 중 라인 차트
    private fun generateLineData(applyList: List<CombinedChartData>): LineData? {
        val d = LineData()
        val entries: ArrayList<Entry> = arrayListOf()
        for (index in 0 until applyList.lastIndex + 1) {
            entries.add(Entry(index + 0f, applyList[index].drinkTimes))
        }
        val set = LineDataSet(entries, getString(R.string.generateLineData))
        set.color = Color.rgb(74, 101, 114)
        set.lineWidth = 2.5f
        set.setCircleColor(Color.rgb(74, 101, 114))
        set.circleRadius = 5f
//        set.fillColor = Color.rgb(240, 238, 70)
        set.fillColor = Color.rgb(74, 101, 114)
        set.mode = LineDataSet.Mode.LINEAR
        set.setDrawValues(true)
        set.valueTextSize = 10f
        set.valueTextColor = Color.rgb(74, 101, 114)
        set.axisDependency = YAxis.AxisDependency.RIGHT
        d.addDataSet(set)
        return d
    }


    // 가로 막대 그래프
    private fun activateHorizontalProgressbar(drinkTypePieChartList: DrinkHorizontalProgressbarList) {

        val allDrinkTimes = drinkTypePieChartList.lowDrinkTimes.roundToInt()
        val targetDrinkTimes = drinkTypePieChartList.highDrinkTimes.roundToInt()
        val allDrinkVOD = drinkTypePieChartList.lowDrinkVOD.roundToInt()
        val targetDrinkVOD = drinkTypePieChartList.highDrinkVOD.roundToInt()
        val allDrinkPVOA = drinkTypePieChartList.lowDrinkPVOA.roundToInt()
        val targetDrinkPVOA = drinkTypePieChartList.highDrinkPVOA.roundToInt()

        with(binding) {
            textTotalIn.text = targetDrinkTimes.toString()
            textTotalOut.text = allDrinkTimes.toString()
            textGroupIn.text = targetDrinkVOD.toString()
            textGroupOut.text = allDrinkVOD.toString()
            textRecoIn.text = targetDrinkPVOA.toString()
            textRecoOut.text = allDrinkPVOA.toString()

            progressbarTargetTextView.text = drinkTypePieChartList.targetDrinkType

            // 발수신 프로그래스바 수치 대입 코드
            rcProgressbarTotal.max =
                drinkTypePieChartList.lowDrinkTimes
            rcProgressbarTotal.progress = drinkTypePieChartList.highDrinkTimes
            rcProgressbarGroup.max =
                drinkTypePieChartList.lowDrinkVOD
            rcProgressbarGroup.progress = drinkTypePieChartList.highDrinkVOD
            rcProgressbarReco.max =
                drinkTypePieChartList.lowDrinkPVOA
            rcProgressbarReco.progress = drinkTypePieChartList.highDrinkPVOA
        }
    }


    // 통합 로딩 다시 정리하기 or 파이차트랑 막대차트 따로 로딩 두거나...
    private fun showDrinkTypePieChartProgressbar(show: Boolean) {
        binding.progressbarForPieChart.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showCombinedChartLayoutProgress(show: Boolean) {
        binding.progressbarForCombinedChartLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showDrinkTendencyProgressbar(show: Boolean) {
        binding.progressBarTendency.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        // 성향을 불러오는 데이터(옵저버 형태가 아니므로, resume 으로 관리)
        with(statisticsViewModel) {
            getTendencyBaseData()
            getTendencyBaseDataRecent3Months()
        }
    }
}


