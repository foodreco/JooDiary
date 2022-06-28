package com.dreamreco.joodiary.ui.list

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.FragmentListBinding
import com.dreamreco.joodiary.util.SORT_IMPORTANCE
import com.dreamreco.joodiary.util.SORT_NORMAL
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : Fragment() {

    private val listViewModel by viewModels<ListViewModel>()
    private val binding by lazy { FragmentListBinding.inflate(layoutInflater) }
    private val mAdapter by lazy { ListFragmentAdapter(requireContext(), childFragmentManager)}

    // 정렬 관련 변수
    private val sortNumber = MutableLiveData(SORT_NORMAL) // 기본 세팅

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRecyclerView()
    }


    // 3) 이미지 가로 세로 똑같이 유지하기 => 잘라서 정사각형으로 도출할 수 있게?

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        with(listViewModel) {

//             정렬 설정에 따라 가져오는 데이터
            sortNumber.observe(viewLifecycleOwner) { sortType ->
                when (sortType) {
                    SORT_NORMAL -> {
                        getAllDataDESC().observe(viewLifecycleOwner){
                            if (sortNumber.value == SORT_NORMAL) {
                                makeList(it)
                                binding.sortTextView.text = getString(R.string.list_menu_sort_recent)
                            }
                        }
                    }
                    SORT_IMPORTANCE -> {
                        getDiaryDataImportant().observe(viewLifecycleOwner){
                            if (sortNumber.value == SORT_IMPORTANCE) {
                                makeList(it)
                                binding.sortTextView.text = getString(R.string.list_menu_sort_important)
                            }
                        }
                    }
                }
            }

            listFragmentDiaryData.observe(viewLifecycleOwner){
                mAdapter.submitList(it)
            }
        }


        // 1. 툴바 관련 코드
        with(binding.listFragmentToolbar) {
            title = getString(com.dreamreco.joodiary.R.string.list_fragment_toolbar_title)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.sort_by_recent -> {
                        sortNumber.postValue(SORT_NORMAL)
                        true
                    }
                    R.id.sort_by_importance -> {
                        sortNumber.postValue(SORT_IMPORTANCE)
                        true
                    }
                    else -> false
                }
            }
        }

        return binding.root
    }




    private fun setRecyclerView() {
        with(binding) {
            with(listFragmentRecyclerView) {
                adapter = mAdapter
                setHasFixedSize(true)
                setItemViewCacheSize(13)
            }


            // recyclerView 갱신 시, 깜빡임 방지
            val animator = listFragmentRecyclerView.itemAnimator
            if (animator is SimpleItemAnimator){
                animator.supportsChangeAnimations = false
            }
        }
    }


}