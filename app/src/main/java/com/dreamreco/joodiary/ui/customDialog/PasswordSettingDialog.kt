package com.dreamreco.joodiary.ui.customDialog

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.PasswordSettingDialogBinding
import com.dreamreco.joodiary.ui.setting.SettingViewModel
import com.dreamreco.joodiary.util.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PasswordSettingDialog : DialogFragment() {

    private val settingViewModel by viewModels<SettingViewModel>()
    private val binding by lazy { PasswordSettingDialogBinding.inflate(layoutInflater) }
    private var reWritePassword = false
    private var focusState = 0

    private var passwordNumber = ""
    private var firstPassword = ""
    private var secondPassword = ""

    private var typeface: Typeface? = null
    private var themeDarkState = false

    // 뒤로 가기 처리를 위한 콜백 변수
    private lateinit var callback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        typeface = getFontType(requireContext())
        typeface?.let { setGlobalFont(binding.root, it) }

        if (getThemeType() == THEME_2) {
            themeDarkState = true
            binding.viewNumber1.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
            binding.viewNumber2.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
            binding.viewNumber3.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
            binding.viewNumber4.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)

            binding.passwordToolbarExitButton.imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.white)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        backButtonCancel()
        with(binding) {
            val textViewArray = arrayListOf<View>(
                number0,
                number1,
                number2,
                number3,
                number4,
                number5,
                number6,
                number7,
                number8,
                number9,
                textDeleteAll,
            )
            for (numberButton in textViewArray) {
                numberButton.setOnClickListener(btnListener)
            }
            guideText.text = getString(R.string.write_password)
        }

        with(settingViewModel) {
            // 비밀번호 등록 완료 후, 이전으로 돌아가는 코드
            passwordRegisterDone.observe(viewLifecycleOwner) { done ->
                if (done) {
                    findNavController().navigateUp()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.login_with_password_setting),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        // 1. 툴바 관련 코드
        with(binding) {
            // #1 Top Toolbar
            toolbarTitleTextView.text = getString(R.string.login_setting_dialog_title)

            passwordToolbarExitButton.setOnClickListener {
                findNavController().navigateUp()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.login_with_password_setting_cancel),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        return binding.root
    }
    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간
    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간
    //함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간//함수구간

    // 버튼 클릭 했을때
    private val btnListener = View.OnClickListener { view ->
        var currentValue = -1
        when (view.id) {
            R.id.number0 -> currentValue = 0
            R.id.number1 -> currentValue = 1
            R.id.number2 -> currentValue = 2
            R.id.number3 -> currentValue = 3
            R.id.number4 -> currentValue = 4
            R.id.number5 -> currentValue = 5
            R.id.number6 -> currentValue = 6
            R.id.number7 -> currentValue = 7
            R.id.number8 -> currentValue = 8
            R.id.number9 -> currentValue = 9
            R.id.text_delete_all -> onClear()
        }

        with(binding) {

            if (currentValue != -1) {

                val strCurrentValue = currentValue.toString() // 현재 입력된 번호 String 으로 변경
                passwordNumber += strCurrentValue

                when (focusState) {
                    0 -> {
                        binding.viewNumber1.apply {
                            setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_circle
                                )
                            )
                            if (themeDarkState) {
                                imageTintList =
                                    ContextCompat.getColorStateList(requireContext(), R.color.white)
                            }
                        }
                        focusState += 1
                    }
                    1 -> {
                        binding.viewNumber2.apply {
                            setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_circle
                                )
                            )
                            if (themeDarkState) {
                                imageTintList =
                                    ContextCompat.getColorStateList(requireContext(), R.color.white)
                            }
                        }
                        focusState += 1
                    }
                    2 -> {
                        binding.viewNumber3.apply {
                            setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_circle
                                )
                            )
                            if (themeDarkState) {
                                imageTintList =
                                    ContextCompat.getColorStateList(requireContext(), R.color.white)
                            }
                        }
                        focusState += 1
                    }
                    3 -> {
                        binding.viewNumber4.apply {
                            setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.ic_circle
                                )
                            )
                            if (themeDarkState) {
                                imageTintList =
                                    ContextCompat.getColorStateList(requireContext(), R.color.white)
                            }
                        }
                        // 비밀번호 4자리 모두 입력시
                        if (!reWritePassword) {
                            // 첫번째 입력인 경우,
                            firstPassword = passwordNumber
                            onClear()
                            reWritePassword = true
                            binding.guideText.text = getString(R.string.password_rewrite)
                        } else {
                            // 두번째 입력인 경우,
                            secondPassword = passwordNumber
                            if (firstPassword == secondPassword) {
                                // 1,2 차 입력이 같아야 비밀번호 등록
                                settingViewModel.passwordRegister(firstPassword)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.second_password_discordant),
                                    Toast.LENGTH_SHORT
                                ).show()
                                onClear()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onClear() {
        focusState = 0
        passwordNumber = ""
        with(binding) {
            viewNumber1.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_outline_circle
                    )
                )
                if (themeDarkState) {
                    imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
                }
            }
            viewNumber2.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_outline_circle
                    )
                )
                if (themeDarkState) {
                    imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
                }
            }
            viewNumber3.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_outline_circle
                    )
                )
                if (themeDarkState) {
                    imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
                }
            }
            viewNumber4.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_outline_circle
                    )
                )
                if (themeDarkState) {
                    imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
                }
            }
        }
    }

    private fun backButtonCancel() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뒤로가기 및 토스트
                findNavController().navigateUp()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.login_with_password_setting_cancel),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
}








