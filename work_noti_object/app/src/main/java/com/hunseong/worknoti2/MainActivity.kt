package com.hunseong.worknoti2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.hunseong.worknoti2.databinding.ActivityInfoBinding
import com.hunseong.worknoti2.databinding.ActivityMainBinding
import com.hunseong.worknoti2.model.Info
import com.hunseong.worknoti2.worker.ProfileWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val workManager: WorkManager by lazy {
        WorkManager.getInstance(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSwitch()
    }

    private fun setSwitch() = with(binding) {
        notiSwitch.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                val data = Data.Builder().apply {
                    putString("name", "HunSeong")
                    putString("description", "Hi, I'm HunSeong!")
                    putString("profileImage", "https://picsum.photos/200/200")
                }.build()

                val workRequest = OneTimeWorkRequest.Builder(ProfileWorker::class.java)
                    .setInitialDelay(1, TimeUnit.MINUTES)
                    .setInputData(data)
                    .build()

                workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, workRequest)
                Log.e("MyInfo", "enqueue!", )
            } else {
                workManager.cancelAllWork()
            }
        }
    }

    companion object {
        const val PROFILE_KEY = "profile_key"
        const val WORK_NAME = "work_name"
    }
}