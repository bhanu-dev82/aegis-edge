package com.bhanu.aegis.feature.modelmanager

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bhanu.aegis.core.data.model.ChakuliModel
import com.bhanu.aegis.core.data.model.ModelDownloadStatus
import com.bhanu.aegis.core.data.model.ModelTier
import com.bhanu.aegis.core.theme.emergencyGradientStart
import com.bhanu.aegis.core.ui.GlassCard
import com.bhanu.aegis.core.ui.GlassElevation
import com.bhanu.aegis.core.ui.pressScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagerScreen(
    onNavigateUp: () -> Unit,
    viewModel: ModelManagerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val importDisplayName = "Custom Model"
    val pickModelFile = rememberModelFilePicker(
        modelRepository = viewModel.modelRepository,
        displayName     = importDisplayName,
        onImported      = viewModel::onModelImported,
    )

    val officialModels = viewModel.modelRepository.allModels.filter { it.tier == ModelTier.OFFICIAL }

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
                        Text("Models")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        LazyColumn(
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
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Import local model
            item {
                GlassCard(
                    elevation = GlassElevation.Medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.FolderOpen,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Import local model",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                "Load a .litertlm file from your device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedButton(
                            onClick = pickModelFile,
                            modifier = Modifier.pressScale(),
                        ) {
                            Text("Browse")
                        }
                    }
                }
            }

            // ── Official Models ──────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
                    Text(
                        text = "Official Models",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Gemma 4 · Apache 2.0 · No token required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(officialModels) { model ->
                val status = uiState.downloadStatuses[model.id]
                    ?: if (viewModel.modelRepository.isModelDownloaded(model)) ModelDownloadStatus.Downloaded
                    else ModelDownloadStatus.NotDownloaded
                ModelCard(
                    model = model,
                    status = status,
                    onDownload = { viewModel.downloadModel(model) },
                    onCancel = { viewModel.cancelDownload(model) },
                    onDelete = { viewModel.deleteModel(model) },
                )
            }
        }
    }
}

@Composable
fun ModelCard(
    model: ChakuliModel,
    status: ModelDownloadStatus,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val elevation = when (status) {
        is ModelDownloadStatus.Downloaded  -> GlassElevation.High
        is ModelDownloadStatus.Downloading -> GlassElevation.Medium
        else                               -> GlassElevation.Low
    }

    GlassCard(
        elevation = elevation,
        modifier = modifier.fillMaxWidth().animateContentSize(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            model.displayName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )
                        if (status is ModelDownloadStatus.Downloaded) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.Outlined.CheckCircle,
                                "Downloaded",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                    Text(
                        model.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                when (status) {
                    is ModelDownloadStatus.NotDownloaded, is ModelDownloadStatus.Error ->
                        IconButton(onClick = onDownload, modifier = Modifier.pressScale()) {
                            Icon(Icons.Outlined.Download, "Download", tint = MaterialTheme.colorScheme.primary)
                        }
                    is ModelDownloadStatus.Downloading ->
                        IconButton(onClick = onCancel, modifier = Modifier.pressScale()) {
                            Icon(Icons.Outlined.Cancel, "Cancel", tint = MaterialTheme.colorScheme.error)
                        }
                    is ModelDownloadStatus.Downloaded ->
                        IconButton(onClick = onDelete, modifier = Modifier.pressScale()) {
                            Icon(Icons.Outlined.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    else -> Unit
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(model.displaySizeMb, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${model.maxContextLength / 1000}K context", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${model.minRamGb}GB RAM min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (status is ModelDownloadStatus.Downloading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { status.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
                Spacer(Modifier.height(4.dp))
                val speedLabel = ModelDownloadWorker.formatSpeed(status.speedBps)
                val etaLabel   = ModelDownloadWorker.formatEta(status.remainingSec)
                Text(
                    text = buildString {
                        if (speedLabel.isNotEmpty()) append("$speedLabel · ")
                        append("${status.progressPercent}%")
                        if (etaLabel.isNotEmpty()) append(etaLabel)
                        append(" — ${status.downloadedBytes / 1_000_000}MB / ${model.displaySizeMb}")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (status is ModelDownloadStatus.Error) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "⚠ ${status.message}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
