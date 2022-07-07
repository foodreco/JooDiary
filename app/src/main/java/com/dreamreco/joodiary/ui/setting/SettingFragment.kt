package com.dreamreco.joodiary.ui.setting

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dreamreco.joodiary.MainActivity
import com.dreamreco.joodiary.MyApplication
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.SplashActivity
import com.dreamreco.joodiary.databinding.FragmentSettingBinding
import com.dreamreco.joodiary.ui.customDialog.BackUpLoadingDialog
import com.dreamreco.joodiary.util.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess


@AndroidEntryPoint
class SettingFragment : Fragment() {

    private val settingViewModel by viewModels<SettingViewModel>()
    private val binding by lazy { FragmentSettingBinding.inflate(layoutInflater) }
    private var settingLoginState = true
    private var settingThemeState = true

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 기록 시작일과 기록 일수를 가져오는 코드
        setDaysOfRecord()

//        2.pdf 파일로 내보내기

        with(binding) {
            settingBackUp.setOnClickListener {
                checkPermissionsAndSetBackup()
            }

            // 테마 변경
            settingTheme.setOnClickListener {
                settingThemeButtonExpend()
            }
            // 테마 기본
            settingThemeBasicBtn.setOnClickListener {
                setThemeAndRestartApp(THEME_BASIC)
            }
            // 테마 1
            settingTheme1Btn.setOnClickListener {
                setThemeAndRestartApp(THEME_1)
            }
            // 테마 2
            settingTheme2Btn.setOnClickListener {
                setThemeAndRestartApp(THEME_2)
            }



            settingLogin.setOnClickListener {
                settingLoginButtonExpend()
            }

            // 비밀번호 잠금
            radioButtonForPassword.setOnClickListener {
                setPassword()
            }
            // 생체인식 잠금
            radioButtonForBio.setOnClickListener {
                setBiometric()
            }
            // 잠금 해제
            radioButtonForNothing.setOnClickListener {
                setLockout()
            }

        }


        settingViewModel.setThemeDone.observe(viewLifecycleOwner) { done ->
            if (done) {
                setThemeRadioButton()
                restartApp()
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
                imageForArrow.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_arrow_up
                    )
                )
                settingLoginChild1.visibility = View.VISIBLE
                settingLoginChild2.visibility = View.VISIBLE
                settingLoginChild3.visibility = View.VISIBLE
                settingLoginChild4.visibility = View.VISIBLE

                // 기본 설정에 따라, 라디오 버튼 체크
                setLoginRadioButton()

            } else {
                settingLoginState = !settingLoginState
                imageForArrow.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_arrow_down
                    )
                )
                settingLoginChild1.visibility = View.GONE
                settingLoginChild2.visibility = View.GONE
                settingLoginChild3.visibility = View.GONE
                settingLoginChild4.visibility = View.GONE
            }
        }
    }

    private fun settingThemeButtonExpend() {
        with(binding) {
            if (settingThemeState) {
                settingThemeState = !settingThemeState
                imageForThemeArrow.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_arrow_up
                    )
                )
                settingThemeChild1.visibility = View.VISIBLE
                settingThemeChild2.visibility = View.VISIBLE
                settingThemeChild3.visibility = View.VISIBLE

                // 기본 설정에 따라, 라디오 버튼 체크
                setThemeRadioButton()

            } else {
                settingThemeState = !settingThemeState
                imageForThemeArrow.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_arrow_down
                    )
                )
                settingThemeChild1.visibility = View.GONE
                settingThemeChild2.visibility = View.GONE
                settingThemeChild3.visibility = View.GONE
            }
        }
    }

    private fun setLoginRadioButton() {
        with(binding) {
            when (MyApplication.prefs.getString(LOGIN_TYPE, LOGIN_WITH_NOTHING)) {
                LOGIN_WITH_NOTHING -> {
                    radioButtonForNothing.isChecked = true
                    radioButtonForNothing.isEnabled = false

                    radioButtonForPassword.isChecked = false
                    radioButtonForPassword.isEnabled = true

                    radioButtonForBio.isChecked = false
                    radioButtonForBio.isEnabled = true
                }
                LOGIN_WITH_PASSWORD -> {
                    radioButtonForNothing.isChecked = false
                    radioButtonForNothing.isEnabled = true

                    radioButtonForPassword.isChecked = true
                    radioButtonForPassword.isEnabled = false

                    radioButtonForBio.isChecked = false
                    radioButtonForBio.isEnabled = true
                }
                LOGIN_WITH_BIO -> {
                    radioButtonForNothing.isChecked = false
                    radioButtonForNothing.isEnabled = true

                    radioButtonForPassword.isChecked = false
                    radioButtonForPassword.isEnabled = true

                    radioButtonForBio.isChecked = true
                    radioButtonForBio.isEnabled = false
                }
            }
        }
    }

    private fun setThemeRadioButton() {
        with(binding) {
            when (MyApplication.prefs.getString(THEME_TYPE, THEME_BASIC)) {
                THEME_BASIC -> {
                    settingThemeBasicBtn.isChecked = true
                    settingThemeBasicBtn.isEnabled = false

                    settingTheme1Btn.isChecked = false
                    settingTheme1Btn.isEnabled = true

                    settingTheme2Btn.isChecked = false
                    settingTheme2Btn.isEnabled = true
                }
                THEME_1 -> {
                    settingThemeBasicBtn.isChecked = false
                    settingThemeBasicBtn.isEnabled = true

                    settingTheme1Btn.isChecked = true
                    settingTheme1Btn.isEnabled = false

                    settingTheme2Btn.isChecked = false
                    settingTheme2Btn.isEnabled = true
                }
                THEME_2 -> {
                    settingThemeBasicBtn.isChecked = false
                    settingThemeBasicBtn.isEnabled = true

                    settingTheme1Btn.isChecked = false
                    settingTheme1Btn.isEnabled = true

                    settingTheme2Btn.isChecked = true
                    settingTheme2Btn.isEnabled = false
                }
            }
        }
    }

    // 비밀번호 잠금
    private fun setPassword() {
        with(binding) {
            radioButtonForBio.isChecked = false
            radioButtonForNothing.isChecked = false
        }
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setPositiveButton(getString(R.string.positive_button)) { _, _ ->
            // 비밀번호 등록이 완료되는 시점에서 login_type 이 변경된다.
            // 비밀번호 설정 Dialog 로 이동
            val action =
                SettingFragmentDirections.actionSettingFragmentToPasswordSettingDialog()
            findNavController().navigate(action)
        }
        builder.setNegativeButton(getString(R.string.negative_button)) { _, _ ->
            // 아니오, 터치 시
            // 기존 상태, 비밀번호 or 생체인식 or 설정안함 유지
            setLoginRadioButton()
        }
        builder.setOnCancelListener {
            // 배경, 뒤로가기 터치 시
            // 기존 상태, 비밀번호 or 생체인식 or 설정안함 유지
            setLoginRadioButton()
        }
        builder.setTitle(getString(R.string.login_setting_dialog_title))
        builder.setMessage(getString(R.string.login_with_password))
        builder.create().show()
    }

    // 생체 인식 잠금
    private fun setBiometric() {
        with(binding) {
            radioButtonForPassword.isChecked = false
            radioButtonForNothing.isChecked = false
        }
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setPositiveButton(getString(R.string.positive_button)) { _, _ ->
            // 생체 인식 설정 시, 작동코드
            // 생체 인식 가능한지 확인
            authenticateToEncrypt()
        }
        builder.setNegativeButton(getString(R.string.negative_button)) { _, _ ->
            // 아니오, 터치 시
            // 기존 상태, 비밀번호 or 생체인식 or 설정안함 유지
            setLoginRadioButton()
        }
        builder.setOnCancelListener {
            // 배경, 뒤로가기 터치 시
            // 기존 상태, 비밀번호 or 생체인식 or 설정안함 유지
            setLoginRadioButton()
        }
        builder.setTitle(getString(R.string.login_setting_dialog_title))
        builder.setMessage(getString(R.string.login_with_bio))
        builder.create().show()
    }

    // 잠금 해제
    private fun setLockout() {
        with(binding) {
            radioButtonForPassword.isChecked = false
            radioButtonForBio.isChecked = false
        }
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setPositiveButton(getString(R.string.positive_button)) { _, _ ->
            // 생체인식 설정 시, 작동코드
            MyApplication.prefs.removeString(LOGIN_TYPE)
            Toast.makeText(
                requireContext(),
                getString(R.string.login_with_nothing_setting),
                Toast.LENGTH_SHORT
            ).show()
            setLoginRadioButton()
        }
        builder.setNegativeButton(getString(R.string.negative_button)) { _, _ ->
            // 아니오, 터치 시
            // 기존 상태, 비밀번호 or 생체인식 or 설정안함 유지
            setLoginRadioButton()
        }
        builder.setOnCancelListener {
            // 배경, 뒤로가기 터치 시
            // 기존 상태, 비밀번호 or 생체인식 or 설정안함 유지
            setLoginRadioButton()
        }
        builder.setTitle(getString(R.string.login_setting_dialog_title))
        builder.setMessage(getString(R.string.login_with_nothing))
        builder.create().show()
    }


    private fun setThemeAndRestartApp(type: String) {
        settingViewModel.setThemeAndRestartApp(type)
    }

    // 테마 적용 후 재시작
    private fun restartApp() {
        var theme = ""
        when (MyApplication.prefs.getString(THEME_TYPE, THEME_BASIC)) {
            THEME_BASIC -> theme = getString(R.string.theme_basic)
            THEME_1 -> theme = getString(R.string.theme_1)
            THEME_2 -> theme = getString(R.string.theme_2)
        }

        val intent = Intent(requireContext(), SplashActivity::class.java)
        requireActivity().finishAffinity()
        startActivity(intent)
        requireActivity().overridePendingTransition(0,1)
        Toast.makeText(requireContext(), getString(R.string.theme_apply_finished, theme),Toast.LENGTH_SHORT).show()
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

    // 백업 Dialog 로 이동 및 백업(자동 실행)
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
                binding.numberOfRecordDays.text =
                    getString(R.string.numberOfRecordedDate, recordedDateNumber)
            }
        }
    }

    // 생체 인증 가능여부 확인 및 대처
    private fun authenticateToEncrypt() {
        val biometricManager = BiometricManager.from(requireContext())
        // BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                MyApplication.prefs.setString(LOGIN_TYPE, LOGIN_WITH_BIO)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.login_with_bio_setting),
                    Toast.LENGTH_SHORT
                ).show()
                setLoginRadioButton()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.login_with_bio_setting_ERROR_NO_HARDWARE),
                    Toast.LENGTH_SHORT
                ).show()
                setLoginRadioButton()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
                Toast.makeText(
                    requireContext(),
                    getString(R.string.login_with_bio_setting_ERROR_HW_UNAVAILABLE),
                    Toast.LENGTH_SHORT
                ).show()
                setLoginRadioButton()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.BIOMETRIC_ERROR_NONE_ENROLLED),
                    Toast.LENGTH_SHORT
                ).show()
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                }
                loginLauncher.launch(enrollIntent)
            }
        }
    }

    private val loginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                authenticateToEncrypt()  //생체 인증 가능 여부확인 다시 호출
            }
        }

    override fun onResume() {
        super.onResume()
        setLoginRadioButton()
        setThemeRadioButton()
    }
}