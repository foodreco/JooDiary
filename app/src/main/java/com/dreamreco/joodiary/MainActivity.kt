package com.dreamreco.joodiary

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.dreamreco.joodiary.databinding.ActivityMainBinding
import com.dreamreco.joodiary.ui.calendar.CalendarViewModel
import com.dreamreco.joodiary.ui.login.Actions
import com.dreamreco.joodiary.ui.login.BioScreenLockActivity
import com.dreamreco.joodiary.ui.login.PasswordScreenLockActivity
import com.dreamreco.joodiary.ui.login.ScreenService
import com.dreamreco.joodiary.util.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Field


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val calendarViewModel by viewModels<CalendarViewModel>()
    private var typeface: Typeface? = null

    // 앱 테마 색상 정하기
    // 레이아웃 weight 기준으로 재설계하기

    override fun onStart() {
        super.onStart()
        // 보안 설정 시, 액티비티 이동
        when (MyApplication.prefs.getString(LOGIN_TYPE, LOGIN_WITH_NOTHING)) {
            // 패스워드로 설정 된 경우,
            LOGIN_WITH_PASSWORD -> {
                when (MyApplication.prefs.getString(LOGIN_STATE, LOGIN_NOT_CONFIRM)) {
                    // 로그인이 된 경우,
                    LOGIN_CLEAR -> {
                    }
                    // 로그인이 안된 경우
                    LOGIN_NOT_CONFIRM -> {
                        if (MyApplication.prefs.getString(
                                PASSWORD_KEY,
                                NO_REGISTERED_PASSWORD
                            ) != NO_REGISTERED_PASSWORD
                        ) {
                            // 등록된 비밀번호가 존재할때만, 비밀번호 잠금 띄움
                            val intent = Intent(this, PasswordScreenLockActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            }
            // 생체 인식으로 설정 된 경우,
            LOGIN_WITH_BIO -> {
                when (MyApplication.prefs.getString(LOGIN_STATE, LOGIN_NOT_CONFIRM)) {
                    // 로그인이 된 경우,
                    LOGIN_CLEAR -> {
                    }
                    // 로그인이 안된 경우
                    LOGIN_NOT_CONFIRM -> {
                        val intent = Intent(this, BioScreenLockActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            // 보안 설정이 안 된 경우,
            LOGIN_WITH_NOTHING -> {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 테마 설정 코드
        when (MyApplication.prefs.getString(THEME_TYPE, THEME_BASIC)) {
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
            }
        }


        FontOverride.setDefaultFont(this, "monospace", "binggraemelona_regular.ttf")

        setFont("monospace","binggraemelona_regular.ttf")




        super.onCreate(savedInstanceState)
        setContentView(binding.root)


//        if (typeface == null) {
//            typeface = Typeface.createFromAsset(this.assets, "binggraemelona_regular.ttf");
//        }
//
//        setGlobalFont(window.decorView)


        // 신규 코드
        val navHostFragment =
            supportFragmentManager.findFragmentById(com.dreamreco.joodiary.R.id.MyNavHostFragment) as NavHostFragment

        val navController = navHostFragment.navController

        val navView: BottomNavigationView =
            findViewById(com.dreamreco.joodiary.R.id.bottom_nav)

        NavigationUI.setupWithNavController(navView, navController)

        // 시작점 Fragment 외에는 bottomNav 표시 안함
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == com.dreamreco.joodiary.R.id.calenderFragment || destination.id == com.dreamreco.joodiary.R.id.listFragment || destination.id == com.dreamreco.joodiary.R.id.statisticsFragment || destination.id == com.dreamreco.joodiary.R.id.settingFragment) {
                binding.bottomNav.visibility = View.VISIBLE
            } else {
                binding.bottomNav.visibility = View.GONE
            }
        }

        // 앱이 시작할 때마다, calendarDate = Today 로 업데이트
        calendarViewModel.calendarDateReset()
    }

    override fun onStop() {
        super.onStop()
        // 잠금화면(폰 전원 off) 일 때, 보안 처리
        // stop 에서 처리해줘야 함
        // ACTION_SCREEN_OFF 로 감지해서 onCreate -> onStart 를 거치지 않고 바로 작동되기 때문.
        when (MyApplication.prefs.getString(LOGIN_TYPE, LOGIN_WITH_NOTHING)) {
            // 패스워드로 설정 된 경우,
            LOGIN_WITH_PASSWORD -> {
                // 기본 잠금화면 설정하기
                // 화면 off 시 잠금화면 팝업
                val intent = Intent(applicationContext, ScreenService::class.java)
                intent.action = Actions.PASSWORD
                startService(intent)
            }
            // 생체 인식으로 설정 된 경우,
            LOGIN_WITH_BIO -> {
                // 기본 잠금화면 설정하기
                // 화면 off 시 잠금화면 팝업
                val intent = Intent(applicationContext, ScreenService::class.java)
                intent.action = Actions.BIO
                startService(intent)
            }
            // 보안 설정이 안 된 경우,
            LOGIN_WITH_NOTHING -> {
                val intent = Intent(applicationContext, ScreenService::class.java)
                stopService(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        MyApplication.prefs.setString(LOGIN_STATE, LOGIN_NOT_CONFIRM)

        // 앱이 완전 종료될 때는 스크린 서비스 종료(leak 방지)
        val intent = Intent(applicationContext, ScreenService::class.java)
        stopService(intent)
    }




    private fun setGlobalFont(view: View?) {
        Log.e("메인 액티비티","setGlobalFont")
        if (view != null) {
            if (view is ViewGroup) {
                val vgCnt = view.childCount
                for (i in 0 until vgCnt) {
                    val v = view.getChildAt(i)
                    if (v is TextView) {
                        v.typeface = typeface
                    }
                    setGlobalFont(v)
                }
            }
        }
    }

    fun setFont(defaultFontNameToOverride: String, newFontFileNameInAssets: String): Boolean {
        try {
            Log.e("메인 액티비티","setFont")
            val fontMaintext = Typeface.createFromAsset(this.assets, newFontFileNameInAssets)
            val defaultFontTypefaceField: Field = Typeface::class.java.getDeclaredField(defaultFontNameToOverride)
            defaultFontTypefaceField.isAccessible = true
            defaultFontTypefaceField.set(null, fontMaintext)
        } catch (e: java.lang.Exception) {
            Log.e("메인 액티비티","catch")
            return false
        }
        return true
    }
}

object FontOverride {
    /** 기본 폰트 셋팅  */
    fun setDefaultFont(context: Context, staticTypefaceFieldName: String?, fontAssetName: String?) {
        Log.e("메인 액티비티","setDefaultFont")
        val regular = Typeface.createFromAsset(context.getAssets(), fontAssetName)
        replaceFont(staticTypefaceFieldName, regular)
    }

    /** 기존의 폰트에서 설정한 폰트로 설정  */
    internal fun replaceFont(staticTypefaceFieldName: String?, newTypeface: Typeface?) {
        try {
            Log.e("메인 액티비티","replaceFont")
            val staticField = Typeface::class.java.getDeclaredField(staticTypefaceFieldName)
            staticField.isAccessible = true
            staticField.set(null,newTypeface)
        } catch (e: NoSuchFieldException) {
            Log.e("메인 액티비티","NoSuchFieldException")
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            Log.e("메인 액티비티","IllegalAccessException")
            e.printStackTrace()
        }
    }
}