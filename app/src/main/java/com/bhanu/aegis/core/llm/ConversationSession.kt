package com.bhanu.aegis.core.llm

import com.bhanu.aegis.tasks.TaskType

/**
 * Represents a single chat message in the conversation history.
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val thinkingContent: String? = null,
    val imageByteArrays: List<ByteArray> = emptyList(),
    val hasAudio: Boolean = false,
    val audioBytes: ByteArray? = null,
    val toolCalls: List<ToolCallRecord> = emptyList(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis(),
    // Skill-produced webview to render inline (url + aspectRatio)
    val skillWebviewUrl: String? = null,
    val skillWebviewAspectRatio: Float = 1.333f,
    val skillWebviewIframe: Boolean = false,
)

enum class MessageRole { USER, MODEL }

/**
 * Record of a single tool call made during an agentic response.
 */
data class ToolCallRecord(
    val toolName: String,
    val params: Map<String, String>,
    val result: String,
    val isSuccess: Boolean,
    val durationMs: Long,
)

/**
 * Encapsulates all state for one open conversation session.
 */
data class ConversationSession(
    val modelKey: String,
    val taskType: TaskType,
    val messages: MutableList<ChatMessage> = mutableListOf(),
    val isThinkingEnabled: Boolean = false,
)
