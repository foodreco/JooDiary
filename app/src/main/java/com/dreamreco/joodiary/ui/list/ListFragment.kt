package com.dreamreco.joodiary.ui.list

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.SimpleItemAnimator
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.FragmentListBinding
import com.dreamreco.joodiary.util.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ListFragment : Fragment() {

    private val listViewModel by viewModels<ListViewModel>()
    private val binding by lazy { FragmentListBinding.inflate(layoutInflater) }
    private var typeface: Typeface? = null
    lateinit var mAdapter: ListFragmentAdapter

    // 정렬 관련 변수
    private val sortNumber = MutableLiveData(SORT_NORMAL) // 기본 세팅

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 폰트 설정 및 적용 코드
        typeface = getFontType(requireContext())
        typeface?.let { setRecyclerView(it) }
        typeface?.let { setGlobalFont(binding.root, it) }
    }

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


    private fun setRecyclerView(typeface: Typeface) {
        mAdapter = ListFragmentAdapter(requireContext(),childFragmentManager, typeface, getThemeType())
        with(binding) {
            with(listFragmentRecyclerView) {
                adapter = mAdapter
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