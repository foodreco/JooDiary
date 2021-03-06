package com.dreamreco.joodiary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.R
import com.dreamreco.joodiary.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

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

    }
}