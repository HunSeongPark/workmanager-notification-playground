package com.hunseong.noti

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.hunseong.noti.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()

        binding.notiBtn.setOnClickListener {
            displayNotification()
        }
    }

    private fun displayNotification() {
        val intent = Intent(this, SecondActivity::class.java).apply {
            // 모든 액티비티 제거 후 해당 액티비티 실행
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("타이틀 설정")
            .setContentText("내용 설정")
            .setSmallIcon(R.drawable.ic_heart) // 아이콘 설정
            .setAutoCancel(true) // 사용자가 알림 클릭 시 자동으로 알림 삭제
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Android 7.1 이하에서 필요한 우선순위
            .setContentIntent(pendingIntent) // 알림 클릭 시 동작할 PendingIntent
            .build()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification)
    }

    // Android 8.0 이상은 노티 채널 생성 필요
    // 반복적으로 앱이 시작할 때마다 수행할 수 있도록 처리
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // NotificationManager에 채널 등록
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        // 앱마다 사용되는 유니크한 채널 ID
        // 채널과 notification이 동일한 채널 ID를 가져야 함
        const val CHANNEL_ID = "channel_id"
    }
}