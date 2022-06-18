package com.dreamreco.joodiary.ui.statistics

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.joodiary.databinding.PagerChildBinding
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.ui.list.ListFragmentAdapter

//class PagerRecyclerAdapter(context: Context, fragmentManager: FragmentManager) :
//    RecyclerView.Adapter<PagerRecyclerAdapter.PagerViewHolder>() {
//
////     -> 모든 데이터를 한방에 다 넘겨주어야 함
////    뷰모델에서 재가공해서 넘겨주어야 함...
////    이중 데이터로...
//
//    private var list = mutableListOf<PagerOutData>()
//    private val diffCallback = MonthListDiffCallback(list, ArrayList())
//    private val mAdapter by lazy { ListFragmentInnerAdapter(context, fragmentManager) }
//    private val mContext = context
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
//        val layoutInflater = LayoutInflater.from(parent.context)
//        val binding = PagerChildBinding.inflate(layoutInflater, parent, false)
//        return PagerViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
//        holder.bind(list[position], list[position].pagerInnerData)
//    }
//
//    override fun getItemCount(): Int = list.size
//
//    inner class PagerViewHolder constructor(private val binding: PagerChildBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(pagerOutData: PagerOutData, diaryBase: MutableList<DiaryBase>) {
//            binding.listFragmentToolbar.title =
//                "${pagerOutData.myMonth.year}년${pagerOutData.myMonth.month}월"
//            binding.listFragmentRecyclerView.adapter = mAdapter
//            mAdapter.submitList(diaryBase)
//        }
//    }
//
//    // DiffUtil 을 적용한 리스트 업데이트 함수
//    fun submitListForInnerRecyclerView(newList: List<PagerOutData>) {
//        // 신규로 들어온 리스트를 diffCallback newList 자리로 업데이트
//        diffCallback.newList = newList
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//
//        // 기존 리스트를 newList 로 갱신
//        list.clear()
//        list.addAll(newList)
//
//        diffResult.dispatchUpdatesTo(this)
//    }
//}
//
//class MonthListDiffCallback(
//    val oldList: List<PagerOutData>,
//    var newList: List<PagerOutData>
//) : DiffUtil.Callback() {
//
//    override fun getOldListSize(): Int {
//        return oldList.size
//    }
//
//    override fun getNewListSize(): Int {
//        return newList.size
//    }
//
//    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//        val oldItem = oldList[oldItemPosition]
//        val newItem = newList[newItemPosition]
//        return oldItem.id == newItem.id
//    }
//
//    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//        val oldItem = oldList[oldItemPosition]
//        val newItem = newList[newItemPosition]
//        return oldItem == newItem
//    }
//}








class PagerRecyclerAdapter(context: Context, fragmentManager: FragmentManager) :
    ListAdapter<PagerOutData, PagerRecyclerAdapter.PagerViewHolder>(MonthListDiffCallback()) {

//     -> 모든 데이터를 한방에 다 넘겨주어야 함
//    뷰모델에서 재가공해서 넘겨주어야 함...
//    이중 데이터로...

//    private var list = mutableListOf<PagerOutData>()
//    private val diffCallback = MonthListDiffCallback(list, ArrayList())
//    private val mAdapter by lazy { ListFragmentInnerAdapter(context, fragmentManager) }

    private val mContext = context
    private val mFragmentManager = fragmentManager


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PagerChildBinding.inflate(layoutInflater, parent, false)
        return PagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.bind(getItem(position), getItem(position).pagerInnerData)
    }

//    override fun getItemCount(): Int = list.size

    inner class PagerViewHolder constructor(private val binding: PagerChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pagerOutData: PagerOutData, diaryBase: List<DiaryBase>) {



            binding.listFragmentToolbar.title =
                "${pagerOutData.myMonth.year}년${pagerOutData.myMonth.month}월"

            binding.listFragmentRecyclerView.adapter = ListFragmentInnerAdapter(mContext, mFragmentManager, pagerOutData.pagerInnerData)

            Log.e("PagerRecyclerAdapter","pagerOutData id : ${pagerOutData.id}")
            Log.e("PagerRecyclerAdapter","pagerOutData month : ${pagerOutData.myMonth}")
            Log.e("PagerRecyclerAdapter","pagerOutData data : ${pagerOutData.pagerInnerData}")

            for (i in diaryBase) {
                Log.e("PagerRecyclerAdapter","diaryBase : ${i.dateForSort}")
            }


//            mAdapter.submitList(diaryBase)

        }
    }

//    // DiffUtil 을 적용한 리스트 업데이트 함수
//    fun submitListForInnerRecyclerView(newList: List<PagerOutData>) {
//        // 신규로 들어온 리스트를 diffCallback newList 자리로 업데이트
//        diffCallback.newList = newList
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//
//        // 기존 리스트를 newList 로 갱신
//        list.clear()
//        list.addAll(newList)
//
//        diffResult.dispatchUpdatesTo(this)
//    }
}

class MonthListDiffCallback : DiffUtil.ItemCallback<PagerOutData>() {
    override fun areItemsTheSame(oldItem: PagerOutData, newItem: PagerOutData): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PagerOutData, newItem: PagerOutData): Boolean {
        return oldItem == newItem
    }
}

//data class PagerOutData(
//    var id : Int,
//    var year : Int,
//    var month : Int,
//    var pagerInnerData : MutableList<DiaryBase>
//)

data class PagerOutData(
    var id: Int,
    var myMonth: MyMonth,
    var pagerInnerData: MutableList<DiaryBase>
)

data class MyMonth(
    var year: Int,
    var month: Int,
)
