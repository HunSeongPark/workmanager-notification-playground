package com.hunseong.worknoti

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPreferencesManager {

    const val PREF_FILE = "pref_file"

    fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    }

    fun setBoolean(context: Context, key: String, value: Boolean) {
        val prefs = getPreferences(context)
        prefs.edit {
            putBoolean(key, value)
        }
    }

    fun getBoolean(context: Context, key: String): Boolean {
        val prefs = getPreferences(context)
        return prefs.getBoolean(key, false)
    }

    fun removeKey(context: Context, key: String) {
        val prefs = getPreferences(context)
        prefs.edit {
            remove(key)
        }
    }

    fun clear(context: Context) {
        val prefs = getPreferences(context)
        prefs.edit {
            clear()
        }
    }
}