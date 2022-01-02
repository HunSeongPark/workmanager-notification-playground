package com.hunseong.worknoti

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
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
        // 현 시점에서 바로 등록할 OneTimeWorkerRequest
        val oneTimeWorkerRequest = OneTimeWorkRequest.Builder(WorkerA::class.java).build()

        // 12시간 후 재등록 할 PeriodicWorkRequest
        val periodicWorkRequest = PeriodicWorkRequest.Builder(WorkerA::class.java,
            12,
            TimeUnit.HOURS,
            5,
            TimeUnit.MINUTES).build()

        try {
            // 이전 Work가 이미 finished라면 새로운 work 등록
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
            // 이전에 등록된 Work가 없다면 새로운 Work 등록
            workManager.enqueue(oneTimeWorkerRequest)
            workManager.enqueueUniquePeriodicWork(WORKER_A,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest)
        }
    }

    fun refreshScheduledNoti(context: Context) {
        // 여러 이유로 소멸된 백그라운드 작업 대비 앱 실행 시 refresh
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
                setScheduledNotification(WorkManager.getInstance(context))
            }
        }
    }

    fun getScheduledCalendar(time: Int): Calendar {
        // 이벤트 시간 Calendar type으로 return
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
            // 현재 시각이 20시 이후일 경우 다음 날 오전 delay
            cal.get(Calendar.HOUR_OF_DAY) >= A_NIGHT -> {
                Log.e("Shared", "20~", )
                val nextDayCal = getScheduledCalendar(A_MORNING)
                nextDayCal.add(Calendar.DAY_OF_YEAR, 1)
                nextDayCal.timeInMillis - currentMillis
            }
            // 현재 시각이 08시 ~ 20시 사이일 경우 당일 오후 delay
            cal.get(Calendar.HOUR_OF_DAY) in A_MORNING until A_NIGHT -> {
                Log.e("Shared", "08 ~ 20", )
                getScheduledCalendar(A_NIGHT).timeInMillis - currentMillis
            }
            // 현재 시각이 08시 이전일 경우 당일 오전 delay
            cal.get(Calendar.HOUR_OF_DAY) < A_MORNING -> {
                Log.e("Shared", "~08", )
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