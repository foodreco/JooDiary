package com.dreamreco.joodiary

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.dreamreco.joodiary.util.THEME_1
import com.dreamreco.joodiary.util.THEME_2
import com.dreamreco.joodiary.util.THEME_BASIC
import com.dreamreco.joodiary.util.THEME_TYPE

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        },1500)
    }

}