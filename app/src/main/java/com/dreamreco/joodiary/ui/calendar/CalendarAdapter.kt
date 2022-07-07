package com.dreamreco.joodiary.ui.calendar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.ImageDecoder
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.CalendarChildBinding
import com.dreamreco.joodiary.databinding.CalendarEmptyHeaderBinding
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.util.decodeSampledBitmapFromInputStream
import java.io.FileNotFoundException

class CalenderAdapter(
    ctx: Context,
    fragmentManager: FragmentManager
) :
    ListAdapter<CalendarAdapterBase, RecyclerView.ViewHolder>(CalendarDiffCallback()) {

    // 기본 코드
    private var mFragmentManager: FragmentManager = fragmentManager
    private var mContext: Context = ctx

//    // 삭제 관련 코드
//    private var checkBoxControlNumber: Int = 0
//    private val checkboxStatus = SparseBooleanArray()
//
//    // item.title 을 리스트로 받음
//    val checkBoxReturnList = mutableListOf<String>()
//    private val _deleteEventActive = MutableLiveData<Boolean?>()
//    val deleteEventActive: LiveData<Boolean?> = _deleteEventActive
//
//
//    private val importanceStatus = SparseBooleanArray()
//    private val _ttrImportanceSetting = MutableLiveData<TextToReadBase?>()
//    val ttrImportanceSetting: LiveData<TextToReadBase?> = _ttrImportanceSetting
//    private val _ttrImportanceRemoving = MutableLiveData<TextToReadBase?>()
//    val ttrImportanceRemoving: LiveData<TextToReadBase?> = _ttrImportanceRemoving
//    private val _checkBoxCheckedNumber = MutableLiveData(0)
//    val checkBoxCheckedNumber: LiveData<Int> = _checkBoxCheckedNumber

    override fun getItemViewType(position: Int): Int = getItem(position).layoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CalendarChildBinding.inflate(layoutInflater, parent, false)

        return when (viewType) {
            CalendarAdapterBase.Item.VIEW_TYPE -> CalenderItemViewHolder(binding)
            CalendarAdapterBase.EmptyHeader.VIEW_TYPE -> CalenderEmptyHeaderViewHolder.from(
                parent
            )
            else -> throw IllegalArgumentException("Cannot create ViewHolder for view type: $viewType")
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is CalenderItemViewHolder -> {
                val item = getItem(position) as CalendarAdapterBase.Item
                val diaryBase = item.diaryBase
                with(viewHolder) {
                    // bind
                    bind(diaryBase)
                }
            }
        }
    }

//    fun onCheckBox(number: Int) {
//        checkBoxControlNumber = number
//        notifyDataSetChanged()
//    }
//
//    fun deleteEventReset() {
//        _deleteEventActive.value = null
//    }
//
//    @JvmName("getCheckBoxReturnList1")
//    fun getCheckBoxReturnList(): List<String> {
//        return checkBoxReturnList
//    }
//
//    fun clearCheckBoxReturnList() {
//        checkBoxReturnList.clear()
//        checkboxStatus.clear()
//        _checkBoxCheckedNumber.value = 0
//    }
//
//    fun importanceReset() {
//        _ttrImportanceSetting.value = null
//        _ttrImportanceRemoving.value = null
//    }

    // 리스트용 뷰홀더
    inner class CalenderItemViewHolder constructor(private val binding: CalendarChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.P)
        fun bind(item: DiaryBase) {
            binding.apply {

                diaryTitle.text = item.title

                if (item.image != null) {
                    try {
                        with(diaryImage) {
                            imageTintList = null
                            setImageBitmap(
                                item.bitmapForRecyclerView
                            )
                        }
                    } catch (e: FileNotFoundException) {
                        // room 에는 등록되었으나, 앨범에서 사진이 삭제되었을 때,
                        // FileNotFoundException 에러 발생
                        diaryImage.imageTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray))
                        diaryImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                mContext, R.drawable.ic_image
                            )
                        )
                    }
                } else {
                    diaryImage.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray))
                    diaryImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            mContext, R.drawable.ic_image
                        )
                    )
                }

                if (item.myDrink != null) {
                    diaryDrinkType.text = item.myDrink!!.drinkType
                    diaryVOD.text = item.myDrink!!.VOD
                    diaryPOA.text = item.myDrink!!.POA
                } else {
                    diaryDrinkType.text = ""
                    diaryVOD.text = ""
                    diaryPOA.text = ""
                }

                if (item.importance) {
                    diaryBaseImportance.visibility = View.VISIBLE
                } else {
                    diaryBaseImportance.visibility = View.INVISIBLE
                }

//                //리싸이클러 길게 터치 시,
//                recyclerViewChildLayout.setOnLongClickListener {
//                    Toast.makeText(mContext, "Long~~Touch!", Toast.LENGTH_SHORT).show()
//                    return@setOnLongClickListener true
//                }

                //리싸이클러 터치 시, 해당 ContactBase 정보를 bundle 로 넘기고 updateDialog show
                recyclerViewChildLayout.setOnClickListener {
                    val action =
                        CalendarFragmentDirections.actionCalenderFragmentToDiaryDetailDialog(
                            item
                        )
                    it.findNavController().navigate(action)
                }
            }
        }
    }

}

// empty 헤더용 뷰홀더
class CalenderEmptyHeaderViewHolder constructor(private val binding: CalendarEmptyHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun from(parent: ViewGroup): CalenderEmptyHeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = CalendarEmptyHeaderBinding.inflate(layoutInflater, parent, false)
            return CalenderEmptyHeaderViewHolder(binding)
        }
    }
}

class CalendarDiffCallback : DiffUtil.ItemCallback<CalendarAdapterBase>() {
    override fun areItemsTheSame(
        oldItem: CalendarAdapterBase,
        newItem: CalendarAdapterBase
    ): Boolean {
        return oldItem.layoutId == newItem.layoutId
    }

    override fun areContentsTheSame(
        oldItem: CalendarAdapterBase,
        newItem: CalendarAdapterBase
    ): Boolean {
        return oldItem == newItem
    }
}

// ListFragment Adapter 에서 헤더용으로 사용된 sealed class
sealed class CalendarAdapterBase {
    abstract val layoutId: Int

    // view
    data class Item(
        val diaryBase: DiaryBase,
        override val layoutId: Int = VIEW_TYPE
    ) : CalendarAdapterBase() {

        companion object {
            const val VIEW_TYPE = R.layout.calendar_child
        }
    }

    // 리스트가 없을 때,
    data class EmptyHeader(
        override val layoutId: Int = VIEW_TYPE
    ) : CalendarAdapterBase() {

        companion object {
            const val VIEW_TYPE = R.layout.calendar_empty_header
        }
    }
}