package com.bhanu.aegis.feature.modelmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class ModelDownloadWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    // Required by WorkManager for expedited work — called before doWork()
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val modelId = inputData.getString(KEY_MODEL_ID) ?: "model"
        return createForegroundInfo(modelId, "Starting download…", 0)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val modelId   = inputData.getString(KEY_MODEL_ID) ?: return@withContext Result.failure()
        val url       = inputData.getString(KEY_MODEL_URL) ?: return@withContext Result.failure()
        val filename  = inputData.getString(KEY_MODEL_FILENAME) ?: return@withContext Result.failure()

        val destDir  = File(context.getExternalFilesDir(null), "models/$modelId").also { it.mkdirs() }
        val destFile = File(destDir, filename)
        val tempFile = File(destDir, "$filename.tmp")

        // Promote to foreground service — keeps the worker alive when app is backgrounded
        setForeground(createForegroundInfo(modelId, "Connecting…", 0))

        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = 30_000
                readTimeout    = 60_000
                instanceFollowRedirects = true
                setRequestProperty("Accept", "application/octet-stream")
                // Add HF token for gated models
                val hfToken = inputData.getString(KEY_HF_TOKEN)
                if (!hfToken.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $hfToken")
                }
            }
            connection.connect()

            if (connection.responseCode !in 200..299) {
                return@withContext Result.failure(
                    workDataOf(KEY_ERROR to "HTTP ${connection.responseCode}")
                )
            }

            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L
            val buffer = ByteArray(64 * 1024)  // 64KB buffer — ~8x faster than 8KB
            var speedBps = 0L
            var lastSpeedUpdateMs = System.currentTimeMillis()
            var bytesAtLastSpeedUpdate = 0L

            connection.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    var bytes: Int
                    var lastProgress = -1
                    while (input.read(buffer).also { bytes = it } != -1) {
                        output.write(buffer, 0, bytes)
                        downloadedBytes += bytes

                        val now = System.currentTimeMillis()
                        val elapsed = now - lastSpeedUpdateMs
                        if (elapsed >= 1000) {
                            speedBps = ((downloadedBytes - bytesAtLastSpeedUpdate) * 1000L) / elapsed
                            bytesAtLastSpeedUpdate = downloadedBytes
                            lastSpeedUpdateMs = now
                        }

                        val progress = if (totalBytes > 0) {
                            (downloadedBytes * 100 / totalBytes).toInt()
                        } else 0

                        val remainingSec = if (speedBps > 0 && totalBytes > 0) {
                            (totalBytes - downloadedBytes) / speedBps
                        } else -1L

                        // Throttle UI / Notification updates to every 1% to save CPU
                        if (progress > lastProgress) {
                            lastProgress = progress
                            setProgress(workDataOf(
                                KEY_PROGRESS         to progress,
                                KEY_DOWNLOADED_BYTES to downloadedBytes,
                                KEY_TOTAL_BYTES      to totalBytes,
                                KEY_SPEED_BPS        to speedBps,
                                KEY_REMAINING_SEC    to remainingSec,
                            ))
                            try {
                                val speedLabel = formatSpeed(speedBps)
                                val etaLabel   = formatEta(remainingSec)
                                val notifText  = if (speedLabel.isNotEmpty()) "$speedLabel · $progress%$etaLabel"
                                                 else "Downloading ($progress%)"
                                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                manager.notify(modelId.hashCode(), createNotification(modelId, notifText, progress))
                            } catch (e: Exception) {}
                        }
                    }
                }
            }

            // Atomic rename — avoids corrupt partial files
            tempFile.renameTo(destFile)
            Result.success()

        } catch (e: Exception) {
            tempFile.delete()
            Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Download failed")))
        }
    }

    private fun createNotification(id: String, text: String, progress: Int): android.app.Notification {
        val channelId = "model_downloads"
        val title = "Downloading Model"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Model Downloads",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .build()
    }

    private fun createForegroundInfo(id: String, text: String, progress: Int): ForegroundInfo {
        val notificationId = id.hashCode()
        val notification = createNotification(id, text, progress)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    companion object {
        const val KEY_MODEL_ID        = "model_id"
        const val KEY_MODEL_URL       = "model_url"
        const val KEY_MODEL_FILENAME  = "model_filename"
        const val KEY_HF_TOKEN        = "hf_token"
        const val KEY_PROGRESS        = "progress"
        const val KEY_DOWNLOADED_BYTES = "downloaded_bytes"
        const val KEY_TOTAL_BYTES     = "total_bytes"
        const val KEY_SPEED_BPS       = "speed_bps"
        const val KEY_REMAINING_SEC   = "remaining_sec"
        const val KEY_ERROR           = "error"

        fun formatSpeed(bps: Long): String {
            if (bps <= 0) return ""
            return when {
                bps >= 1_000_000 -> "%.1f MB/s".format(bps / 1_000_000.0)
                bps >= 1_000     -> "%.0f KB/s".format(bps / 1_000.0)
                else             -> "$bps B/s"
            }
        }

        fun formatEta(remainingSec: Long): String {
            if (remainingSec <= 0) return ""
            return when {
                remainingSec >= 3600 -> " · ~${remainingSec / 3600}h left"
                remainingSec >= 60   -> " · ~${remainingSec / 60}m left"
                else                 -> " · ~${remainingSec}s left"
            }
        }
    }
}
