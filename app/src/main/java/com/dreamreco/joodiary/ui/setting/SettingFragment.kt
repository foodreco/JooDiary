package com.dreamreco.joodiary.ui.setting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.FragmentSettingBinding
import com.dreamreco.joodiary.ui.customDialog.BackUpLoadingDialog
import com.dreamreco.joodiary.ui.customDialog.GetImageDialog
import com.dreamreco.joodiary.util.GET_DATA_PERMISSIONS
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SettingFragment : Fragment() {

    private val settingViewModel by viewModels<SettingViewModel>()
    private val binding by lazy { FragmentSettingBinding.inflate(layoutInflater) }
    private var settingLoginState = true

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 기록 시작일과 기록 일수를 가져오는 코드
        setDaysOfRecord()

//        2.pdf 파일로 내보내기

        // 터치 설계하기
        with(binding) {
            settingBackUp.setOnClickListener {
                checkPermissionsAndSetBackup()
            }

            settingFont.setOnClickListener {
                // 테마 색상, 폰트 변경
            }

            settingLogin.setOnClickListener {
                // 보안 설정
                settingLoginButtonExpend()
            }
            radioButtonForPassword.setOnClickListener {
                radioButtonForBio.isChecked = false
                radioButtonForNothing.isChecked = false
                Toast.makeText(requireContext(),"패스워드",Toast.LENGTH_SHORT).show()
            }
            radioButtonForBio.setOnClickListener {
                radioButtonForPassword.isChecked = false
                radioButtonForNothing.isChecked = false
                Toast.makeText(requireContext(),"생체",Toast.LENGTH_SHORT).show()
            }
            radioButtonForNothing.setOnClickListener {
                radioButtonForPassword.isChecked = false
                radioButtonForBio.isChecked = false
                Toast.makeText(requireContext(),"설정 안함",Toast.LENGTH_SHORT).show()
            }
        }


        // 1. 툴바 관련 코드
        with(binding.settingToolbar) {
            title = getString(com.dreamreco.joodiary.R.string.setting_fragment_toolbar_title)
        }

        return binding.root
    }

    private fun settingLoginButtonExpend() {
        with(binding) {
            if (settingLoginState) {
                settingLoginState = !settingLoginState
                imageForArrow.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_arrow_up))
                settingLoginChild1.visibility = View.VISIBLE
                settingLoginChild2.visibility = View.VISIBLE
                settingLoginChild3.visibility = View.VISIBLE

                // 기본 설정에 따라, 라디오 버튼 체크해줘야 함

                radioButtonForBio.isChecked = false
                radioButtonForPassword.isChecked = false
                radioButtonForNothing.isChecked = false

            } else {
                settingLoginState = !settingLoginState
                imageForArrow.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_arrow_down))
                settingLoginChild1.visibility = View.GONE
                settingLoginChild2.visibility = View.GONE
                settingLoginChild3.visibility = View.GONE


                radioButtonForBio.isChecked = false
                radioButtonForPassword.isChecked = false
                radioButtonForNothing.isChecked = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun checkPermissionsAndSetBackup() {
        val permissions = GET_DATA_PERMISSIONS
        if (!checkNeedPermissionBoolean(permissions)) {
            // 허용 안되어 있는 경우, 요청
            requestMultiplePermissionsForBackUp.launch(
                permissions
            )
        } else {
            // 허용 되어있는 경우, 코드 작동
            moveToDialog()
        }
    }

    // 허용 요청 코드 및 작동 (백업 작동)
    @RequiresApi(Build.VERSION_CODES.P)
    private val requestMultiplePermissionsForBackUp =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                // 허용된, 경우
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_access),
                    Toast.LENGTH_SHORT
                ).show()
                // 백업 작동
                moveToDialog()
            } else {
                // 허용안된 경우,
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_denied_backup),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // 허용 여부에 따른 Boolean 반환
    private fun checkNeedPermissionBoolean(permissions: Array<String>): Boolean {
        for (perm in permissions) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun moveToDialog() {
        val popUpDialog = BackUpLoadingDialog()
        popUpDialog.show(childFragmentManager, "BackUpLoadingDialog")
    }

    // 기록 시작일과 기록 일수를 가져오는 코드
    private fun setDaysOfRecord() {
        with(settingViewModel) {
            getStartDate().observe(viewLifecycleOwner) { firstDate ->
                if (firstDate != null) {
                    binding.recordStartDate.text = getString(
                        R.string.diary_start_date,
                        firstDate.year,
                        firstDate.month,
                        firstDate.day
                    )
                } else {
                    binding.recordStartDate.text = getString(R.string.empty_recordStartDate)
                }
            }
            getNumberOfRecord().observe(viewLifecycleOwner) { dateList ->
                countNumberOfRecords(dateList)
            }
            numberOfRecordDays.observe(viewLifecycleOwner) { recordedDateNumber ->
                binding.numberOfRecordDays.text = getString(R.string.numberOfRecordedDate, recordedDateNumber)
            }
        }
    }
}