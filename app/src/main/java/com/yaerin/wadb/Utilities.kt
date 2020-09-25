package com.yaerin.wadb

import android.content.Context
import android.net.wifi.WifiManager
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 *
 * @author littlecorgi 2020/9/24
 */
object Utilities {

    const val PREF_AUTO_RUN = "auto_run"
    const val INTENT_ACTION_ADB_STATE = "com.yaerin.intent.ADB_STATE"
    private var ServicePort = "5419"

    fun isActivated(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("getprop service.adb.tcp.port\n")
            os.flush()
            os.close()
            val reader = InputStreamReader(process.inputStream)
            val chars = CharArray(5)
            reader.read(chars)
            reader.close()
            process.destroy()
            val result = String(chars)
            result.matches(Regex("[0-9]+\\n")) && !result.contains("-1")
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun getIpAddress(context: Context): String? {
        val wm = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
        return if (wm != null) {
            val i = wm.connectionInfo.ipAddress
            java.lang.String.format(Locale.getDefault(), "%d.%d.%d.%d",
                    i and 0xFF, i shr 8 and 0xFF, i shr 16 and 0xFF, i shr 24 and 0xFF)
        } else {
            "0.0.0.0"
        }
    }

    fun getServicePort(): String? {
        return ServicePort
    }

    fun setServicePort(a: Int) {
        ServicePort = a.toString()
    }

    fun setWadbState(enabled: Boolean): Boolean {
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            if (enabled) {
                os.writeBytes(java.lang.String.format("setprop service.adb.tcp.port %s\n", getServicePort()))
            } else {
                os.writeBytes("setprop service.adb.tcp.port -1\n")
            }
            os.writeBytes("stop adbd\n")
            os.writeBytes("start adbd\n")
            os.flush()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }
}