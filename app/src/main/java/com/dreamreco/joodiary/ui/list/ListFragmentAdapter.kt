package com.dreamreco.joodiary.ui.list

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.ImageDecoder
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.CalendarEmptyHeaderBinding
import com.dreamreco.joodiary.databinding.DateHeaderBinding
import com.dreamreco.joodiary.databinding.ListFragmentChildBinding
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.util.LIST_FRAGMENT
import java.io.FileNotFoundException

class ListFragmentAdapter(
    ctx: Context,
    fragmentManager: FragmentManager
) :
    ListAdapter<ListFragmentAdapterBase, RecyclerView.ViewHolder>(ListFragmentDiffCallback()) {

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
        val binding = ListFragmentChildBinding.inflate(layoutInflater, parent, false)

        return when (viewType) {
            ListFragmentAdapterBase.Item.VIEW_TYPE -> ListFragmentItemViewHolder(binding)
            ListFragmentAdapterBase.EmptyHeader.VIEW_TYPE -> ListFragmentEmptyHeaderViewHolder.from(
                parent
            )
            ListFragmentAdapterBase.DateHeader.VIEW_TYPE -> ListFragmentDateHeaderViewHolder.from(
                parent,
                mContext
            )
            else -> throw IllegalArgumentException("Cannot create ViewHolder for view type: $viewType")
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is ListFragmentItemViewHolder -> {
                val item = getItem(position) as ListFragmentAdapterBase.Item
                val diaryBase = item.diaryBase
                with(viewHolder) {
                    // bind
                    bind(diaryBase)
                }
            }
            is ListFragmentDateHeaderViewHolder -> {
                val item = getItem(position) as ListFragmentAdapterBase.DateHeader
                viewHolder.bind(item)
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
    inner class ListFragmentItemViewHolder constructor(private val binding: ListFragmentChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.P)
        fun bind(item: DiaryBase) {
            binding.apply {

                diaryTitle.text = item.title
                diaryContent.text = item.content

                if (item.image != null) {
                    try {
                        diaryImage.imageTintList = null
                        val imageBitmap = ImageDecoder.createSource(
                            mContext.contentResolver,
                            item.image!!
                        )
                        diaryImage.setImageBitmap(ImageDecoder.decodeBitmap(imageBitmap))
                    } catch (e: FileNotFoundException) {
                        // room 에는 등록되었으나, 앨범에서 사진이 삭제되었을 때,
                        // FileNotFoundException 에러 발생
                        diaryImage.imageTintList =
                            ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray))
                        diaryImage.setImageDrawable(
                            ContextCompat.getDrawable(
                                mContext, R.drawable.ic_add_photo_52
                            )
                        )
                    }
                } else {
                    diaryImage.imageTintList =
                        ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray))
                    diaryImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            mContext, R.drawable.ic_add_photo_52
                        )
                    )
                }
                if (item.drinkType != null) {
                    diaryDrinkType.text = item.drinkType
                }
                if (item.POA != null) {
                    diaryPOA.text = item.POA
                }
                if (item.VOD != null) {
                    diaryVOD.text = item.VOD
                }

                //리싸이클러 길게 터치 시,
                recyclerViewChildLayout.setOnLongClickListener {

                    return@setOnLongClickListener true
                }

                //리싸이클러 터치 시, 해당 ContactBase 정보를 bundle 로 넘기고 updateDialog show
                recyclerViewChildLayout.setOnClickListener {
                    val action = ListFragmentDirections.actionListFragmentToDiaryDetailDialog(
                        item
                    )
                    it.findNavController().navigate(action)
                }
            }
        }
    }

}

// empty 헤더용 뷰홀더
class ListFragmentEmptyHeaderViewHolder constructor(private val binding: CalendarEmptyHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun from(parent: ViewGroup): ListFragmentEmptyHeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = CalendarEmptyHeaderBinding.inflate(layoutInflater, parent, false)
            return ListFragmentEmptyHeaderViewHolder(binding)
        }
    }
}

// date 헤더용 뷰홀더
class ListFragmentDateHeaderViewHolder constructor(
    private val binding: DateHeaderBinding,
    context: Context
) :
    RecyclerView.ViewHolder(binding.root) {

    private val mContext = context

    companion object {
        fun from(parent: ViewGroup, context: Context): ListFragmentDateHeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = DateHeaderBinding.inflate(layoutInflater, parent, false)
            return ListFragmentDateHeaderViewHolder(binding, context)
        }
    }

    fun bind(item: ListFragmentAdapterBase) {
        val diaryBase = (item as ListFragmentAdapterBase.DateHeader).diaryBase
        binding.apply {
            textDate.text = mContext.getString(
                R.string.diary_month,
                diaryBase.date.year,
                diaryBase.date.month
            )
        }
    }
}

class ListFragmentDiffCallback : DiffUtil.ItemCallback<ListFragmentAdapterBase>() {
    override fun areItemsTheSame(
        oldItem: ListFragmentAdapterBase,
        newItem: ListFragmentAdapterBase
    ): Boolean {
        return oldItem.layoutId == newItem.layoutId
    }

    override fun areContentsTheSame(
        oldItem: ListFragmentAdapterBase,
        newItem: ListFragmentAdapterBase
    ): Boolean {
        return oldItem == newItem
    }
}

// ListFragment Adapter sealed class
sealed class ListFragmentAdapterBase {
    abstract val layoutId: Int

    // Item
    data class Item(
        val diaryBase: DiaryBase,
        override val layoutId: Int = VIEW_TYPE
    ) : ListFragmentAdapterBase() {

        companion object {
            const val VIEW_TYPE = R.layout.calendar_child
        }
    }

    // Date
    data class DateHeader(
        val diaryBase: DiaryBase,
        override val layoutId: Int = VIEW_TYPE
    ) : ListFragmentAdapterBase() {

        companion object {
            const val VIEW_TYPE = R.layout.date_header
        }
    }

    // 리스트가 없을 때,
    data class EmptyHeader(
        override val layoutId: Int = VIEW_TYPE
    ) : ListFragmentAdapterBase() {

        companion object {
            const val VIEW_TYPE = R.layout.list_fragment_empty_header
        }
    }
}