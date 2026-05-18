package com.bhanu.aegis.feature.modelmanager

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bhanu.aegis.core.data.model.ChakuliModel
import com.bhanu.aegis.core.data.model.ModelDownloadStatus
import com.bhanu.aegis.core.data.repository.ModelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelManagerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val modelRepository: ModelRepository,
    private val settingsRepository: com.bhanu.aegis.core.data.repository.SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModelManagerUiState())
    val uiState: StateFlow<ModelManagerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            modelRepository.downloadStatuses.collect { statuses ->
                _uiState.update { it.copy(downloadStatuses = statuses) }
            }
        }
        viewModelScope.launch {
            settingsRepository.huggingFaceToken.collect { token ->
                _uiState.update { it.copy(huggingFaceToken = token) }
            }
        }
        modelRepository.allModels.forEach { model ->
            if (modelRepository.isModelDownloaded(model)) {
                modelRepository.updateDownloadStatus(model.id, ModelDownloadStatus.Downloaded)
            } else {
                modelRepository.updateDownloadStatus(model.id, ModelDownloadStatus.NotDownloaded)
            }
        }
        modelRepository.allModels.forEach { model ->
            observeWorker(model)
        }
    }

    /**
     * Single persistent WorkManager observer per model.
     * Uses getWorkInfosForUniqueWorkFlow (by unique name) so we always get exactly
     * the current work item — not stale completed items from previous runs.
     */
    private fun observeWorker(model: ChakuliModel) {
        viewModelScope.launch {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow("download_${model.id}")
                .collect { workInfoList ->
                    val workInfo = workInfoList.firstOrNull() ?: return@collect
                    when (workInfo.state) {
                        WorkInfo.State.ENQUEUED,
                        WorkInfo.State.RUNNING -> {
                            val progress  = workInfo.progress.getInt(ModelDownloadWorker.KEY_PROGRESS, 0)
                            val downloaded = workInfo.progress.getLong(ModelDownloadWorker.KEY_DOWNLOADED_BYTES, 0L)
                            val total     = workInfo.progress.getLong(ModelDownloadWorker.KEY_TOTAL_BYTES, 0L)
                            val speed     = workInfo.progress.getLong(ModelDownloadWorker.KEY_SPEED_BPS, 0L)
                            val remaining = workInfo.progress.getLong(ModelDownloadWorker.KEY_REMAINING_SEC, -1L)
                            modelRepository.updateDownloadStatus(
                                model.id,
                                ModelDownloadStatus.Downloading(progress, downloaded, total, speed, remaining),
                            )
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            modelRepository.updateDownloadStatus(model.id, ModelDownloadStatus.Downloaded)
                        }
                        WorkInfo.State.FAILED -> {
                            val error = workInfo.outputData.getString(ModelDownloadWorker.KEY_ERROR) ?: "Download failed"
                            modelRepository.updateDownloadStatus(model.id, ModelDownloadStatus.Error(error))
                        }
                        WorkInfo.State.CANCELLED -> {
                            if (!modelRepository.isModelDownloaded(model)) {
                                modelRepository.updateDownloadStatus(model.id, ModelDownloadStatus.NotDownloaded)
                            }
                        }
                        else -> Unit
                    }
                }
        }
    }

    fun downloadModel(model: ChakuliModel) {
        modelRepository.updateDownloadStatus(model.id, ModelDownloadStatus.Downloading())

        val inputData = Data.Builder()
            .putString(ModelDownloadWorker.KEY_MODEL_ID, model.id)
            .putString(ModelDownloadWorker.KEY_MODEL_URL, model.huggingFaceDownloadUrl)
            .putString(ModelDownloadWorker.KEY_MODEL_FILENAME, model.filename)
            .also { builder ->
                // Pass HF token for gated models
                val token = _uiState.value.huggingFaceToken
                if (token.isNotBlank()) builder.putString(ModelDownloadWorker.KEY_HF_TOKEN, token)
            }
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(inputData)
            .build()

        // REPLACE: cancel any previous work for this model and start fresh
        // This ensures getWorkInfosForUniqueWorkFlow always returns the new job
        WorkManager.getInstance(context).enqueueUniqueWork(
            "download_${model.id}",
            ExistingWorkPolicy.REPLACE,
            downloadRequest,
        )
        // observeWorker() already running from init — no second observer needed
    }

    fun cancelDownload(model: ChakuliModel) {
        WorkManager.getInstance(context).cancelUniqueWork("download_${model.id}")
        modelRepository.updateDownloadStatus(model.id, ModelDownloadStatus.NotDownloaded)
    }

    fun deleteModel(model: ChakuliModel) {
        viewModelScope.launch {
            val file = modelRepository.getModelFile(model)
            file.parentFile?.deleteRecursively()
            modelRepository.updateDownloadStatus(model.id, ModelDownloadStatus.NotDownloaded)
        }
    }

    fun onModelImported(model: ChakuliModel) {
        modelRepository.updateDownloadStatus(model.id, ModelDownloadStatus.Downloaded)
        _uiState.update { it.copy(snackbarMessage = "Imported ${model.displayName}") }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    suspend fun saveHuggingFaceToken(token: String) {
        settingsRepository.setHuggingFaceToken(token)
    }
}

data class ModelManagerUiState(
    val downloadStatuses: Map<String, ModelDownloadStatus> = emptyMap(),
    val snackbarMessage: String? = null,
    val huggingFaceToken: String = "",
)
