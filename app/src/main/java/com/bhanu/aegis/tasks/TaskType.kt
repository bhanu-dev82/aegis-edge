package com.bhanu.aegis.tasks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.ui.graphics.vector.ImageVector

sealed class TaskType(
    val id: String,
    val label: String,
    val supportImage: Boolean = false,
    val supportAudio: Boolean = false,
    val defaultMaxTokens: Int = 8192,
) {
    data object DisasterTriage : TaskType(
        id             = "disaster_triage",
        label          = "Triage",
        supportImage   = true,
        supportAudio   = true,
        defaultMaxTokens = 8192,
    )

    companion object {
        val all: List<TaskType> get() = listOf(DisasterTriage)
        fun fromId(id: String): TaskType? = all.find { it.id == id }
    }
}
