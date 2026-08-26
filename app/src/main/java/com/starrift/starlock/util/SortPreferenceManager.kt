package com.starrift.starlock.util

import android.content.Context
import android.content.SharedPreferences
import com.starrift.starlock.data.SortOption

class SortPreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("sort_prefs", Context.MODE_PRIVATE)

    fun getAppSortOption(): SortOption =
        runCatching { SortOption.valueOf(prefs.getString(KEY_APPS, null) ?: DEFAULT.name) }
            .getOrDefault(DEFAULT)

    fun setAppSortOption(option: SortOption) {
        prefs.edit().putString(KEY_APPS, option.name).apply()
    }

    fun getAccountSortOption(): SortOption =
        runCatching { SortOption.valueOf(prefs.getString(KEY_ACCOUNTS, null) ?: DEFAULT.name) }
            .getOrDefault(DEFAULT)

    fun setAccountSortOption(option: SortOption) {
        prefs.edit().putString(KEY_ACCOUNTS, option.name).apply()
    }

    companion object {
        private const val KEY_APPS = "sort_apps"
        private const val KEY_ACCOUNTS = "sort_accounts"
        private val DEFAULT = SortOption.ALPHA_ASC
    }
}
