package com.dreamreco.joodiary.ui.statistics

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.FragmentStatisticsBinding
import com.dreamreco.joodiary.util.CUSTOM_CHART_COLORS
import com.dreamreco.joodiary.util.toMonthString
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs
import kotlin.math.round


@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private val statisticsViewModel by viewModels<StatisticsViewModel>()
    private val binding by lazy { FragmentStatisticsBinding.inflate(layoutInflater) }

    //    음주 유형 4~8종?? 중간 결과 합산해서 로직으로 도출하기..

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 주종 파이 차트 로딩
        showDrinkTypePieChartProgressbar(true)

        // 컴바인 차트 로딩
        showCombinedChartLayoutProgress(true)

        // 주종 파이 차트와 저고도주 가로막대바 기준 데이터를 가져오는 함수
        getPieChartAndHorizontalProgressbarBaseData()

        // 주종 파이 차트 유지 코드
        setDrinkTypePieChart()

        // 컴바인 차트 유지 코드
        setVODCombinedChart()

        // 가로막대바 유지 코드
        setHorizontalProgressbar()

        return binding.root
    }

    // 가로막대바 유지 코드
    private fun setHorizontalProgressbar() {
        with(statisticsViewModel) {
            horizontalProgressbarData.observe(viewLifecycleOwner){ data ->
                if (data == null) {
                    // null 일 경우, 그래프 숨김 작동
                    Toast.makeText(requireContext(),"가로막대바 데이터 없음", Toast.LENGTH_SHORT).show()
                } else {
                    activateHorizontalProgressbar(data)
                }
            }
        }
    }

    // 주종 파이 차트와 저고도주 가로막대바 기준 데이터를 가져오는 함수
    private fun getPieChartAndHorizontalProgressbarBaseData() {
        with(statisticsViewModel) {
            getMyDrinkListFlow().observe(viewLifecycleOwner) {
                makePieChartList(it)
                makeHorizontalProgressbarList(it)
            }
        }
    }

    // 주종 파이 차트 유지 코드
    private fun setDrinkTypePieChart() {
        with(statisticsViewModel) {
            // PieChart 데이터 출력 코드
            drinkTypeChartData.observe(viewLifecycleOwner) {
                // null 이 아니면 empty list 도 아님
                if (it != emptyList<DrinkTypePieChartList>()) {
                    activateDrinkTypePieChart(it)
                } else {
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
                    showDrinkTypePieChartProgressbar(false)
                }
            }
        }
    }

    // 많은 수대로 정렬해서 list 넘겨야 함
    private fun activateDrinkTypePieChart(drinkTypePieChartList: List<DrinkTypePieChartList>) {
        // MP 차트 관련 코드
        with(binding.drinkTypePieChart) {
            // 데이터 없을 때, 텍스트 생략
            setNoDataText("")
            // 퍼센트값 적용
            setUsePercentValues(true)
            description.isEnabled = false
            isRotationEnabled = false
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
            // 가운데 구멍 색상
            setHoleColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            // 가운데 불투명써클 크기
            transparentCircleRadius = 0f

            // 차트 범례 표현 여부
            legend.isEnabled = true
            val l = legend
            l.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            l.orientation = Legend.LegendOrientation.VERTICAL
            l.setDrawInside(false)
            l.textSize = 16f
            l.xEntrySpace = 8f
            l.yEntrySpace = 0f
            l.yOffset = 0f


            // 중앙 텍스트 및 크기, 색상, 글자유형
            centerText = "비율\n(%)"
            setCenterTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pieChart_center_text
                )
            )

            setCenterTextSize(16f)
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)

            // 그래프 애니메이션 (https://superkts.com/jquery/@easingEffects)
            // ★ 이게 있어야 실시간으로 업데이트 됨
            animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInCubic)

            // data set - 데이터 넣어주기
            // 인수가 많은 집합부터 불러오기(사람수 많은 그룹부터 불러오기)
            // 데이터 넣기, 조건부로 null 일 때 넣을 리스트 따로 만들기
            val entries = ArrayList<PieEntry>()
            if (drinkTypePieChartList == emptyList<DrinkTypePieChartList>()) {
                entries.add(PieEntry(100f, "그룹없음"))
            } else {
                for (list in drinkTypePieChartList) {
                    entries.add(PieEntry(list.drinkTimes, list.drinkType))
                }
            }

            // 데이터 관련 옵션 정하기
            val dataSet = PieDataSet(entries, "")
            with(dataSet) {
                // 그래프 사이 간격
                sliceSpace = 1f
                colors = CUSTOM_CHART_COLORS
            }

            // 차트 제목 텍스트?
            val pieData = PieData(dataSet)
            with(pieData) {
                // 그래프 value(수치) 크기
                setValueTextSize(12f)
                // value 색상
                setValueTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
            data = pieData
        }

        showDrinkTypePieChartProgressbar(false)
    }

    // 컴바인 차트 유지 코드
    private fun setVODCombinedChart() {
        with(statisticsViewModel) {
            getBaseDataForCombinedChartData().observe(viewLifecycleOwner) {
                convertToCombinedChartData(it)
            }

            combinedChartData.observe(viewLifecycleOwner) {
                if (it != emptyList<CombinedChartData>()) {
                    activateVODCombinedChart(it)
                } else {
                    with(binding.combinedChart) {
                        setNoDataText("관련 기록이 없습니다.")
                        setNoDataTextTypeface(Typeface.DEFAULT_BOLD)
                        setNoDataTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.pieChart_no_data_text_color
                            )
                        )
                        showCombinedChartLayoutProgress(false)
                    }
                }
            }
        }
    }

    // CombinedChart 세팅 코드
    // CombinedChartData 를 받아 CombinedChart 를 표시하는 코드
    private fun activateVODCombinedChart(applyList: List<CombinedChartData>) {
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
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(months)

        val data = CombinedData()

        data.setData(generateBarData(applyList))
        data.setData(generateLineData(applyList))
        data.setValueTypeface(Typeface.DEFAULT_BOLD)

        xAxis.axisMaximum = data.xMax + 0.5f
        xAxis.axisMinimum = data.xMin - 0.5f

        chart.data = data
        showCombinedChartLayoutProgress(false)

        with(binding) {
            // 요약 문구 로직 설계하기
            var reviewText1 = "ㆍ주량 경향"
            var reviewText2 = "ㆍ알콜 섭취량 경항"
            var reviewText3 = "ㆍ빈도 경향"

            textComReview.text = reviewText1
            textComReview2.text = reviewText2
            textComReview3.text = reviewText3
        }

    }

    // Combined Chart 중 바 차트
    private fun generateBarData(applyList: List<CombinedChartData>): BarData? {
        val entries1: ArrayList<BarEntry> = ArrayList()
        for (index in 1 until applyList.lastIndex + 2) {
            entries1.add(BarEntry(index + 0f, applyList[index - 1].VOD))
        }
        val set1 = BarDataSet(entries1, "알콜섭취량(ml)")
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
        for (index in 1 until applyList.lastIndex + 2) {
            entries.add(Entry(index + 0f, applyList[index - 1].drinkTimes))
        }
        val set = LineDataSet(entries, "음주횟수")
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


    // 가록 막대 그래프
    // 총량 횟수 알콜 항목을 저도주와 고도주(20도 이상)로 나누는 그래프
    private fun activateHorizontalProgressbar(drinkTypePieChartList: DrinkHorizontalProgressbarList) {

        with(binding) {
            textTotalIn.text = drinkTypePieChartList.lowDrinkTimes.toString()
            textTotalOut.text = drinkTypePieChartList.highDrinkTimes.toString()
            textGroupIn.text = drinkTypePieChartList.lowDrinkVOD.toString()
            textGroupOut.text = drinkTypePieChartList.highDrinkVOD.toString()
            textRecoIn.text = drinkTypePieChartList.lowDrinkPVOA.toString()
            textRecoOut.text = drinkTypePieChartList.highDrinkPVOA.toString()


            // 문구 로직 설계하기
            textCallTypeReview.text = "로직설계 : 저도주 횟수가 많아요."
            textCallTypeReview2.text = "로직설계 : 저도주를 3배 이상 마셔요."
            textCallTypeReview3.text = "로직설계 : 순수 섭취 알콜양도 저도주가 많아요."
            textCallTypeReview4.text = "로직설계 : 저도주로 대부분 알콜을 섭취해요."

            // 발수신 프로그래스바 수치 대입 코드
            rcProgressbarTotal.max =
                drinkTypePieChartList.lowDrinkTimes + drinkTypePieChartList.highDrinkTimes
            rcProgressbarTotal.progress = drinkTypePieChartList.lowDrinkTimes
            rcProgressbarGroup.max =
                drinkTypePieChartList.lowDrinkVOD + drinkTypePieChartList.highDrinkVOD
            rcProgressbarGroup.progress = drinkTypePieChartList.lowDrinkVOD
            rcProgressbarReco.max =
                drinkTypePieChartList.lowDrinkPVOA + drinkTypePieChartList.highDrinkPVOA
            rcProgressbarReco.progress = drinkTypePieChartList.lowDrinkPVOA
        }
    }


    // 통합 로딩 다시 정리하기 or 파이차트랑 막대차트 따로 로딩 두거나...
    private fun showDrinkTypePieChartProgressbar(show: Boolean) {
        binding.progressbarForPieChart.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showCombinedChartLayoutProgress(show: Boolean) {
        binding.progressbarForCombinedChartLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

}
