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
    private val listViewModel by viewModels<ListViewModel>()
    private val binding by lazy { FragmentStatisticsBinding.inflate(layoutInflater) }
    private val mAdapter by lazy { PagerRecyclerAdapter(requireContext(), childFragmentManager) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        with(binding.viewPager) {
            adapter = mAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    Toast.makeText(requireContext(),"페이지 : ${position+1}",Toast.LENGTH_SHORT).show()

                }
            })
        }

        with(listViewModel) {
            getPagerOutData().observe(viewLifecycleOwner){
                makePagerOutDataList(it)
                Log.e("뷰페이져 확인","makePagerOutDataList : $it")
            }

            listFragmentPagerOutData.observe(viewLifecycleOwner){
                mAdapter.submitList(it)
                Log.e("뷰페이져 확인","listFragmentPagerOutData : $it")
            }
        }


        return binding.root
    }
}