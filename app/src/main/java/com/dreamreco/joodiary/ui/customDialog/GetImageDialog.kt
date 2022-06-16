package com.dreamreco.joodiary.ui.customDialog

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.dreamreco.joodiary.MyApplication
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.GetImageDialogBinding
import com.dreamreco.joodiary.ui.calendar.CalendarViewModel
import com.dreamreco.joodiary.util.LOAD_IMAGE_FROM_CAMERA
import com.dreamreco.joodiary.util.LOAD_IMAGE_FROM_GALLERY
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GetImageDialog : BottomSheetDialogFragment() {

    private val calendarViewModel by viewModels<CalendarViewModel>()
    private val binding by lazy { GetImageDialogBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setImageIcon()

        // 이미지 불러오는 방식을 설정하는 코드
        with(binding) {
            galleryImageBtn.setOnClickListener {
                calendarViewModel.setImageLoadType(LOAD_IMAGE_FROM_GALLERY)
            }
            cameraImageBtn.setOnClickListener {
                calendarViewModel.setImageLoadType(LOAD_IMAGE_FROM_CAMERA)
            }
        }

        // 이미지 불러오는 방식이 변경완료되면 dismiss
        calendarViewModel.changeLoadImageTypeCompleted.observe(viewLifecycleOwner){
            if (it) {
                dismiss()
            }
        }

        return binding.root
    }

    private fun setImageIcon() {
        val packageManager = requireContext().packageManager
        val flag = 0;
        val galleryPackageName = "com.sec.android.gallery3d"
        val cameraPackageName = "com.sec.android.app.camera"

        // 패키지 네임 존재 유무에 따라, 이미지를 세팅하는 코드
        try {
            val info: PackageInfo = packageManager.getPackageInfo(
                galleryPackageName, flag
            )
            val appIcon: Drawable =
                requireContext().packageManager.getApplicationIcon(galleryPackageName)
            binding.galleryImageBtn.setImageDrawable(appIcon)

        } catch (e: PackageManager.NameNotFoundException) {
            binding.galleryImageBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_photo_library_48
                )
            )
        }

        try {
            val info: PackageInfo = packageManager.getPackageInfo(
                cameraPackageName, flag
            )
            val appIcon: Drawable =
                requireContext().packageManager.getApplicationIcon(cameraPackageName)
            binding.cameraImageBtn.setImageDrawable(appIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            binding.cameraImageBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_camera_48
                )
            )
        }
    }
}