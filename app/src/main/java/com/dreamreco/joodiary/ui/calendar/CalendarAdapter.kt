package com.dreamreco.joodiary.ui.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.ImageDecoder
import android.graphics.Typeface
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
import com.dreamreco.joodiary.MyApplication
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.CalendarChildBinding
import com.dreamreco.joodiary.databinding.CalendarEmptyHeaderBinding
import com.dreamreco.joodiary.room.entity.DiaryBase
import com.dreamreco.joodiary.util.*
import java.io.FileNotFoundException

class CalenderAdapter(
    ctx: Context,
    fragmentManager: FragmentManager, typeface: Typeface, theme: String
) :
    ListAdapter<CalendarAdapterBase, RecyclerView.ViewHolder>(CalendarDiffCallback()) {

    // 기본 코드
    private var mFragmentManager: FragmentManager = fragmentManager
    private var mContext: Context = ctx
    private var mTypeface = typeface
    private val thisTheme = theme

    override fun getItemViewType(position: Int): Int = getItem(position).layoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CalendarChildBinding.inflate(layoutInflater, parent, false)

        return when (viewType) {
            CalendarAdapterBase.Item.VIEW_TYPE -> CalenderItemViewHolder(binding)
            CalendarAdapterBase.EmptyHeader.VIEW_TYPE -> CalenderEmptyHeaderViewHolder.from(
                parent, mTypeface
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
            is CalenderEmptyHeaderViewHolder -> {
                viewHolder.bind()
            }
        }
    }

    // 리스트용 뷰홀더
    inner class CalenderItemViewHolder constructor(private val binding: CalendarChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.P)
        fun bind(item: DiaryBase) {
            setGlobalFont(binding.root, mTypeface)
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
                    diaryVOD.text = "${item.myDrink!!.VOD}mL"
                    diaryPOA.text = "${item.myDrink!!.POA}%"
                } else {
                    diaryDrinkType.text = ""
                    diaryVOD.text = ""
                    diaryPOA.text = ""
                }

                if (item.importance) {
                    diaryBaseImportance.visibility = View.VISIBLE
                    when (thisTheme) {
                        THEME_2 -> diaryBaseImportance.imageTintList =
                            mContext.getColorStateList(R.color.theme2_primary_touch_color)
                        else -> diaryBaseImportance.imageTintList =
                            mContext.getColorStateList(R.color.basic_primary)
                    }
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
class CalenderEmptyHeaderViewHolder constructor(
    private val binding: CalendarEmptyHeaderBinding,
    typeface: Typeface
) :
    RecyclerView.ViewHolder(binding.root) {

    private val mTypeface = typeface

    fun bind() {
        setGlobalFont(binding.root, mTypeface)
    }

    companion object {
        fun from(parent: ViewGroup, typeface: Typeface): CalenderEmptyHeaderViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = CalendarEmptyHeaderBinding.inflate(layoutInflater, parent, false)
            return CalenderEmptyHeaderViewHolder(binding, typeface)
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