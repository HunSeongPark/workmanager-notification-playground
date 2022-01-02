package com.hunseong.worknoti2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Info(
    val name: String? = null,
    val description: String? = null,
    val profileImage: String? = null,
) : Parcelable