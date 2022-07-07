package com.dreamreco.joodiary.ui.login

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class ScreenReceiverWithBio : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            val i = Intent(context, BioScreenLockActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }
}

class ScreenReceiverWithPassword : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            val i = Intent(context, PasswordScreenLockActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }
}