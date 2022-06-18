package com.dreamreco.joodiary.ui.statistics

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

//class ListFragmentInnerAdapter(
//    ctx: Context,
//    fragmentManager: FragmentManager
//) :
//    ListAdapter<DiaryBase, ListFragmentInnerAdapter.ListFragmentInnerItemViewHolder>(ListFragmentInnerDiffCallback()) {
//
//    // 기본 코드
//    private var mFragmentManager: FragmentManager = fragmentManager
//    private var mContext: Context = ctx
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListFragmentInnerItemViewHolder {
//        val layoutInflater = LayoutInflater.from(parent.context)
//        val binding = ListFragmentChildBinding.inflate(layoutInflater, parent, false)
//        return ListFragmentInnerItemViewHolder(binding)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.P)
//    override fun onBindViewHolder(viewHolder: ListFragmentInnerItemViewHolder, position: Int) {
//        viewHolder.bind(getItem(position), mFragmentManager)
//    }
//
//    // 리스트용 뷰홀더
//    inner class ListFragmentInnerItemViewHolder constructor(private val binding: ListFragmentChildBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        @RequiresApi(Build.VERSION_CODES.P)
//        fun bind(item: DiaryBase, fragmentManager: FragmentManager) {
//            binding.apply {
//
//                diaryTitle.text = item.title
//                diaryContent.text = item.content
//
//                if (item.image != null) {
//                    try {
//                        diaryImage.imageTintList = null
//                        val imageBitmap = ImageDecoder.createSource(
//                            mContext.contentResolver,
//                            item.image!!
//                        )
//                        diaryImage.setImageBitmap(ImageDecoder.decodeBitmap(imageBitmap))
//                    } catch (e: FileNotFoundException) {
//                        // room 에는 등록되었으나, 앨범에서 사진이 삭제되었을 때,
//                        // FileNotFoundException 에러 발생
//                        diaryImage.imageTintList =
//                            ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.gray))
//                        diaryImage.setImageDrawable(
//                            ContextCompat.getDrawable(
//                                mContext, R.drawable.ic_add_photo_52
//                            )
//                        )
//                    }
//                }
//                if (item.drinkType != null) {
//                    diaryDrinkType.text = item.drinkType
//                }
//                if (item.POA != null) {
//                    diaryPOA.text = item.POA
//                }
//                if (item.VOD != null) {
//                    diaryVOD.text = item.VOD
//                }
//
//                //리싸이클러 길게 터치 시,
//                recyclerViewChildLayout.setOnLongClickListener {
//
//                    return@setOnLongClickListener true
//                }
//
//                //리싸이클러 터치 시, 해당 ContactBase 정보를 bundle 로 넘기고 updateDialog show
////                recyclerViewChildLayout.setOnClickListener {
////                    val action = ListFragmentDirections.actionListFragmentToDiaryDetailDialog(
////                        item,
////                        LIST_FRAGMENT
////                    )
////                    it.findNavController().navigate(action)
////                }
//            }
//        }
//    }
//}
//
//class ListFragmentInnerDiffCallback : DiffUtil.ItemCallback<DiaryBase>() {
//    override fun areItemsTheSame(
//        oldItem: DiaryBase,
//        newItem: DiaryBase
//    ): Boolean {
//        return oldItem.id == newItem.id
//    }
//
//    override fun areContentsTheSame(
//        oldItem: DiaryBase,
//        newItem: DiaryBase
//    ): Boolean {
//        return oldItem == newItem
//    }
//}




class ListFragmentInnerAdapter(
    ctx: Context,
    fragmentManager: FragmentManager,
    val item: List<DiaryBase>
) :
    RecyclerView.Adapter<ListFragmentInnerAdapter.ListFragmentInnerItemViewHolder>() {

    // 기본 코드
    private var mFragmentManager: FragmentManager = fragmentManager
    private var mContext: Context = ctx

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListFragmentInnerItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListFragmentChildBinding.inflate(layoutInflater, parent, false)
        return ListFragmentInnerItemViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onBindViewHolder(viewHolder: ListFragmentInnerItemViewHolder, position: Int) {
        viewHolder.bind(item[position], mFragmentManager)
    }

    // 리스트용 뷰홀더
    inner class ListFragmentInnerItemViewHolder constructor(private val binding: ListFragmentChildBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.P)
        fun bind(item: DiaryBase, fragmentManager: FragmentManager) {
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
//                recyclerViewChildLayout.setOnClickListener {
//                    val action = ListFragmentDirections.actionListFragmentToDiaryDetailDialog(
//                        item,
//                        LIST_FRAGMENT
//                    )
//                    it.findNavController().navigate(action)
//                }
            }
        }
    }

    override fun getItemCount(): Int {
        return item.size
    }
}