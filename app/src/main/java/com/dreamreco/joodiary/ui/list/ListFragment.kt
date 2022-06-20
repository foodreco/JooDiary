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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : Fragment() {

    private val listViewModel by viewModels<ListViewModel>()
    private val binding by lazy { FragmentListBinding.inflate(layoutInflater) }
    private val mAdapter by lazy { ListFragmentAdapter(requireContext(), childFragmentManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRecyclerView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        Listfragment 2중 recyclerview x -> 상단 Textview 스와이프, 스와이프 적용 시, RecyclerView SubmitList 변경하는 방식으로...

        listViewModel.getAllDataDESC().observe(viewLifecycleOwner){
            listViewModel.makeList(it)
        }

        listViewModel.listFragmentDiaryData.observe(viewLifecycleOwner){
            mAdapter.submitList(it)
        }

        // 1. 툴바 관련 코드
        with(binding.listFragmentToolbar) {
            title = getString(com.dreamreco.joodiary.R.string.list_fragment_toolbar_title)
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



        return binding.root
    }




    private fun setRecyclerView() {
        with(binding) {
            listFragmentRecyclerView.adapter = mAdapter

            // recyclerView 갱신 시, 깜빡임 방지
            val animator = listFragmentRecyclerView.itemAnimator
            if (animator is SimpleItemAnimator){
                animator.supportsChangeAnimations = false
            }
        }
    }


}