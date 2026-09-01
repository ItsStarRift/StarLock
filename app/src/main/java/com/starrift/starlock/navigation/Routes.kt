package com.starrift.starlock.navigation

object Routes {
    const val HOME = "home"
    const val ACCOUNT_LIST = "accounts/{appId}"
    const val ACCOUNT_DETAIL = "account_detail/{accountId}"
    const val FIELD_HISTORY = "field_history/{accountId}"
    const val TRASH = "trash"
    const val ARCHIVED = "archived"
    const val APP_LOCK = "app_lock"
    const val BACKUP = "backup"
    const val CLOUD_EXPORT = "cloud_export"
    const val CLOUD_IMPORT = "cloud_import"

    fun accountList(appId: Long) = "accounts/$appId"
    fun accountDetail(accountId: Long) = "account_detail/$accountId"
    fun fieldHistory(accountId: Long) = "field_history/$accountId"
}
