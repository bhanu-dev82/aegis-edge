package com.bhanu.aegis.feature.triage

import androidx.compose.ui.graphics.Color
import java.util.UUID

/**
 * Represents a single triaged patient in the Aegis-Edge queue.
 * patientNumber is a sequential 1-based counter (001, 002…) assigned at triage time,
 * used for RAG-style references in the chat screen ("how to treat patient 001").
 */
data class TriagePatient(
    val id: String = UUID.randomUUID().toString(),
    val patientNumber: Int = 0,
    val color: TriageColor,
    val action: String,
    val reasoning: String,
    val symptoms: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val hasImage: Boolean = false,
) {
    /** Zero-padded label, e.g. "001", "012" */
    val numberLabel: String get() = patientNumber.toString().padStart(3, '0')
}

/**
 * START Triage Protocol color codes.
 * Sorted by priority — RED (most urgent) → BLACK (deceased).
 */
enum class TriageColor(
    val label: String,
    val sortOrder: Int,
    val bgColor: Color,
    val textColor: Color,
) {
    RED(
        label = "Immediate",
        sortOrder = 0,
        bgColor = Color(0xFFE53935),
        textColor = Color.White,
    ),
    YELLOW(
        label = "Delayed",
        sortOrder = 1,
        bgColor = Color(0xFFFDD835),
        textColor = Color(0xFF212121),
    ),
    GREEN(
        label = "Minor",
        sortOrder = 2,
        bgColor = Color(0xFF43A047),
        textColor = Color.White,
    ),
    BLACK(
        label = "Deceased",
        sortOrder = 3,
        bgColor = Color(0xFF212121),
        textColor = Color.White,
    );

    companion object {
        fun fromString(value: String): TriageColor? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}
