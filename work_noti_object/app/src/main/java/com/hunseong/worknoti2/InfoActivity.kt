package com.hunseong.worknoti2

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.TransitionOptions
import com.hunseong.worknoti2.databinding.ActivityInfoBinding
import com.hunseong.worknoti2.model.Info

class InfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val info = intent.getParcelableExtra<Info>(INFO_KEY)

        info?.let {
            setProfile(info)
        }
    }

    private fun setProfile(info: Info) = with(binding) {
        nameTv.text = info.name
        descTv.text = info.description

        Glide.with(profileIv)
            .load(info.profileImage)
            .centerCrop()
            .into(profileIv)
    }

    companion object {
        const val INFO_KEY = "info_key"
    }
}