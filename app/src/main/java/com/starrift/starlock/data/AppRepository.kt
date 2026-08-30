package com.starrift.starlock.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import android.util.Base64

class AppRepository(private val database: AppDatabase) {

    // --- Çöp Kutusu ---
    fun getDeletedApps(): Flow<List<AppItem>> = database.appDao().getDeletedApps()
    fun getDeletedAccounts(): Flow<List<AccountWithAppName>> = database.accountDao().getDeletedAccounts()
    fun getDeletedFields(): Flow<List<AccountFieldWithAccountName>> = database.accountFieldDao().getDeletedFields()

    suspend fun softDeleteApp(appId: Long) {
        val now = System.currentTimeMillis()
        database.accountFieldDao().softDeleteFieldsByAppId(appId, now)
        database.accountDao().softDeleteAccountsByAppId(appId, now)
        database.appDao().softDeleteApp(appId, now)
    }

    suspend fun softDeleteAccount(accountId: Long) {
        val now = System.currentTimeMillis()
        database.accountFieldDao().softDeleteFieldsByAccountId(accountId, now)
        database.accountDao().softDeleteAccount(accountId, now)
    }

    suspend fun softDeleteField(fieldId: Long) {
        database.accountFieldDao().softDeleteField(fieldId, System.currentTimeMillis())
    }

    suspend fun restoreApp(appId: Long) {
        database.appDao().restoreApp(appId)
        database.accountDao().restoreAccountsByAppId(appId)
        database.accountFieldDao().restoreFieldsByAppId(appId)
    }
    suspend fun restoreAccount(accountId: Long) {
        database.accountDao().restoreAccount(accountId)
        database.accountFieldDao().restoreFieldsByAccountId(accountId)
    }
    suspend fun restoreField(fieldId: Long) = database.accountFieldDao().restoreField(fieldId)

    suspend fun permanentlyDeleteApp(appId: Long) = database.appDao().permanentlyDeleteApp(appId)
    suspend fun permanentlyDeleteAccount(accountId: Long) = database.accountDao().permanentlyDeleteAccount(accountId)
    suspend fun permanentlyDeleteField(fieldId: Long) = database.accountFieldDao().permanentlyDeleteField(fieldId)

    // --- Arşivleme ---
    fun getArchivedApps(): Flow<List<AppItem>> = database.appDao().getArchivedApps()
    fun getArchivedAccounts(): Flow<List<AccountWithAppName>> = database.accountDao().getArchivedAccounts()

    suspend fun archiveApp(appId: Long) = database.appDao().archiveApp(appId, System.currentTimeMillis())
    suspend fun unarchiveApp(appId: Long) = database.appDao().unarchiveApp(appId)
    suspend fun archiveAccount(accountId: Long) = database.accountDao().archiveAccount(accountId, System.currentTimeMillis())
    suspend fun unarchiveAccount(accountId: Long) = database.accountDao().unarchiveAccount(accountId)

    fun getArchivedFields(): Flow<List<AccountFieldWithAccountName>> = database.accountFieldDao().getArchivedFields()

    suspend fun archiveField(fieldId: Long) = database.accountFieldDao().archiveField(fieldId, System.currentTimeMillis())
    suspend fun unarchiveField(fieldId: Long) = database.accountFieldDao().unarchiveField(fieldId)

    fun getAllAppsWithCount(): Flow<List<AppWithAccountCount>> =
        database.appDao().getAllAppsWithCount()

    fun getAppById(appId: Long): Flow<AppItem?> =
        database.appDao().getAppById(appId)

    suspend fun addApp(name: String, category: AppCategory, iconPath: String?): Long {
        return database.appDao().insertApp(
            AppItem(name = name.trim(), category = category, iconPath = iconPath)
        )
    }

    suspend fun deleteApp(app: AppItem) = database.appDao().deleteApp(app)
    suspend fun updateApp(app: AppItem) = database.appDao().updateApp(app)
    suspend fun setFavorite(appId: Long, isFavorite: Boolean) = database.appDao()
        .setFavorite(appId, isFavorite)

    fun getAccountsForApp(appId: Long): Flow<List<AccountItem>> =
        database.accountDao().getAccountsForApp(appId)

    suspend fun addAccount(appId: Long, name: String, iconPath: String?, tag: String? = null): Long {
        return database.accountDao().insertAccount(
            AccountItem(appId = appId, name = name.trim(), iconPath = iconPath, tag = tag?.trim()?.ifBlank { null })
        )
    }

    suspend fun deleteAccount(account: AccountItem) = database.accountDao().deleteAccount(account)
    suspend fun updateAccount(account: AccountItem) = database.accountDao().updateAccount(account)
    suspend fun setAccountFavorite(accountId: Long, isFavorite: Boolean) = database
        .accountDao().setFavorite(accountId, isFavorite)

    fun getFieldsForAccount(accountId: Long): Flow<List<AccountField>> =
        database.accountFieldDao().getFieldsForAccount(accountId)

    // --- Field History ---
    suspend fun addFieldHistoryEntry(entry: FieldHistoryEntry) =
        database.fieldHistoryDao().insertEntry(entry)

    fun getCurrentFieldHistory(accountId: Long): Flow<List<FieldHistoryEntry>> =
        database.fieldHistoryDao().getCurrentHistoryForAccount(accountId)

    fun getDeletedFieldHistory(accountId: Long): Flow<List<FieldHistoryEntry>> =
        database.fieldHistoryDao().getDeletedHistoryForAccount(accountId)

    suspend fun clearFieldHistory(accountId: Long) =
        database.fieldHistoryDao().clearHistoryForAccount(accountId)

    suspend fun addField(accountId: Long, label: String, value: String, isCustomLabel: Boolean, orderIndex: Int, isCensored: Boolean = false): Long {
        return database.accountFieldDao().insertField(
            AccountField(
                accountId = accountId,
                label = label.trim(),
                value = value.trim(),
                isCustomLabel = isCustomLabel,
                orderIndex = orderIndex,
                isCensored = isCensored
            )
        )
    }

    suspend fun deleteField(field: AccountField) = database.accountFieldDao().deleteField(field)
    suspend fun updateField(field: AccountField) = database.accountFieldDao().updateField(field)

    suspend fun updateFieldsOrder(fields: List<AccountField>) = database.accountFieldDao().updateFields(fields)

    /** Verilen dosya yolundaki görseli Base64 string'e çevirir, yoksa null döner. */
    private fun iconPathToBase64(path: String?): String? {
        if (path == null) return null
        val file = File(path)
        if (!file.exists()) return null
        return try {
            Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    /** Base64 string'i dahili depolamaya yeni bir dosya olarak yazar, yeni path'i döner. */
    private fun base64ToIconPath(context: android.content.Context, base64: String?): String? {
        if (base64.isNullOrBlank()) return null
        return try {
            val iconsDir = File(context.filesDir, "icons").apply { mkdirs() }
            val destFile = File(iconsDir, "${UUID.randomUUID()}.png")
            destFile.writeBytes(Base64.decode(base64, Base64.NO_WRAP))
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    suspend fun exportAllDataAsJson(): String {
        val root = JSONObject()

        val appsArray = JSONArray()
        database.appDao().getAllAppsOnce().forEach { app ->
            appsArray.put(
                JSONObject().apply {
                    put("id", app.id)
                    put("name", app.name)
                    put("category", app.category.name)
                    put("iconPath", app.iconPath ?: JSONObject.NULL)
                    put("iconData", iconPathToBase64(app.iconPath) ?: JSONObject.NULL)
                put("isDeleted", app.isDeleted)
                put("deletedAt", app.deletedAt ?: JSONObject.NULL)
                put("isArchived", app.isArchived)
                put("archivedAt", app.archivedAt ?: JSONObject.NULL)
                }
            )
        }

        val accountsArray = JSONArray()
        database.accountDao().getAllAccountsOnce().forEach { account ->
            accountsArray.put(
                JSONObject().apply {
                    put("id", account.id)
                    put("appId", account.appId)
                    put("name", account.name)
                    put("iconPath", account.iconPath ?: JSONObject.NULL)
                    put("iconData", iconPathToBase64(account.iconPath) ?: JSONObject.NULL)
                put("isDeleted", account.isDeleted)
                put("deletedAt", account.deletedAt ?: JSONObject.NULL)
                put("isArchived", account.isArchived)
                put("archivedAt", account.archivedAt ?: JSONObject.NULL)
                put("tag", account.tag ?: JSONObject.NULL)
                }
            )
        }

        val fieldsArray = JSONArray()
        database.accountFieldDao().getAllFieldsOnce().forEach { field ->
            fieldsArray.put(
                JSONObject().apply {
                    put("id", field.id)
                    put("accountId", field.accountId)
                    put("label", field.label)
                    put("value", field.value)
                    put("isCustomLabel", field.isCustomLabel)
                    put("orderIndex", field.orderIndex)
                put("isDeleted", field.isDeleted)
                put("deletedAt", field.deletedAt ?: JSONObject.NULL)
                put("isArchived", field.isArchived)
                put("archivedAt", field.archivedAt ?: JSONObject.NULL)
                put("isCensored", field.isCensored)
                }
            )
        }

        root.put("apps", appsArray)
        root.put("accounts", accountsArray)
        root.put("fields", fieldsArray)

        val historyArray = JSONArray()
        database.fieldHistoryDao().getAllHistoryOnce().forEach { h ->
            historyArray.put(
                JSONObject().apply {
                    put("id", h.id)
                    put("fieldId", h.fieldId)
                    put("accountId", h.accountId)
                    put("changeType", h.changeType.name)
                    put("oldLabel", h.oldLabel ?: JSONObject.NULL)
                    put("newLabel", h.newLabel ?: JSONObject.NULL)
                    put("oldValue", h.oldValue ?: JSONObject.NULL)
                    put("newValue", h.newValue ?: JSONObject.NULL)
                    put("timestamp", h.timestamp)
                }
            )
        }
        root.put("history", historyArray)
        return root.toString(2)
    }

    suspend fun importAllDataFromJson(context: android.content.Context, json: String) {
        val root = JSONObject(json)

        val apps = mutableListOf<AppItem>()
        val appsArray = root.optJSONArray("apps") ?: JSONArray()
        for (i in 0 until appsArray.length()) {
            val obj = appsArray.getJSONObject(i)
            val iconData = if (obj.isNull("iconData")) null else obj.getString("iconData")
            apps.add(
                AppItem(
                    id = obj.getLong("id"),
                    name = obj.getString("name"),
                    category = AppCategory.valueOf(obj.getString("category")),
                    iconPath = base64ToIconPath(context, iconData),
                isDeleted = obj.optBoolean("isDeleted", false),
                deletedAt = if (obj.isNull("deletedAt")) null else obj.optLong("deletedAt"),
                isArchived = obj.optBoolean("isArchived", false),
                archivedAt = if (obj.isNull("archivedAt")) null else obj.optLong("archivedAt")
                )
            )
        }

        val accounts = mutableListOf<AccountItem>()
        val accountsArray = root.optJSONArray("accounts") ?: JSONArray()
        for (i in 0 until accountsArray.length()) {
            val obj = accountsArray.getJSONObject(i)
            val iconData = if (obj.isNull("iconData")) null else obj.getString("iconData")
            accounts.add(
                AccountItem(
                    id = obj.getLong("id"),
                    appId = obj.getLong("appId"),
                    name = obj.getString("name"),
                    iconPath = base64ToIconPath(context, iconData),
                isDeleted = obj.optBoolean("isDeleted", false),
                deletedAt = if (obj.isNull("deletedAt")) null else obj.optLong("deletedAt"),
                isArchived = obj.optBoolean("isArchived", false),
                archivedAt = if (obj.isNull("archivedAt")) null else obj.optLong("archivedAt"),
                tag = if (obj.isNull("tag")) null else obj.getString("tag")
                )
            )
        }

        val fields = mutableListOf<AccountField>()
        val fieldsArray = root.optJSONArray("fields") ?: JSONArray()
        for (i in 0 until fieldsArray.length()) {
            val obj = fieldsArray.getJSONObject(i)
            fields.add(
                AccountField(
                    id = obj.getLong("id"),
                    accountId = obj.getLong("accountId"),
                    label = obj.getString("label"),
                    value = obj.getString("value"),
                    isCustomLabel = obj.getBoolean("isCustomLabel"),
                    orderIndex = obj.getInt("orderIndex"),
                isDeleted = obj.optBoolean("isDeleted", false),
                deletedAt = if (obj.isNull("deletedAt")) null else obj.optLong("deletedAt"),
                isArchived = obj.optBoolean("isArchived", false),
                archivedAt = if (obj.isNull("archivedAt")) null else obj.optLong("archivedAt"),
                isCensored = obj.optBoolean("isCensored", false)
                )
            )
        }

        database.appDao().clearApps()
        database.appDao().insertApps(apps)
        database.accountDao().insertAccounts(accounts)
        database.accountFieldDao().insertFields(fields)

        val history = mutableListOf<FieldHistoryEntry>()
        val historyArray = root.optJSONArray("history") ?: JSONArray()
        for (i in 0 until historyArray.length()) {
            val obj = historyArray.getJSONObject(i)
            history.add(
                FieldHistoryEntry(
                    id = obj.getLong("id"),
                    fieldId = obj.getLong("fieldId"),
                    accountId = obj.getLong("accountId"),
                    changeType = FieldChangeType.valueOf(obj.getString("changeType")),
                    oldLabel = if (obj.isNull("oldLabel")) null else obj.getString("oldLabel"),
                    newLabel = if (obj.isNull("newLabel")) null else obj.getString("newLabel"),
                    oldValue = if (obj.isNull("oldValue")) null else obj.getString("oldValue"),
                    newValue = if (obj.isNull("newValue")) null else obj.getString("newValue"),
                    timestamp = obj.getLong("timestamp")
                )
            )
        }
        database.fieldHistoryDao().clearAllHistory()
        database.fieldHistoryDao().insertAll(history)
    }
}
