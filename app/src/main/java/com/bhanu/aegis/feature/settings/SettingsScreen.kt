package com.bhanu.aegis.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bhanu.aegis.core.data.model.ModelDownloadStatus
import com.bhanu.aegis.core.data.repository.EngineMode
import com.bhanu.aegis.core.data.repository.ThemeMode
import com.bhanu.aegis.core.theme.emergencyGradientStart
import com.bhanu.aegis.core.ui.GlassCard
import com.bhanu.aegis.core.ui.GlassElevation
import com.bhanu.aegis.core.ui.pressScale
import com.bhanu.aegis.feature.modelmanager.ModelCard
import com.bhanu.aegis.feature.modelmanager.ModelManagerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    modelManagerViewModel: ModelManagerViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val engineMode by viewModel.engineMode.collectAsState(initial = EngineMode.AUTO)
    val modelUiState by modelManagerViewModel.uiState.collectAsState()
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocalHospital,
                            contentDescription = null,
                            tint = emergencyGradientStart,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Settings")
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainerLow,
                        )
                    )
                )
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Models ──────────────────────────────────────────────────
            SettingsSectionHeader("Models")

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                modelManagerViewModel.modelRepository.allModels.forEach { model ->
                    val status = modelUiState.downloadStatuses[model.id]
                        ?: if (modelManagerViewModel.modelRepository.isModelDownloaded(model))
                            ModelDownloadStatus.Downloaded
                        else ModelDownloadStatus.NotDownloaded
                    ModelCard(
                        model = model,
                        status = status,
                        onDownload = { modelManagerViewModel.downloadModel(model) },
                        onCancel = { modelManagerViewModel.cancelDownload(model) },
                        onDelete = { modelManagerViewModel.deleteModel(model) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Appearance ──────────────────────────────────────────────
            SettingsSectionHeader("Appearance")

            GlassCard(elevation = GlassElevation.Low, modifier = Modifier.fillMaxWidth()) {
                Column {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pressScale()
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = themeMode == mode, onClick = { viewModel.setThemeMode(mode) })
                            Text(
                                text = when (mode) {
                                    ThemeMode.SYSTEM -> "Follow system"
                                    ThemeMode.LIGHT  -> "Light"
                                    ThemeMode.DARK   -> "Dark"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        if (index < ThemeMode.entries.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Model Performance ───────────────────────────────────────
            SettingsSectionHeader("Model Performance")

            GlassCard(elevation = GlassElevation.Low, modifier = Modifier.fillMaxWidth()) {
                Column {
                    listOf(
                        Triple(EngineMode.AUTO, "Smart (recommended)", "Keeps one engine loaded, unloads others automatically"),
                        Triple(EngineMode.PERFORMANCE, "Performance", "Keep engine in memory — faster, uses more RAM"),
                        Triple(EngineMode.MEMORY_SAVER, "Memory Saver", "Unload when idle — slower to return, saves RAM"),
                    ).forEachIndexed { index, (mode, label, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pressScale()
                                .clickable { viewModel.setEngineMode(mode) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = engineMode == mode, onClick = { viewModel.setEngineMode(mode) })
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(text = label, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (index < 2) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── About ───────────────────────────────────────────────────
            SettingsSectionHeader("About")

            GlassCard(elevation = GlassElevation.Low, modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingsRow(
                        title = "App version",
                        subtitle = "Aegis-Edge 1.0.0 • Gemma 4 Good Hackathon",
                        onClick = null,
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        title = "By Devanshu Studios",
                        subtitle = "In memory of Devanshu Nagpure (2003–2021)",
                        onClick = null,
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        title = "Privacy",
                        subtitle = "Tap to read our privacy commitment",
                        onClick = { showPrivacyDialog = true },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        title = "Open Source",
                        subtitle = "Apache 2.0 • LiteRT-LM, Jetpack Compose, Hilt",
                        onClick = null,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Privacy dialog — no URL, just the commitment text
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Commitment") },
            text = {
                Text(
                    "Aegis-Edge stores no data outside your device.\n\n" +
                    "• All AI inference runs locally using LiteRT-LM — no data is sent to any server.\n" +
                    "• Photos, voice recordings, and patient information never leave your phone.\n" +
                    "• No analytics, no crash reporting, no telemetry of any kind.\n" +
                    "• The app works fully offline — no internet connection is required after the model is downloaded.\n\n" +
                    "This is open-source software. You can verify these claims by reading the source code.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Got it") }
            },
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.pressScale().clickable(onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
