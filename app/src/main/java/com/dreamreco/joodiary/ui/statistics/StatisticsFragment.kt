package com.dreamreco.joodiary.ui.statistics

import android.graphics.Color.green
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.FragmentListBinding
import com.dreamreco.joodiary.databinding.FragmentStatisticsBinding
import com.dreamreco.joodiary.ui.list.ListFragmentAdapter
import com.dreamreco.joodiary.ui.list.ListViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private val statisticsViewModel by viewModels<StatisticsViewModel>()
    private val binding by lazy { FragmentStatisticsBinding.inflate(layoutInflater) }

//        1.월별 주별 연별 주량 통계내기
//        순수 알콜 섭취량 기준
//                막대 그래프?
//        주류 구성 원그래프

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return binding.root
    }
}