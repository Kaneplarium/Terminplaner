package com.terminplaner.widget

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.TileService
import com.terminplaner.MainActivity

class QuickTaskTileService : TileService() {
    override fun onClick() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = "ACTION_QUICK_TASK"
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        startActivityAndCollapse(pendingIntent)
    }
}
