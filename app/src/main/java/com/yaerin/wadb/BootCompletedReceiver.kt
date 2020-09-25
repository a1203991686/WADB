package com.yaerin.wadb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.yaerin.wadb.Utilities.PREF_AUTO_RUN
import com.yaerin.wadb.Utilities.getIpAddress
import com.yaerin.wadb.Utilities.getServicePort
import com.yaerin.wadb.Utilities.setWadbState

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pref = context.getSharedPreferences("AutoRun", Context.MODE_PRIVATE)
        if (Intent.ACTION_BOOT_COMPLETED == intent.action && pref.getBoolean(PREF_AUTO_RUN, false)) {
            if (setWadbState(true)) {
                val text = context.getString(R.string.adb_running, getIpAddress(context), getServicePort())
                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }
        }
    }
}
