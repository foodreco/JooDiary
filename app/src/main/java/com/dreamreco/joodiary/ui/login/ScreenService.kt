package com.dreamreco.joodiary.ui.login

import android.app.Service
import android.content.BroadcastReceiver

import android.content.Intent

import android.content.IntentFilter

import android.os.IBinder
import android.util.Log


class ScreenService : Service() {
    private var mReceiver: BroadcastReceiver? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
//        mReceiver = ScreenReceiverWithBio()
//        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
//        registerReceiver(mReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            Actions.BIO -> {
                if (mReceiver != null) {
                    unregisterReceiver(mReceiver)
                }
                mReceiver = ScreenReceiverWithBio()
                val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
                registerReceiver(mReceiver, filter)
            }
            Actions.PASSWORD -> {
                if (mReceiver != null) {
                    unregisterReceiver(mReceiver)
                }
                mReceiver = ScreenReceiverWithPassword()
                val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
                registerReceiver(mReceiver, filter)
            }
        }
//        if (intent != null) {
//            if (intent.action == null) {
//                if (mReceiver == null) {
//                    mReceiver = ScreenReceiver()
//                    val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
//                    registerReceiver(mReceiver, filter)
//                }
//            }
//        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mReceiver != null) {
            unregisterReceiver(mReceiver)
        }
    }
}

object Actions {
    const val BIO = "Bio"
    const val PASSWORD = "Password"
}