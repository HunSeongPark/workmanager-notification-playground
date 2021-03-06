package com.hunseong.worknoti

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hunseong.worknoti.Constants.A_MORNING
import com.hunseong.worknoti.Constants.A_NIGHT
import com.hunseong.worknoti.Constants.INTERVAL_HOUR
import com.hunseong.worknoti.Constants.KOREA_TIMEZONE
import java.util.*
import java.util.concurrent.TimeUnit

class WorkerA(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val currentMillis =
            Calendar.getInstance(TimeZone.getTimeZone(KOREA_TIMEZONE), Locale.KOREA).timeInMillis

        val eventCal = NotiManager.getScheduledCalendar(A_MORNING)
        val morningNotifyMinRange = eventCal.timeInMillis

        eventCal.add(Calendar.HOUR_OF_DAY, INTERVAL_HOUR)
        val morningNotifyMaxRange = eventCal.timeInMillis

        eventCal.set(Calendar.HOUR_OF_DAY, A_NIGHT)
        val nightNotifyMinRange = eventCal.timeInMillis

        eventCal.add(Calendar.HOUR_OF_DAY, INTERVAL_HOUR)
        val nightNotifyMaxRange = eventCal.timeInMillis

        val isMorningNotifyRange =
            currentMillis in morningNotifyMinRange..morningNotifyMaxRange
        val isNightNotifyRange =
            currentMillis in nightNotifyMinRange..nightNotifyMaxRange

        val isEventNotifyRange = isMorningNotifyRange || isNightNotifyRange

        // 현재 Worker 실행시점이 이벤트 동작 시점이라면 바로 notify
        if (isEventNotifyRange) {
            NotiManager.createNotification(context)
        } else {
            // 이벤트 동작 시점이 아니라면 delay 계산 후 Work 등록
            Log.e("Shared", "delay 후 Work 등록", )
            val delay = NotiManager.getNotiDelay()
            val workRequest = OneTimeWorkRequest.Builder(WorkerA::class.java)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
        return Result.success()
    }
}