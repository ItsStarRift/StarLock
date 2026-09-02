package com.starrift.starlock.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

object WebDavClient {

    private const val TIMEOUT_MS = 15000

    private fun basicAuthHeader(username: String, password: String): String {
        val raw = "$username:$password"
        return "Basic " + Base64.getEncoder().encodeToString(raw.toByteArray(Charsets.UTF_8))
    }

    suspend fun upload(
        url: String,
        username: String,
        password: String,
        data: ByteArray
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.doOutput = true
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("Authorization", basicAuthHeader(username, password))
            connection.setRequestProperty("Content-Type", "application/octet-stream")
            connection.setFixedLengthStreamingMode(data.size)

            val outputStream: OutputStream = connection.outputStream
            outputStream.write(data)
            outputStream.flush()
            outputStream.close()

            val code = connection.responseCode
            connection.disconnect()

            if (code in 200..299) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("WebDAV upload failed: HTTP $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun download(
        url: String,
        username: String,
        password: String
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("Authorization", basicAuthHeader(username, password))

            val code = connection.responseCode
            if (code !in 200..299) {
                connection.disconnect()
                return@withContext Result.failure(Exception("WebDAV download failed: HTTP $code"))
            }

            val bytes = connection.inputStream.use { it.readBytes() }
            connection.disconnect()
            Result.success(bytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
