package com.hunseong.worknoti2.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hunseong.worknoti2.model.Info
import com.hunseong.worknoti2.util.NotificationHelper

class ProfileWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val name = inputData.getString("name")
        val description = inputData.getString("description")
        val profileImage = inputData.getString("profileImage")

        val info = Info(name, description, profileImage)
        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.createNotification(context, info)
        return Result.success()
    }
}