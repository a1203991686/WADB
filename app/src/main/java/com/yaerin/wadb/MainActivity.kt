package com.yaerin.wadb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.yaerin.wadb.Utilities.setServicePort

class MainActivity : AppCompatActivity() {

    private lateinit var mSwitchCompat: SwitchCompat
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
        mSwitchCompat = findViewById(R.id.sw_state)
        mTvInfo = findViewById(R.id.tv_info)
        mTvPort = findViewById(R.id.tv_port)
        mSwitchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (Utilities.setWadbState(isChecked)) {
                setChecked(isChecked)
            }
        }
        mTvPort.setOnClickListener {
            val inflate = layoutInflater
            val view = inflate.inflate(R.layout.layout_input_service_port, null)
            view.findViewById<AppCompatButton>(R.id.btn_sure_port).setOnClickListener {
                val str = view.findViewById<AppCompatEditText>(R.id.et_port).text?.toString()
                        ?: "5419"
                val port = str.toInt()
                mTvPort.text = str
                setServicePort(port)
            }
            AlertDialog.Builder(this)
                    .setView(view)
                    .setTitle(R.string.input_service_port)
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
        mSwitchCompat.isChecked = checked
        mSwitchCompat.setText(if (checked) R.string.enabled else R.string.disabled)
        mTvInfo.text = if (checked)
            getString(R.string.help) + getString(R.string.help_text, Utilities.getIpAddress(this), Utilities.getServicePort()) else getString(R.string.help)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setTitle(R.string.about)
                .setMessage(R.string.about_text)
                .setNegativeButton(R.string.open_source) { _, _ -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Yaerin/WADB"))) }
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
        }
    }
}