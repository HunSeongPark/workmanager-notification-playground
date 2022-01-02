package com.hunseong.worknoti2.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hunseong.worknoti2.InfoActivity
import com.hunseong.worknoti2.InfoActivity.Companion.INFO_KEY
import com.hunseong.worknoti2.R
import com.hunseong.worknoti2.model.Info

object NotificationHelper {

    const val CHANNEL_ID = "channel_id"
    const val CHANNEL_NAME = "알림 이름"
    const val DESCRIPTION = "알림 설명"
    const val CONTENT_TEXT = "유저의 프로필을 확인하세요!"
    const val TITLE = "새로운 유저 알림"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(context: Context, info: Info) {
        val intent = Intent(context, InfoActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(INFO_KEY, info)
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(TITLE)
            .setContentText(CONTENT_TEXT)
            .setSmallIcon(R.drawable.ic_baseline_tag_faces_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        notificationManager.notify(0, notification)

    }
}