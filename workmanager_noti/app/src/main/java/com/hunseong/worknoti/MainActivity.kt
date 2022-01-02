package com.hunseong.worknoti

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.work.WorkManager
import com.hunseong.worknoti.Constants.PREF_NOTI_KEY
import com.hunseong.worknoti.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val workManager = WorkManager.getInstance(applicationContext)

        // 알림 스위치 변경값에 따라 sharedPreferences에 값 저장
        binding.notiSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.e("Shared", isChecked.toString())
            val isChannelCreated = NotiManager.isNotificationChannelCreated(applicationContext)
            SharedPreferencesManager.setBoolean(this, PREF_NOTI_KEY, isChecked)
            if (isChecked) {
                if (!isChannelCreated) {
                    Log.e("Shared", "create Channel")
                    NotiManager.createNotificationChannel(applicationContext)
                }
                NotiManager.setScheduledNotification(workManager)
            } else {
                workManager.cancelAllWork()
            }
        }
    }
}