package com.dreamreco.joodiary.ui.customDialog

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.FullImageDialogBinding
import com.dreamreco.joodiary.room.entity.MyDate
import com.dreamreco.joodiary.ui.calendar.CalendarViewModel
import com.dreamreco.joodiary.util.*
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileNotFoundException


@AndroidEntryPoint
class FullImageDialog : DialogFragment() {

    private val calendarViewModel by viewModels<CalendarViewModel>()
    private val binding by lazy { FullImageDialogBinding.inflate(layoutInflater) }
    private val args by navArgs<FullImageDialogArgs>()

    private val spinnerDrinkTypeList = mutableListOf<String>()
    private val spinnerPOAList = mutableListOf<String>()
    private val spinnerVODList = mutableListOf<String>()
    private var selectedDrinkType = ""
    private var selectedPOA = ""
    private var selectedVOD = ""
    private var diaryImportance = MutableLiveData<Boolean>(false)
    private var diaryImportanceForUpdate = false

    // 원본 사진이 저장되는 Uri
    // update 변수 역할도 함
    private var photoUri: Uri? = null

    // 뒤로 가기 처리를 위한 콜백 변수
    private lateinit var callback: OnBackPressedCallback

    // 뒤로 가기 처리를 위한 Live 변수
    private val backEventCheckLive = MutableLiveData<Boolean>()


//    // Dialog 배경 투명하게 하는 코드??
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState)
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//        return dialog
//    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // diaryBase 요소 세팅
        basicDiarySetting()

        with(calendarViewModel) {

//            // 저장 완료 후, 이전으로 돌아가는 코드
//            insertOrUpdateEventDone.observe(viewLifecycleOwner) { done ->
//                if (done) {
//                    findNavController().navigateUp()
//                }
//            }
//            // 삭제 완료 후, 이전으로 돌아가는 코드
//            dialogDeleteCompleted.observe(viewLifecycleOwner) { done ->
//                if (done) {
//                    findNavController().navigateUp()
//                }
//            }

            // 이미지를 불러오는 코드(GetImageDialog 에서 결정 시, 옵저버로 작동)
            getImageChangeSignal().observe(viewLifecycleOwner) { loadType ->
                when (loadType) {
                    0 -> {}
                    else -> {
                        findNavController().navigateUp()
                    }
                }
            }
        }

//        3) 툴바 타이틀 날짜로 해서 중간으로 정렬 (스피너로 날짜 변경가능 [insertOrUpdateData 로직 바꿔야함]/ 기존 데이터의 경우 날짜 수정 가능)
//        4) 주종, 도수, 주량 부분 감싸기 extendedLayout 또는 내용 아래로 내리기
//        주종 작성 후 키보드 '다음' 터치 시, 다음 스피너로 포커스 변경?
        // 이미지 크기에 관한 고찰 : 어떻게 size 설정해야 하는가?
        // 이미지 터치 시, 작동 코드 설계 (터치 시 확대 + 이미지 변경, 삭제)


        // 1. 툴바 관련 코드
        with(binding) {
            // #1 Top Toolbar
            with(dialogToolbar) {

                // X 터치 시 이전으로 돌아가는 코드
                setNavigationIcon(R.drawable.ic_close)
                navigationIcon?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
                setNavigationOnClickListener {
                    findNavController().navigateUp()
                }

                menuImageChange.setOnClickListener {
                    val bottomSheetDialog = GetImageDialog()
                    bottomSheetDialog.show(childFragmentManager, "GetImageDialog")
                }

//                setOnMenuItemClickListener {
//                    when (it.itemId) {
//                        // 이미지 변경
//                        R.id.menu_image_change -> {
//                            val bottomSheetDialog = GetImageDialog()
//                            bottomSheetDialog.show(childFragmentManager, "GetImageDialog")
//                            true
//                        }
//                        else -> false
//                    }
//                }
            }

            // #2 Bottom Toolbar
            btnBottomToolbarDelete.setOnClickListener {
                deleteData()
            }
        }

        return binding.root
    }


    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간
    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간
    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간


    private fun basicDiarySetting() {
        with(binding) {
            // 이미지
            photoUri = args.uriString.toUri()

            if (photoUri != null) {
                try {
                    val imageBitmap = ImageDecoder.createSource(
                        requireContext().contentResolver,
                        photoUri!!
                    )
                    diaryImageView.setImageBitmap(ImageDecoder.decodeBitmap(imageBitmap))
                } catch (e: FileNotFoundException) {
                    // room 에는 등록되었으나, 앨범에서 사진이 삭제되었을 때,
                    // FileNotFoundException 에러 발생
                    diaryImageView.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.gray
                        )
                    )
                    diaryImageView.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(), R.drawable.ic_add_photo_52
                        )
                    )
                }
            } else {
                diaryImageView.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.gray))
                diaryImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_add_photo_52
                    )
                )
            }
        }
    }

    // 저장 버튼 터치 시, 데이터를 업데이트하는 함수
    private fun updateData() {
//        val newImage = photoUri
////        스피너 추가 시 변경
//        val newDate = MyDate(
//            args.diaryBase.date.year,
//            args.diaryBase.date.month,
//            args.diaryBase.date.day
//        )
//        val newTitle = binding.titleText.text.trim().toString()
//        val newContent = binding.contentText.text.trim().toString()
//        val newDrinkType = binding.drinkType.text.trim().toString()
//        val newPOA = binding.POA.text.trim().toString()
//        val newVOD = binding.VOD.text.trim().toString()
//        val newImportance = diaryImportanceForUpdate
//
//        val updateList = DiaryBase(
//            newImage,
//            newDate,
//            args.diaryBase.calendarDay,
//            newTitle,
//            newContent,
//            newDrinkType,
//            newPOA,
//            newVOD,
//            newImportance,
//            args.diaryBase.calendarDay.toDateInt(),
//            args.diaryBase.id
//        )
//
//        // 제목이 빈칸이면
//        if (newTitle == "") {
//            Toast.makeText(
//                requireContext(),
//                getString(R.string.empty_title),
//                Toast.LENGTH_SHORT
//            ).show()
//        } else {
//            // 빈칸이 아닐 때, DB update 진행
//            calendarViewModel.insertOrUpdateData(updateList, args.diaryBase)
//        }
    }

    private fun deleteData() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton(getString(R.string.positive_button)) { _, _ ->
            // 해당 이미지 데이터 삭제 적용 코드
            calendarViewModel.setImageLoadType(IMAGE_DELETE)
        }
        builder.setNegativeButton(getString(R.string.negative_button)) { _, _ -> }
        builder.setTitle(getString(R.string.image_delete))
        builder.setMessage(getString(R.string.image_delete_setMessage))
        builder.create().show()
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setPositiveButton(getString(R.string.positive_button)) { _, _ ->
//            // 해당 데이터 삭제 코드
//            val deleteDate = MyDate(
//                args.diaryBase.date.year,
//                args.diaryBase.date.month,
//                args.diaryBase.date.day
//            )
//            val deleteTitle = args.diaryBase.title
//            calendarViewModel.deleteData(deleteDate, deleteTitle)
//        }
//        builder.setNegativeButton(getString(R.string.negative_button)) { _, _ -> }
//        builder.setTitle(getString(R.string.data_delete))
//        builder.setMessage(getString(R.string.data_format_setMessage))
//        builder.create().show()
    }

}