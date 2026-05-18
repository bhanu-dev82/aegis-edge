package com.bhanu.aegis.core.llm

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.ToolProvider
import kotlinx.coroutines.CancellationException

private const val TAG = "ChakuliEngine"

/**
 * Manages the LiteRT-LM Engine lifecycle for Chakuli.
 *
 * Design follows the Gallery's LlmChatModelHelper singleton pattern.
 * Each model gets its own (Engine, Conversation) session pair.
 * Inference MUST be called from a background thread (engine.initialize() blocks).
 *
 * API reference: LiteRT-LM Config.kt (main branch, verified April 8 2026)
 */
object LiteRTEngineManager {

    private data class EngineSession(
        val engine: Engine,
        var conversation: Conversation,
        val config: SessionConfig,
    )

    data class SessionConfig(
        val modelPath: String,
        val supportImage: Boolean,
        val supportAudio: Boolean,
        val maxTokens: Int,
        val systemInstruction: String?,
        val tools: List<ToolProvider>,
        val samplerConfig: SamplerConfig,
        val useConstrainedDecoding: Boolean,
    )

    private val sessions = mutableMapOf<String, EngineSession>()
    private val lock = Any()

    // Pending callbacks for in-progress initializations — prevents double-init race
    private val initCallbacks = mutableMapOf<String, MutableList<(String) -> Unit>>()

    /**
     * Initialize a model engine session.
     * MUST be called from Dispatchers.Default — engine.initialize() is blocking (10+ seconds).
     *
     * @param modelKey  Unique key for this session (typically model ID)
     * @param onDone    Callback with empty string on success, error message on failure
     */
    fun initialize(
        context: Context,
        modelKey: String,
        sessionConfig: SessionConfig,
        onDone: (error: String) -> Unit,
    ) {
        try {
            // ── ONE MODEL AT A TIME ──
            // Unload ALL other sessions before loading a new model.
            // This is a hard constraint for 8GB devices — simultaneous models would OOM.
            synchronized(lock) {
                sessions.keys
                    .filter { it != modelKey }
                    .forEach { key -> cleanUpInternal(key) }
            }

            // Log available RAM for diagnostics
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
            val availRamMb = memInfo.availMem / 1_048_576
            Log.d(TAG, "Available RAM: ${availRamMb} MB, Total RAM: ${memInfo.totalMem / 1_048_576} MB")

            synchronized(lock) {
                val existingSession = sessions[modelKey]
                if (existingSession != null) {
                    val oldConfig = existingSession.config
                    val canReuseEngine = oldConfig.modelPath == sessionConfig.modelPath &&
                            oldConfig.useConstrainedDecoding == sessionConfig.useConstrainedDecoding &&
                            oldConfig.supportImage == sessionConfig.supportImage &&
                            oldConfig.supportAudio == sessionConfig.supportAudio &&
                            oldConfig.maxTokens == sessionConfig.maxTokens

                    if (canReuseEngine) {
                        Log.d(TAG, "Reusing existing engine for $modelKey (fast path)")
                        existingSession.conversation.close()

                        val systemContents = sessionConfig.systemInstruction?.let {
                            Contents.of(Content.Text(it))
                        }
                        val newConversation = existingSession.engine.createConversation(
                            ConversationConfig(
                                systemInstruction    = systemContents,
                                tools                = sessionConfig.tools,
                                samplerConfig        = sessionConfig.samplerConfig,
                                automaticToolCalling = true,
                            )
                        )

                        sessions[modelKey] = existingSession.copy(
                            conversation = newConversation,
                            config       = sessionConfig
                        )
                        onDone("")
                        return
                    }
                }

                // Already initializing — queue this callback instead of starting a second init
                if (initCallbacks.containsKey(modelKey)) {
                    Log.d(TAG, "Init already in progress for $modelKey — queuing callback")
                    initCallbacks[modelKey]?.add(onDone)
                    return
                }

                // First caller — register callback list and start init
                initCallbacks[modelKey] = mutableListOf(onDone)

                cleanUpInternal(modelKey)
            }

            Log.d(TAG, "Initializing engine for $modelKey...")

            // Vision backend: GPU for Gemma 4 image understanding (requires litertlm ≥0.11.0)
            // Audio backend: must be CPU for Gemma audio
            // FALLBACK: If vision init fails, retry with CPU vision, then without vision.
            var engineConfig = EngineConfig(
                modelPath    = sessionConfig.modelPath,
                backend      = if (sessionConfig.useConstrainedDecoding) Backend.CPU()
                               else Backend.GPU(),
                visionBackend = if (sessionConfig.supportImage) Backend.GPU() else null,
                audioBackend  = if (sessionConfig.supportAudio) Backend.CPU() else null,
                maxNumTokens  = sessionConfig.maxTokens,
                cacheDir = context.cacheDir.path,
            )

            var engine: Engine
            try {
                engine = Engine(engineConfig)
                engine.initialize()   // BLOCKS — called from background thread only
                Log.d(TAG, "Engine initialized for $modelKey (vision=GPU)")
            } catch (visionError: Exception) {
                if (sessionConfig.supportImage && visionError.message?.contains("Vision") == true) {
                    Log.w(TAG, "GPU vision failed: ${visionError.message}. Trying CPU vision...")
                    try {
                        engineConfig = EngineConfig(
                            modelPath    = sessionConfig.modelPath,
                            backend      = if (sessionConfig.useConstrainedDecoding) Backend.CPU()
                                           else Backend.GPU(),
                            visionBackend = Backend.CPU(),
                            audioBackend  = if (sessionConfig.supportAudio) Backend.CPU() else null,
                            maxNumTokens  = sessionConfig.maxTokens,
                            cacheDir = context.cacheDir.path,
                        )
                        engine = Engine(engineConfig)
                        engine.initialize()
                        Log.d(TAG, "Engine initialized for $modelKey (vision=CPU fallback)")
                    } catch (cpuVisionError: Exception) {
                        Log.w(TAG, "CPU vision also failed: ${cpuVisionError.message}. Disabling vision...")
                        engineConfig = EngineConfig(
                            modelPath    = sessionConfig.modelPath,
                            backend      = if (sessionConfig.useConstrainedDecoding) Backend.CPU()
                                           else Backend.GPU(),
                            visionBackend = null,
                            audioBackend  = if (sessionConfig.supportAudio) Backend.CPU() else null,
                            maxNumTokens  = sessionConfig.maxTokens,
                            cacheDir = context.cacheDir.path,
                        )
                        engine = Engine(engineConfig)
                        engine.initialize()
                        Log.d(TAG, "Engine initialized for $modelKey (NO vision — text+audio only)")
                    }
                } else {
                    throw visionError
                }
            }

            // For agent tasks: enable constrained decoding for accurate tool calls
            @OptIn(ExperimentalApi::class)
            if (sessionConfig.useConstrainedDecoding) {
                ExperimentalFlags.enableConversationConstrainedDecoding = true
            }

            val systemContents = sessionConfig.systemInstruction?.let {
                Contents.of(Content.Text(it))
            }

            val conversationConfig = ConversationConfig(
                systemInstruction   = systemContents,
                tools               = sessionConfig.tools,
                samplerConfig       = sessionConfig.samplerConfig,
                automaticToolCalling = true,
            )

            val conversation = engine.createConversation(conversationConfig)

            // Reset constrained decoding flag after createConversation
            @OptIn(ExperimentalApi::class)
            if (sessionConfig.useConstrainedDecoding) {
                ExperimentalFlags.enableConversationConstrainedDecoding = false
            }

            synchronized(lock) {
                sessions[modelKey] = EngineSession(engine, conversation, sessionConfig)
            }
            Log.d(TAG, "Session created for $modelKey")

            // Notify all waiters — success
            synchronized(lock) {
                initCallbacks.remove(modelKey)?.forEach { it("") }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize engine for $modelKey", e)
            // Remove callbacks BEFORE notifying so a retry call can register fresh
            val waiters = synchronized(lock) { initCallbacks.remove(modelKey) ?: mutableListOf() }
            waiters.forEach { it(e.message ?: "Unknown engine initialization error") }
        }
    }

    /**
     * Send a message and stream the response via [onResult].
     *
     * @param images     Bitmaps converted to PNG byte arrays (Content.ImageBytes)
     * @param audioClip  Raw PCM 16kHz mono audio bytes (Content.AudioBytes)
     * @param onResult   (responseChunk, isDone, thinkingChunk) — called for each token
     */
    fun sendMessage(
        modelKey: String,
        text: String,
        images: List<ByteArray> = emptyList(),
        audioClip: ByteArray? = null,
        extraContext: Map<String, Any> = emptyMap(),
        onResult: (text: String, done: Boolean, thinking: String?) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = synchronized(lock) { sessions[modelKey] }
        if (session == null) {
            onError("Model '$modelKey' is not initialized. Please wait for loading to complete.")
            return
        }

        // Images and audio MUST precede text (per Gallery LlmChatModelHelper.kt)
        val contents = mutableListOf<Content>()
        for (imageBytes in images) {
            contents.add(Content.ImageBytes(imageBytes))
        }
        audioClip?.let {
            contents.add(Content.AudioBytes(it))
        }
        if (text.trim().isNotEmpty()) {
            contents.add(Content.Text(text))
        }

        session.conversation.sendMessageAsync(
            Contents.of(contents),
            object : MessageCallback {
                override fun onMessage(message: Message) {
                    val thinking = message.channels?.get("thought")
                    onResult(message.toString(), false, thinking)
                }
                override fun onDone() {
                    onResult("", true, null)
                }
                override fun onError(throwable: Throwable) {
                    if (throwable is CancellationException) {
                        // User cancelled — treat as done
                        onResult("", true, null)
                    } else {
                        Log.e(TAG, "Inference error for $modelKey", throwable)
                        onError("Inference error: ${throwable.message}")
                    }
                }
            },
            extraContext,
        )
    }

    /** Cancel any ongoing generation for this model. */
    fun cancelGeneration(modelKey: String) {
        synchronized(lock) { sessions[modelKey] }?.conversation?.cancelProcess()
        Log.d(TAG, "Cancelled generation for $modelKey")
    }

    /**
     * Reset the conversation history while keeping the engine alive.
     * Useful for "New Chat" without reloading the model (expensive).
     */
    fun resetConversation(
        modelKey: String,
        samplerConfig: SamplerConfig? = null,
        onDone: (error: String) -> Unit,
    ) {
        val session = synchronized(lock) { sessions[modelKey] } ?: return onDone("Model not initialized")
        try {
            session.conversation.close()
            val systemContents = session.config.systemInstruction?.let {
                Contents.of(Content.Text(it))
            }
            val effectiveSampler = samplerConfig ?: session.config.samplerConfig
            val newConversation = session.engine.createConversation(
                ConversationConfig(
                    systemInstruction    = systemContents,
                    tools                = session.config.tools,
                    samplerConfig        = effectiveSampler,
                    automaticToolCalling = true,
                )
            )
            sessions[modelKey] = session.copy(
                conversation = newConversation,
                config       = session.config.copy(samplerConfig = effectiveSampler),
            )
            onDone("")
        } catch (e: Exception) {
            onDone(e.message ?: "Failed to reset conversation")
        }
    }

    /** Release all resources for this model session. */
    fun cleanUp(modelKey: String, onDone: () -> Unit = {}) {
        synchronized(lock) {
            initCallbacks.remove(modelKey)
            cleanUpInternal(modelKey)
        }
        onDone()
    }

    private fun cleanUpInternal(modelKey: String) {
        // Do NOT remove initCallbacks here — active callbacks must survive cleanup
        sessions.remove(modelKey)?.let { session ->
            try {
                session.conversation.close()
                session.engine.close()
                Log.d(TAG, "Cleaned up session for $modelKey")
            } catch (e: Exception) {
                Log.w(TAG, "Error during cleanup for $modelKey", e)
            }
        }
    }

    /** Release all sessions (called on memory pressure). */
    fun cleanUpAll() {
        synchronized(lock) {
            initCallbacks.clear()
            sessions.keys.toList().forEach { cleanUpInternal(it) }
        }
    }

    /**
     * Handle system memory pressure callbacks.
     * Called from ChakuliApplication.onTrimMemory().
     */
    fun onTrimMemory(level: Int) {
        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w(TAG, "CRITICAL memory pressure — unloading all engines")
                cleanUpAll()
            }
            // LOW and MODERATE: keep engines alive — camera/gallery transitions trigger LOW
            // and we don't want to force the user to reload the model after every photo pick.
            else -> {
                Log.d(TAG, "Memory pressure level $level — engines kept alive")
            }
        }
    }

    fun isInitialized(modelKey: String): Boolean = synchronized(lock) { sessions.containsKey(modelKey) }
}
