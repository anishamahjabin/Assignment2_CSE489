package com.example.cse489_assignment2.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CustomMessageReceiver(
    private val onMessageReceived: (String) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != BroadcastContract.ACTION_CUSTOM_MESSAGE) return
        onMessageReceived(intent.getStringExtra(BroadcastContract.EXTRA_MESSAGE).orEmpty())
    }
}
