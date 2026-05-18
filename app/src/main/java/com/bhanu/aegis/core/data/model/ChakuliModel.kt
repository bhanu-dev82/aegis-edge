package com.bhanu.aegis.core.data.model

import com.bhanu.aegis.tasks.TaskType

data class ChakuliModel(
    val id: String,
    val displayName: String,
    val description: String,
    val filename: String,
    val huggingFaceRepo: String,
    val sizeBytes: Long,
    val maxContextLength: Int,
    val defaultMaxTokens: Int,
    val supportedTasks: List<TaskType>,
    val supportsImage: Boolean,
    val supportsAudio: Boolean,
    val minRamGb: Int,
    val tier: ModelTier = ModelTier.OFFICIAL,
) {
    val huggingFaceDownloadUrl: String
        get() = "https://huggingface.co/$huggingFaceRepo/resolve/main/$filename?download=true"

    val displaySizeMb: String
        get() = if (sizeBytes > 1_000_000_000L)
            "%.1f GB".format(sizeBytes / 1_000_000_000.0)
        else
            "%.0f MB".format(sizeBytes / 1_000_000.0)
}

enum class ModelTier { OFFICIAL }

sealed class ModelDownloadStatus {
    data object NotDownloaded : ModelDownloadStatus()
    data object Checking : ModelDownloadStatus()
    data class Downloading(
        val progressPercent: Int = 0,
        val downloadedBytes: Long = 0L,
        val totalBytes: Long = 0L,
        val speedBps: Long = 0L,
        val remainingSec: Long = -1L,
    ) : ModelDownloadStatus()
    data object Downloaded : ModelDownloadStatus()
    data class Error(val message: String) : ModelDownloadStatus()
}
