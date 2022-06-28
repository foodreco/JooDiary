package com.dreamreco.joodiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.R
import com.dreamreco.joodiary.databinding.ActivityMainBinding
import com.dreamreco.joodiary.ui.calendar.CalendarViewModel
import com.dreamreco.joodiary.ui.login.ScreenLockActivity
import com.dreamreco.joodiary.util.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val calendarViewModel by viewModels<CalendarViewModel>()

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
                        val intent = Intent(this, ScreenLockActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            // 보안 설정이 안 된 경우,
            LOGIN_WITH_NOTHING -> {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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

    override fun onPause() {
        super.onPause()
        Log.e("메인액티비티","onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.e("메인액티비티","onStop")
        MyApplication.prefs.setString(LOGIN_STATE, LOGIN_NOT_CONFIRM)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("메인액티비티","onDestroy")
        MyApplication.prefs.setString(LOGIN_STATE, LOGIN_NOT_CONFIRM)
    }
}