package com.hunseong.worknoti

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.hunseong.worknoti.Constants.A_MORNING
import com.hunseong.worknoti.Constants.A_NIGHT
import com.hunseong.worknoti.Constants.INTERVAL_HOUR
import com.hunseong.worknoti.Constants.KOREA_TIMEZONE
import com.hunseong.worknoti.Constants.NOTI_CHANNEL_ID
import com.hunseong.worknoti.Constants.PREF_NOTI_KEY
import com.hunseong.worknoti.Constants.WORKER_A
import java.util.*
import java.util.concurrent.TimeUnit

object NotiManager {

    const val CHANNEL_NAME = "채널 이름"
    const val CHANNEL_DESCRIPTION = "채널에 대한 설명입니다."

    fun isNotificationChannelCreated(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.getNotificationChannel(NOTI_CHANNEL_ID) != null
        }
        return true
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTI_CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true) // 알림 시 상단 LED Light
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun setScheduledNotification(workManager: WorkManager) {
        setANoti(workManager)
    }

    fun setANoti(workManager: WorkManager) {
        val oneTimeWorkerRequest = OneTimeWorkRequest.Builder(WorkerA::class.java).build()
        val periodicWorkRequest = PeriodicWorkRequest.Builder(WorkerA::class.java,
            12,
            TimeUnit.HOURS,
            5,
            TimeUnit.MINUTES).build()

        try {
            val workerInfoList = workManager.getWorkInfosForUniqueWorkLiveData(WORKER_A).value
            workerInfoList!!.forEach {
                if (it.state.isFinished) {
                    workManager.enqueue(oneTimeWorkerRequest)
                    workManager.enqueueUniquePeriodicWork(WORKER_A,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicWorkRequest)
                }
            }
        } catch (e: Exception) {
            workManager.enqueue(oneTimeWorkerRequest)
            workManager.enqueueUniquePeriodicWork(WORKER_A,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest)
        }
    }

    fun refreshScheduledNoti(context: Context) {
        val isNotiSwitch = SharedPreferencesManager.getBoolean(context, PREF_NOTI_KEY)
        if (isNotiSwitch) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            val isNotiAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance =
                    notificationManager.getNotificationChannel(NOTI_CHANNEL_ID).importance
                importance != NotificationManager.IMPORTANCE_NONE
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
            if (isNotiAllowed) {
                NotiManager.setScheduledNotification(WorkManager.getInstance(context))
            }
        }
    }

    fun getScheduledCalendar(time: Int): Calendar {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(KOREA_TIMEZONE), Locale.KOREA)
        cal.set(Calendar.HOUR_OF_DAY, time)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        return cal
    }

    fun getNotiDelay(): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(KOREA_TIMEZONE), Locale.KOREA)
        val currentMillis = cal.timeInMillis
        return when {
            cal.get(Calendar.HOUR_OF_DAY) >= A_NIGHT -> {
                val nextDayCal = getScheduledCalendar(A_MORNING)
                nextDayCal.add(Calendar.DAY_OF_YEAR, 1)
                nextDayCal.timeInMillis - currentMillis
            }
            cal.get(Calendar.HOUR_OF_DAY) in A_MORNING until A_NIGHT -> {
                getScheduledCalendar(A_NIGHT).timeInMillis - currentMillis
            }
            cal.get(Calendar.HOUR_OF_DAY) < A_MORNING -> {
                getScheduledCalendar(A_MORNING).timeInMillis - currentMillis
            }
            else -> 0L
        }
    }

    fun createNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val notification = NotificationCompat.Builder(context, NOTI_CHANNEL_ID)
            .setContentTitle("타이틀")
            .setContentText("내용")
            .setSmallIcon(R.drawable.ic_baseline_accessibility_new_24)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(0, notification)
    }
}