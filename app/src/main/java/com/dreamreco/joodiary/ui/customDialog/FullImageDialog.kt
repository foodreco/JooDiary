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

    // 원본 사진이 저장되는 Uri
    // update 변수 역할도 함
    private var photoUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // diaryBase 요소 세팅
        basicDiarySetting()

        with(calendarViewModel) {
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


    @RequiresApi(Build.VERSION_CODES.P)
    private fun basicDiarySetting() {
        with(binding) {
            // 이미지
            photoUri = args.uriString.toUri()

            if (photoUri != null) {
                try {
                    with(diaryImageView) {
                        imageTintList = null
                        diaryImageView.setImageBitmap(decodeSampledBitmapFromInputStream(photoUri!!,500,500,requireContext()))
                    }
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
                    Toast.makeText(requireContext(),getString(R.string.image_load_error), Toast.LENGTH_SHORT).show()
                }
            } else {
                diaryImageView.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.gray))
                diaryImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_add_photo_52
                    )
                )
                Toast.makeText(requireContext(),getString(R.string.image_load_error), Toast.LENGTH_SHORT).show()
            }
        }
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
    }

}