package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class VolumeBoosterService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): VolumeBoosterService = this@VolumeBoosterService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val boostLevel = intent?.getIntExtra(EXTRA_BOOST_LEVEL, 100) ?: 100
        val isEnabled = intent?.getBooleanExtra(EXTRA_IS_ENABLED, true) ?: true

        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createNotification(boostLevel, isEnabled)
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Volume Booster Active Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps audio volume booster & equalizer active in background"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(boostLevel: Int, isEnabled: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, VolumeBoosterService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isEnabled) "Volume Booster: ACTIVE ($boostLevel%)" else "Volume Booster: Disabled"
        val text = "Tap to adjust volume, bass boost, and equalizer presets"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Turn Off",
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "volume_booster_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_BOOST_LEVEL = "extra_boost_level"
        const val EXTRA_IS_ENABLED = "extra_is_enabled"
        const val ACTION_STOP_SERVICE = "action_stop_service"

        fun startService(context: Context, boostLevel: Int, isEnabled: Boolean) {
            val intent = Intent(context, VolumeBoosterService::class.java).apply {
                putExtra(EXTRA_BOOST_LEVEL, boostLevel)
                putExtra(EXTRA_IS_ENABLED, isEnabled)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, VolumeBoosterService::class.java)
            context.stopService(intent)
        }
    }
}
