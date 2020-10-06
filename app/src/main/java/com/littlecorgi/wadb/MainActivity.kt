package com.littlecorgi.wadb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.littlecorgi.wadb.Utilities.noOpDelegate
import com.littlecorgi.wadb.Utilities.setServicePort

class MainActivity : AppCompatActivity() {

    private lateinit var mSwState: SwitchCompat
    private lateinit var mTvInfo: TextView
    private lateinit var mTvPort: TextView
    private val mReceiver = StateReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // todo 首页密码解锁
        // toSplash
        // startActivity(Intent(this, SplashActivity::class.java))

        lifecycle.addObserver(Lifecycle())
        mSwState = findViewById(R.id.sw_state)
        mTvInfo = findViewById(R.id.tv_info)
        mTvPort = findViewById(R.id.tv_port)
        mSwState.setOnCheckedChangeListener { _, isChecked ->
            if (Utilities.setWadbState(isChecked)) {
                setChecked(isChecked)
            }
        }
        mTvPort.setOnClickListener {
            val inflate = layoutInflater
            val view = inflate.inflate(R.layout.layout_input_service_port, null)
            view.findViewById<AppCompatEditText>(R.id.et_port).addTextChangedListener(object : TextWatcher by noOpDelegate() {
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val str = s?.toString() ?: "5419"
                    val port = str.toInt()
                    setServicePort(port)
                    mTvPort.text = str
                }
            })
            AlertDialog.Builder(this)
                    .setView(view)
                    .setTitle(R.string.input_service_port)
                    .setPositiveButton(R.string.ok) { dialog, _ -> dialog.cancel() }
                    .create()
                    .show()
        }
        val pref = getSharedPreferences("MainActivity", Context.MODE_PRIVATE)
        val autoRun = findViewById<SwitchCompat>(R.id.sw_autoRun)
        autoRun.setOnCheckedChangeListener { _, isChecked ->
            pref.edit().putBoolean(Utilities.PREF_AUTO_RUN, isChecked).apply()
        }
        autoRun.isChecked = pref.getBoolean(Utilities.PREF_AUTO_RUN, false)
        registerReceiver(mReceiver, IntentFilter(Utilities.INTENT_ACTION_ADB_STATE))
    }

    private fun setChecked(checked: Boolean) {
        mSwState.isChecked = checked
        mSwState.setText(if (checked) R.string.enabled else R.string.disabled)
        mTvInfo.text = if (checked)
            getString(R.string.help) + getString(R.string.help_text, Utilities.getIpAddress(this), Utilities.getServicePort()) else getString(R.string.help)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        AlertDialog.Builder(this)
                .setTitle(R.string.about)
                .setMessage(R.string.about_text)
                .setNegativeButton(R.string.open_source) { _, _ -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/a1203991686/WADB"))) }
                .setNeutralButton(R.string.open_source_yaerin) { _, _ -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Yaerin/WADB"))) }
                .setPositiveButton(R.string.ok) { dialog, _ -> dialog.cancel() }
                .create()
                .show()
        return true
    }

    inner class StateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            setChecked(Utilities.isActivated())
        }
    }

    inner class Lifecycle : LifecycleObserver {
        @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_RESUME)
        fun onResume() {
            Thread {
                val activated = Utilities.isActivated()
                runOnUiThread { setChecked(activated) }
            }.start()
        }

        @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            unregisterReceiver(mReceiver)
            // 当软件被清除时自动关闭网络ADB
            Utilities.setWadbState(false)
        }
    }
}