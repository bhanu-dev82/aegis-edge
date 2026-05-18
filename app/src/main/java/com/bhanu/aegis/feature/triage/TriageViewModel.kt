package com.bhanu.aegis.feature.triage

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhanu.aegis.core.data.repository.ModelRepository
import com.bhanu.aegis.core.llm.LiteRTEngineManager
import com.bhanu.aegis.core.llm.ModelRouter
import com.bhanu.aegis.core.llm.SystemPrompts
import com.bhanu.aegis.tasks.TaskType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

private const val TAG = "TriageViewModel"

// ─────────────────────────────────────────────────────────────────────────────
//  Chat message model (inline assistant panel — no separate screen needed)
// ─────────────────────────────────────────────────────────────────────────────

enum class AegisChatRole { USER, MODEL }

data class AegisChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: AegisChatRole,
    val text: String,
    val isStreaming: Boolean = false,
)

// ─────────────────────────────────────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class TriageViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    val modelRepository: ModelRepository,
    private val modelRouter: ModelRouter,
) : ViewModel() {

    private val taskType = TaskType.DisasterTriage

    val model = modelRouter.resolveModel(taskType, null)
        ?: modelRepository.getModelsForTask(taskType).firstOrNull()

    // Single session key — triage and chat share the same loaded engine.
    // The system prompt covers both triage JSON output and free-form assistant chat.
    private val sessionKey = "${model?.id ?: "gemma-4-e2b-it"}-${taskType.id}"

    private val _uiState = MutableStateFlow(TriageUiState())
    val uiState: StateFlow<TriageUiState> = _uiState.asStateFlow()

    init {
        if (LiteRTEngineManager.isInitialized(sessionKey)) {
            _uiState.update { it.copy(engineState = TriageEngineState.Ready) }
        }
    }

    // ── Engine lifecycle ──────────────────────────────────────────────────────

    fun loadModel() {
        val currentModel = model ?: return
        if (_uiState.value.engineState == TriageEngineState.Loading ||
            _uiState.value.engineState == TriageEngineState.Ready) return

        if (!modelRepository.isModelDownloaded(currentModel)) {
            _uiState.update {
                it.copy(engineState = TriageEngineState.Error(
                    "\"${currentModel.displayName}\" is not downloaded. Go to Settings → Models to download it."
                ))
            }
            return
        }

        _uiState.update { it.copy(engineState = TriageEngineState.Loading) }

        viewModelScope.launch(Dispatchers.Default) {
            val modelFile = modelRepository.getModelFile(currentModel)
            LiteRTEngineManager.initialize(
                context = context,
                modelKey = sessionKey,
                sessionConfig = LiteRTEngineManager.SessionConfig(
                    modelPath = modelFile.absolutePath,
                    supportImage = true,
                    supportAudio = true,
                    maxTokens = 8192,
                    systemInstruction = SystemPrompts.AEGIS_ASSISTANT,
                    tools = emptyList(),
                    samplerConfig = com.google.ai.edge.litertlm.SamplerConfig(
                        topK = 40,
                        topP = 0.95,
                        temperature = 0.7,
                    ),
                    useConstrainedDecoding = false,
                ),
                onDone = { error ->
                    if (error.isEmpty()) {
                        _uiState.update { it.copy(engineState = TriageEngineState.Ready) }
                    } else {
                        _uiState.update { it.copy(engineState = TriageEngineState.Error(error)) }
                    }
                },
            )
        }
    }

    fun checkEngineAlive() {
        if (_uiState.value.engineState == TriageEngineState.Ready &&
            !LiteRTEngineManager.isInitialized(sessionKey)) {
            _uiState.update { it.copy(engineState = TriageEngineState.Idle) }
        }
    }

    // ── Triage inference ──────────────────────────────────────────────────────

    fun executeTriage(
        symptoms: String,
        imageBytes: List<ByteArray> = emptyList(),
        audioBytes: ByteArray? = null,
    ) {
        if (_uiState.value.isProcessing) return
        if (symptoms.isBlank() && imageBytes.isEmpty() && audioBytes == null) return

        _uiState.update { it.copy(isProcessing = true, currentThinking = "", currentResponse = "") }

        viewModelScope.launch(Dispatchers.Default) {
            val fullResponse = StringBuilder()
            val fullThinking = StringBuilder()
            val extraCtx = mapOf("enable_thinking" to "true")

            val patientNumber = _uiState.value.patientCounter + 1
            _uiState.update { it.copy(patientCounter = patientNumber) }

            val prompt = buildString {
                append("TRIAGE_REQUEST:\n")
                if (symptoms.isNotBlank()) append("Symptoms: $symptoms\n")
                if (imageBytes.isNotEmpty()) append("Image of patient/wound/medicine attached.\n")
                if (audioBytes != null) append("Voice notes attached.\n")
                append("\nProvide your triage assessment as JSON.")
            }

            LiteRTEngineManager.sendMessage(
                modelKey = sessionKey,
                text = prompt,
                images = imageBytes,
                audioClip = audioBytes,
                extraContext = extraCtx,
                onResult = { chunk, done, thinkingChunk ->
                    val filtered = if (chunk.startsWith("<ctrl")) "" else chunk
                    if (thinkingChunk != null) fullThinking.append(thinkingChunk)
                    if (filtered.isNotEmpty()) fullResponse.append(filtered)

                    _uiState.update { state ->
                        state.copy(
                            currentThinking = fullThinking.toString(),
                            currentResponse = fullResponse.toString()
                                .trimStart()
                                .replace("<|im_end|>", "")
                                .replace("<|endoftext|>", ""),
                        )
                    }

                    if (done) {
                        val patient = parseTriageResponse(
                            response = fullResponse.toString().trim(),
                            reasoning = fullThinking.toString(),
                            symptoms = symptoms,
                            hasImage = imageBytes.isNotEmpty(),
                            patientNumber = patientNumber,
                        )
                        _uiState.update { state ->
                            state.copy(
                                patientQueue = (state.patientQueue + patient).sortedBy { it.color.sortOrder },
                                isProcessing = false,
                                currentThinking = "",
                                currentResponse = "",
                                lastPatient = patient,
                            )
                        }
                    }
                },
                onError = { error ->
                    Log.e(TAG, "Triage error: $error")
                    _uiState.update { it.copy(
                        isProcessing = false,
                        currentResponse = "Error: $error",
                        patientCounter = patientNumber - 1,
                    ) }
                },
            )
        }
    }

    fun cancelTriage() {
        LiteRTEngineManager.cancelGeneration(sessionKey)
    }

    fun clearQueue() {
        _uiState.update { it.copy(patientQueue = emptyList(), lastPatient = null, patientCounter = 0) }
    }

    fun removePatient(patientId: String) {
        _uiState.update { state ->
            state.copy(patientQueue = state.patientQueue.filter { it.id != patientId })
        }
    }

    // ── Inline chat (assistant panel) ─────────────────────────────────────────

    /**
     * Send a free-form message to the assistant panel.
     * Uses the same loaded engine — no reload needed.
     * The system prompt (AEGIS_ASSISTANT) handles both triage JSON and chat naturally.
     */
    fun sendChatMessage(text: String) {
        if (_uiState.value.isChatGenerating) return
        if (text.isBlank()) return

        val userMsg = AegisChatMessage(role = AegisChatRole.USER, text = text)
        val modelMsgId = UUID.randomUUID().toString()
        val modelMsg = AegisChatMessage(id = modelMsgId, role = AegisChatRole.MODEL, text = "", isStreaming = true)

        _uiState.update { state ->
            state.copy(
                chatMessages = state.chatMessages + userMsg + modelMsg,
                isChatGenerating = true,
            )
        }

        viewModelScope.launch(Dispatchers.Default) {
            val fullResponse = StringBuilder()

            LiteRTEngineManager.sendMessage(
                modelKey = sessionKey,
                text = text,
                onResult = { chunk, done, _ ->
                    val filtered = if (chunk.startsWith("<ctrl")) "" else chunk
                    if (filtered.isNotEmpty()) fullResponse.append(filtered)

                    val cleanText = fullResponse.toString()
                        .trimStart()
                        .replace("<|im_end|>", "")
                        .replace("<|endoftext|>", "")

                    _uiState.update { state ->
                        state.copy(
                            chatMessages = state.chatMessages.map { msg ->
                                if (msg.id == modelMsgId) msg.copy(text = cleanText, isStreaming = !done)
                                else msg
                            },
                            isChatGenerating = !done,
                        )
                    }
                },
                onError = { error ->
                    Log.e(TAG, "Chat error: $error")
                    _uiState.update { state ->
                        state.copy(
                            chatMessages = state.chatMessages.map { msg ->
                                if (msg.id == modelMsgId) msg.copy(text = "Error: $error", isStreaming = false)
                                else msg
                            },
                            isChatGenerating = false,
                        )
                    }
                },
            )
        }
    }

    fun cancelChat() {
        LiteRTEngineManager.cancelGeneration(sessionKey)
        _uiState.update { state ->
            state.copy(
                chatMessages = state.chatMessages.map { msg ->
                    if (msg.isStreaming) msg.copy(isStreaming = false) else msg
                },
                isChatGenerating = false,
            )
        }
    }

    fun clearChat() {
        _uiState.update { it.copy(chatMessages = emptyList(), isChatGenerating = false) }
    }

    // ── JSON parsing ──────────────────────────────────────────────────────────

    private fun parseTriageResponse(
        response: String,
        reasoning: String,
        symptoms: String,
        hasImage: Boolean,
        patientNumber: Int,
    ): TriagePatient {
        try {
            val jsonStart = response.indexOf('{')
            val jsonEnd = response.lastIndexOf('}')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val json = JSONObject(response.substring(jsonStart, jsonEnd + 1))
                val colorStr = json.optString("color", "YELLOW").uppercase()
                val action = json.optString("action", "Monitor patient closely")
                val jsonReasoning = json.optString("reasoning", reasoning)
                return TriagePatient(
                    patientNumber = patientNumber,
                    color = TriageColor.fromString(colorStr) ?: TriageColor.YELLOW,
                    action = action,
                    reasoning = jsonReasoning.ifBlank { reasoning },
                    symptoms = symptoms,
                    hasImage = hasImage,
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse triage JSON, using fallback", e)
        }
        return TriagePatient(
            patientNumber = patientNumber,
            color = TriageColor.YELLOW,
            action = response.take(200).ifBlank { "Monitor patient — could not parse triage output" },
            reasoning = reasoning.ifBlank { response },
            symptoms = symptoms,
            hasImage = hasImage,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  UI State
// ─────────────────────────────────────────────────────────────────────────────

data class TriageUiState(
    val engineState: TriageEngineState = TriageEngineState.Idle,
    // Triage
    val isProcessing: Boolean = false,
    val patientQueue: List<TriagePatient> = emptyList(),
    val patientCounter: Int = 0,
    val currentThinking: String = "",
    val currentResponse: String = "",
    val lastPatient: TriagePatient? = null,
    // Inline chat
    val chatMessages: List<AegisChatMessage> = emptyList(),
    val isChatGenerating: Boolean = false,
)

sealed class TriageEngineState {
    data object Idle    : TriageEngineState()
    data object Loading : TriageEngineState()
    data object Ready   : TriageEngineState()
    data class Error(val message: String) : TriageEngineState()
}
