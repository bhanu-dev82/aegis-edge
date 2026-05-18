package com.bhanu.aegis.core.llm

import com.bhanu.aegis.core.data.model.ChakuliModel
import com.bhanu.aegis.core.data.repository.ModelRepository
import com.bhanu.aegis.tasks.TaskType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRouter @Inject constructor(
    private val modelRepo: ModelRepository,
) {
    fun resolveModel(task: TaskType, preferredModelId: String?): ChakuliModel? {
        val preferred = preferredModelId?.let { modelRepo.getModelById(it) }
        if (preferred != null && task in preferred.supportedTasks && modelRepo.isModelDownloaded(preferred)) {
            return preferred
        }
        // DisasterTriage needs image + audio — pick smallest downloaded multimodal model
        return modelRepo.getPreferredMultimodalModel(needsImage = true, needsAudio = true)
            ?: modelRepo.getModelsForTask(task).firstOrNull { modelRepo.isModelDownloaded(it) }
    }
}
