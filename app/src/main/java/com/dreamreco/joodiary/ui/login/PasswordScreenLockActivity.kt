package com.dreamreco.joodiary.ui.login

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dreamreco.joodiary.MainActivity
import com.dreamreco.joodiary.MyApplication
import com.dreamreco.joodiary.R
import com.dreamreco.joodiary.databinding.ActivityPasswordScreenLockBinding
import com.dreamreco.joodiary.util.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordScreenLockActivity : AppCompatActivity() {

    private val binding by lazy { ActivityPasswordScreenLockBinding.inflate(layoutInflater) }
    private var focusState = 0

    // 복호화 진행??
    private var passwordNumber = ""

    // 등록된 비밀번호
    private val registeredPassword =
        MyApplication.prefs.getString(PASSWORD_KEY, NO_REGISTERED_PASSWORD)

    private var typeface: Typeface? = null

    private var themeDarkState = false


    override fun onCreate(savedInstanceState: Bundle?) {
        // 테마 설정 코드
        when (getThemeType()) {
            // 기본 테마
            THEME_BASIC -> {
                setTheme(R.style.Theme_JooDiary)
            }
            // 테마 1
            THEME_1 -> {
                setTheme(R.style.NewCustomAppTheme)
            }
            // 테마 2
            THEME_2 -> {
                setTheme(R.style.SoundCustomAppTheme)
                themeDarkState = true

                with(binding) {
                    viewNumber1.imageTintList = ContextCompat.getColorStateList(
                        this@PasswordScreenLockActivity,
                        R.color.white
                    )
                    viewNumber2.imageTintList = ContextCompat.getColorStateList(
                        this@PasswordScreenLockActivity,
                        R.color.white
                    )
                    viewNumber3.imageTintList = ContextCompat.getColorStateList(
                        this@PasswordScreenLockActivity,
                        R.color.white
                    )
                    viewNumber4.imageTintList = ContextCompat.getColorStateList(
                        this@PasswordScreenLockActivity,
                        R.color.white
                    )

                    passwordToolbarLogo.imageTintList = ContextCompat.getColorStateList(
                        this@PasswordScreenLockActivity,
                        R.color.white
                    )
                    passwordToolbarExitButton.imageTintList = ContextCompat.getColorStateList(
                        this@PasswordScreenLockActivity,
                        R.color.white
                    )
                }
            }
        }


        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        typeface = getFontType(this)
        setGlobalFont(binding.root, typeface!!)

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

            passwordToolbarExitButton.setOnClickListener {
                finishAffinity()
            }
        }
    }

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
                                    this@PasswordScreenLockActivity,
                                    R.drawable.ic_circle
                                )
                            )
                            if (themeDarkState) {
                                imageTintList = ContextCompat.getColorStateList(
                                    this@PasswordScreenLockActivity,
                                    R.color.white
                                )
                            }
                        }
                        focusState += 1
                    }
                    1 -> {
                        binding.viewNumber2.apply {
                            setImageDrawable(
                                ContextCompat.getDrawable(
                                    this@PasswordScreenLockActivity,
                                    R.drawable.ic_circle
                                )
                            )
                            if (themeDarkState) {
                                imageTintList = ContextCompat.getColorStateList(
                                    this@PasswordScreenLockActivity,
                                    R.color.white
                                )
                            }
                        }
                        focusState += 1
                    }
                    2 -> {
                        binding.viewNumber3.apply {
                            setImageDrawable(
                                ContextCompat.getDrawable(
                                    this@PasswordScreenLockActivity,
                                    R.drawable.ic_circle
                                )
                            )
                            if (themeDarkState) {
                                imageTintList = ContextCompat.getColorStateList(
                                    this@PasswordScreenLockActivity,
                                    R.color.white
                                )
                            }
                        }
                        focusState += 1
                    }
                    3 -> {
                        binding.viewNumber4.apply {
                            setImageDrawable(
                                ContextCompat.getDrawable(
                                    this@PasswordScreenLockActivity,
                                    R.drawable.ic_circle
                                )
                            )
                            if (themeDarkState) {
                                imageTintList = ContextCompat.getColorStateList(
                                    this@PasswordScreenLockActivity,
                                    R.color.white
                                )
                            }
                        }
                        // 비밀번호 4자리 모두 입력시
                        if (registeredPassword == passwordNumber) {
                            // 등록된 비밀번호와 일치하는 경우,
                            // 로그인 성공
                            MyApplication.prefs.setString(LOGIN_STATE, LOGIN_CLEAR)
                            val intent =
                                Intent(this@PasswordScreenLockActivity, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            // 비밀번호가 일치하지 않을 때,
                            Toast.makeText(
                                this@PasswordScreenLockActivity,
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

    private fun onClear() {
        focusState = 0
        passwordNumber = ""
        with(binding) {
            viewNumber1.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        this@PasswordScreenLockActivity,
                        R.drawable.ic_outline_circle
                    )
                )
                if (themeDarkState) {
                    imageTintList = ContextCompat.getColorStateList(
                        this@PasswordScreenLockActivity,
                        R.color.white
                    )
                }
            }
            viewNumber2.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        this@PasswordScreenLockActivity,
                        R.drawable.ic_outline_circle
                    )
                )
                if (themeDarkState) {
                    imageTintList = ContextCompat.getColorStateList(
                        this@PasswordScreenLockActivity,
                        R.color.white
                    )
                }
            }
            viewNumber3.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        this@PasswordScreenLockActivity,
                        R.drawable.ic_outline_circle
                    )
                )
                if (themeDarkState) {
                    imageTintList = ContextCompat.getColorStateList(
                        this@PasswordScreenLockActivity,
                        R.color.white
                    )
                }
            }
            viewNumber4.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        this@PasswordScreenLockActivity,
                        R.drawable.ic_outline_circle
                    )
                )
                if (themeDarkState) {
                    imageTintList = ContextCompat.getColorStateList(
                        this@PasswordScreenLockActivity,
                        R.color.white
                    )
                }
            }
        }
    }


    // 로그인 하지 않고, 뒤로가기를 눌렀을 때, 동작하는 코드
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}

