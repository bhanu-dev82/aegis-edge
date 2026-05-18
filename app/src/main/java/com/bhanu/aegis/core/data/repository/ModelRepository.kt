package com.bhanu.aegis.core.data.repository

import com.bhanu.aegis.core.data.model.ChakuliModel
import com.bhanu.aegis.core.data.model.ModelDownloadStatus
import com.bhanu.aegis.core.data.model.ModelTier
import com.bhanu.aegis.tasks.TaskType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context

@Singleton
class ModelRepository @Inject constructor(
    private val context: Context,
) {
    private val modelsDir: File
        get() = File(context.getExternalFilesDir(null), "models").also { it.mkdirs() }

    private val _downloadStatuses = MutableStateFlow<Map<String, ModelDownloadStatus>>(emptyMap())
    val downloadStatuses: StateFlow<Map<String, ModelDownloadStatus>> = _downloadStatuses.asStateFlow()

    /** Official Gemma 4 models — Apache 2.0, no token required. */
    val allModels: List<ChakuliModel> = listOf(
        ChakuliModel(
            id = "gemma-4-e2b-it",
            displayName = "Gemma 4 E2B",
            description = "Google's multimodal model — text, images, audio. Recommended.",
            filename = "gemma-4-E2B-it.litertlm",
            huggingFaceRepo = "litert-community/gemma-4-E2B-it-litert-lm",
            sizeBytes = 2_773_000_000L,
            maxContextLength = 32000,
            defaultMaxTokens = 8192,
            supportedTasks = listOf(TaskType.DisasterTriage),
            supportsImage = true,
            supportsAudio = true,
            minRamGb = 8,
            tier = ModelTier.OFFICIAL,
        ),
        ChakuliModel(
            id = "gemma-4-e4b-it",
            displayName = "Gemma 4 E4B",
            description = "More capable 4B model — richer reasoning, better quality.",
            filename = "gemma-4-E4B-it.litertlm",
            huggingFaceRepo = "litert-community/gemma-4-E4B-it-litert-lm",
            sizeBytes = 3_919_000_000L,
            maxContextLength = 32000,
            defaultMaxTokens = 8192,
            supportedTasks = listOf(TaskType.DisasterTriage),
            supportsImage = true,
            supportsAudio = true,
            minRamGb = 12,
            tier = ModelTier.OFFICIAL,
        ),
    )

    fun getModelsForTask(taskType: TaskType): List<ChakuliModel> =
        allModels.filter { taskType in it.supportedTasks }

    fun getModelById(id: String): ChakuliModel? = allModels.find { it.id == id }

    fun getModelFile(model: ChakuliModel): File =
        File(modelsDir, "${model.id}/${model.filename}")

    /** Smallest downloaded model that supports image + audio (for triage). */
    fun getPreferredMultimodalModel(
        needsImage: Boolean = false,
        needsAudio: Boolean = false,
    ): ChakuliModel? = allModels
        .filter { model ->
            isModelDownloaded(model)
                && (!needsImage || model.supportsImage)
                && (!needsAudio || model.supportsAudio)
        }
        .minByOrNull { it.sizeBytes }

    fun isModelDownloaded(model: ChakuliModel): Boolean = getModelFile(model).exists()

    fun updateDownloadStatus(modelId: String, status: ModelDownloadStatus) {
        _downloadStatuses.update { it + (modelId to status) }
    }

    fun getDownloadStatus(modelId: String): ModelDownloadStatus =
        _downloadStatuses.value[modelId] ?: ModelDownloadStatus.NotDownloaded

    /** Register a locally imported .litertlm file. */
    fun importLocalModel(file: File, displayName: String): ChakuliModel {
        val sanitised = displayName.replace(Regex("[^a-zA-Z0-9_-]"), "_").lowercase()
        val destDir = File(modelsDir, "local_$sanitised").also { it.mkdirs() }
        val destFile = File(destDir, file.name)
        file.copyTo(destFile, overwrite = true)
        return ChakuliModel(
            id = "local_$sanitised",
            displayName = displayName,
            description = "Locally imported model",
            filename = file.name,
            huggingFaceRepo = "",
            sizeBytes = destFile.length(),
            maxContextLength = 8192,
            defaultMaxTokens = 4096,
            supportedTasks = listOf(TaskType.DisasterTriage),
            supportsImage = false,
            supportsAudio = false,
            minRamGb = 4,
        )
    }
}
