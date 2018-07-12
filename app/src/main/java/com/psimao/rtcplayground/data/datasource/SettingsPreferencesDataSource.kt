package com.psimao.rtcplayground.data.datasource

import android.content.Context

class SettingsPreferencesDataSource(context: Context) {

    companion object {
        const val PREFERENCES_NAME = "WebSocketSettings"
        const val KEY_URL = "url"
        const val KEY_USER = "user"
    }

    private val sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun get(key: String): String? = sharedPreferences.getString(key, null)

    fun put(key: String, value: String) = sharedPreferences.edit().putString(key, value).apply()
}