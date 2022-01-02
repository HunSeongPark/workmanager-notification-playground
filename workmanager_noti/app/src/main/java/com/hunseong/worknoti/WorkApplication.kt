package com.hunseong.worknoti

import android.app.Application

class WorkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotiManager.createNotificationChannel(this)
        NotiManager.refreshScheduledNoti(this)
    }
}