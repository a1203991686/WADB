package com.littlecorgi.wadb

import android.content.Context
import android.net.wifi.WifiManager
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.*

/**
 *
 * @author littlecorgi 2020/9/24
 */
object Utilities {

    const val PREF_AUTO_RUN = "auto_run"
    const val INTENT_ACTION_ADB_STATE = "com.littlecorgi.intent.ADB_STATE"
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
        return run {
            val i = wm.connectionInfo.ipAddress
            java.lang.String.format(Locale.getDefault(), "%d.%d.%d.%d",
                    i and 0xFF, i shr 8 and 0xFF, i shr 16 and 0xFF, i shr 24 and 0xFF)
        }
    }

    fun getServicePort(): String? {
        return ServicePort
    }

    fun setServicePort(a: Int) {
        ServicePort = a.toString()
    }

    /**
     * Android 开启WI-FI调试的3种方式: https://www.jianshu.com/p/790b29b80117
     */
    fun setWadbState(enabled: Boolean): Boolean {
        try {
            // 执行su脚本，获取权限
            val process = Runtime.getRuntime().exec("su")
            // 获取输出流
            val os = DataOutputStream(process.outputStream)
            if (enabled) {
                // 启动adb
                os.writeBytes(java.lang.String.format("setprop service.adb.tcp.port %s\n", getServicePort()))
            } else {
                // 取消adb
                os.writeBytes("setprop service.adb.tcp.port -1\n")
            }
            // 重启adb
            os.writeBytes("stop adbd\n")
            os.writeBytes("start adbd\n")
            os.flush()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 使用动态代理，让接口也可以选择性的实现方法，用以缩短代码长度，减少不必要的方法
     */
    inline fun <reified T> noOpDelegate(): T {
        val javaClass = T::class.java
        val noOpHandler = InvocationHandler { _, _, _ ->
            // no op
        }
        return Proxy.newProxyInstance(javaClass.classLoader,
                arrayOf(javaClass), noOpHandler) as T
    }
}