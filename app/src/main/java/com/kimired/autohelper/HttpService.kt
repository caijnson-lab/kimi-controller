package com.kimired.autohelper

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class HttpService : Service() {

    companion object {
        const val TAG = "HttpService"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "kimi_controller_channel"
        
        var isRunning = false
            private set
    }

    private var httpServer: Any? = null  // HTTP 服务器实例

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "HTTP 服务创建")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        
        when (action) {
            "START" -> {
                Log.i(TAG, "启动 HTTP 服务")
                startHttpServer()
            }
            "STOP" -> {
                Log.i(TAG, "停止 HTTP 服务")
                stopHttpServer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                // 默认启动
                startHttpServer()
            }
        }
        
        return START_STICKY  // 服务被杀后自动重启
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "HTTP 服务销毁")
        stopHttpServer()
        isRunning = false
    }

    // 创建通知渠道（Android 8.0+）
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kimi Controller 服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "控制 Kimi Controller 后台服务"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 创建通知
    private fun createNotification(): Notification {
        // 点击通知打开主应用
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 停止服务
        val stopIntent = Intent(this, HttpService::class.java).apply {
            action = "STOP"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 创建通知
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kimi Controller")
            .setContentText("HTTP 服务运行中 - 端口 8082")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "停止服务",
                stopPendingIntent
            )
            .setOngoing(true)
            .build()
    }

    // 启动 HTTP 服务器
    private fun startHttpServer() {
        try {
            // TODO: 启动实际的 HTTP 服务器
            Log.i(TAG, "HTTP 服务器已启动 - 端口 8082")
            
            // 更新通知
            val notification = createNotification().apply {
                setContentText("HTTP 服务运行中 - 端口 8082")
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, notification)
            
        } catch (e: Exception) {
            Log.e(TAG, "启动 HTTP 服务器失败", e)
        }
    }

    // 停止 HTTP 服务器
    private fun stopHttpServer() {
        try {
            // TODO: 停止实际的 HTTP 服务器
            Log.i(TAG, "HTTP 服务器已停止")
        } catch (e: Exception) {
            Log.e(TAG, "停止 HTTP 服务器失败", e)
        }
    }
}
