package com.littlecorgi.wadb

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Handler
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.littlecorgi.wadb.Utilities.INTENT_ACTION_ADB_STATE
import com.littlecorgi.wadb.Utilities.getIpAddress
import com.littlecorgi.wadb.Utilities.isActivated
import com.littlecorgi.wadb.Utilities.setWadbState

@RequiresApi(Build.VERSION_CODES.N)
class TileService : TileService() {

    override fun onTileAdded() {
        updateTile()
    }

    override fun onStartListening() {
        updateTile()
    }

    override fun onClick() {
        qsTile.state = Tile.STATE_UNAVAILABLE
        qsTile.updateTile()
        setWadbState(!isActivated())
        Handler().postDelayed({
            updateTile()
            sendBroadcast(Intent(INTENT_ACTION_ADB_STATE))
        }, 500)
    }

    private fun updateTile() {
        val b: Boolean = isActivated()
        qsTile.state = if (b) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.icon = if (b) Icon.createWithResource(this, R.drawable.ic_qs_network_adb_on) else Icon.createWithResource(this, R.drawable.ic_qs_network_adb_off)
        qsTile.label = if (b) getIpAddress(this) else getString(R.string.app_name)
        qsTile.updateTile()
    }
}
