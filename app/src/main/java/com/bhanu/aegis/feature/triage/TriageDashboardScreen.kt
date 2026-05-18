package com.bhanu.aegis.feature.triage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.bhanu.aegis.core.theme.emergencyGradientStart
import com.bhanu.aegis.core.theme.offlineGreen
import com.bhanu.aegis.core.ui.GlassCard
import com.bhanu.aegis.core.ui.GlassElevation
import com.bhanu.aegis.feature.triage.components.AudioRecorderButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriageDashboardScreen(
    onNavigateToModels: () -> Unit,
    viewModel: TriageViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var symptoms by remember { mutableStateOf("") }
    var capturedImages by remember { mutableStateOf<List<ByteArray>>(emptyList()) }
    var capturedAudio by remember { mutableStateOf<ByteArray?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Chat bottom sheet state
    var showChatSheet by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }
    val chatSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkEngineAlive()
                viewModel.loadModel()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val newImages = uris.mapNotNull { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    resizeImageForModel(stream.readBytes())
                }
            } catch (_: Exception) { null }
        }
        capturedImages = (capturedImages + newImages).takeLast(4)
    }

    var showCameraView by remember { mutableStateOf(false) }

    if (showImageSourceDialog) {
        @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showImageSourceDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentWindowInsets = { WindowInsets(0) }
        ) {
            Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Text(
                    "Add Photo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                androidx.compose.material3.ListItem(
                    headlineContent = { Text("Take photo with Camera") },
                    leadingContent = { Icon(Icons.Filled.Add, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showImageSourceDialog = false
                        showCameraView = true
                    },
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
                androidx.compose.material3.ListItem(
                    headlineContent = { Text("Choose from Gallery") },
                    leadingContent = { Icon(Icons.Filled.Image, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showImageSourceDialog = false
                        imagePickerLauncher.launch("image/*")
                    },
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
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
                        Column {
                            Text(
                                text = "Aegis-Edge",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            )
                            Text(
                                text = "Offline Disaster Triage • Gemma 4",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    // Chat button — opens the inline assistant panel
                    FilledTonalButton(
                        onClick = { showChatSheet = true },
                        modifier = Modifier.padding(end = 8.dp).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Filled.Chat,
                            contentDescription = "Ask assistant",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Chat", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.surfaceContainerLow,
                )))
                .padding(innerPadding),
        ) {
            when (val state = uiState.engineState) {
                is TriageEngineState.Idle, is TriageEngineState.Loading -> {
                    EngineLoadingView(
                        isLoading = state is TriageEngineState.Loading,
                        modelName = viewModel.model?.displayName ?: "Gemma 4 E2B",
                    )
                }
                is TriageEngineState.Error -> {
                    EngineErrorView(state.message, { viewModel.loadModel() }, onNavigateToModels)
                }
                is TriageEngineState.Ready -> {
                    TriageContent(
                        uiState = uiState,
                        symptoms = symptoms,
                        imageCount = capturedImages.size,
                        hasAudio = capturedAudio != null,
                        onSymptomsChange = { symptoms = it },
                        onPickImages = { showImageSourceDialog = true },
                        onClearImages = { capturedImages = emptyList() },
                        onAudioCaptured = { capturedAudio = it },
                        onClearAudio = { capturedAudio = null },
                        onExecuteTriage = {
                            viewModel.executeTriage(
                                symptoms = symptoms,
                                imageBytes = capturedImages,
                                audioBytes = capturedAudio,
                            )
                            symptoms = ""
                            capturedImages = emptyList()
                            capturedAudio = null
                        },
                        onQuickAction = { prompt ->
                            // Open chat sheet with the quick action pre-filled
                            chatInput = prompt
                            showChatSheet = true
                        },
                        onCancelTriage = { viewModel.cancelTriage() },
                        onClearQueue = { viewModel.clearQueue() },
                        onRemovePatient = { viewModel.removePatient(it) },
                        onOpenChatForPatient = { patient ->
                            // Pre-fill chat with patient context, ask user to confirm name
                            chatInput = "I need help with Patient #${patient.numberLabel} " +
                                "(${patient.color.name} — ${patient.color.label}). " +
                                "Their triage action: ${patient.action.take(80)}. " +
                                "Please give me step-by-step treatment instructions."
                            showChatSheet = true
                        },
                    )
                }
            }
        }
    }

    // ── Inline Chat Bottom Sheet ──────────────────────────────────────────────
    if (showChatSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChatSheet = false },
            sheetState = chatSheetState,
            modifier = Modifier.imePadding(),
        ) {
            AegisAssistantPanel(
                uiState = uiState,
                initialInput = chatInput,
                onSend = { text ->
                    viewModel.sendChatMessage(text)
                    chatInput = ""
                },
                onCancel = { viewModel.cancelChat() },
                onDismiss = {
                    scope.launch { chatSheetState.hide() }.invokeOnCompletion {
                        showChatSheet = false
                    }
                },
            )
        }
    }

    if (showCameraView) {
        com.bhanu.aegis.feature.triage.components.CameraView(
            onImageCaptured = { bytes ->
                capturedImages = (capturedImages + bytes).takeLast(4)
                showCameraView = false
            },
            onClose = { showCameraView = false }
        )
    }
} // End of Box
}

// ─────────────────────────────────────────────────────────────────────────────
//  Inline Aegis Assistant Panel (bottom sheet)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AegisAssistantPanel(
    uiState: TriageUiState,
    initialInput: String,
    onSend: (String) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    var inputText by remember { mutableStateOf(initialInput) }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-scroll to latest message
    LaunchedEffect(uiState.chatMessages.size, uiState.isChatGenerating) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 400.dp, max = 600.dp)
            .padding(horizontal = 16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Chat, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Aegis Assistant",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "100% On-Device",
                    style = MaterialTheme.typography.labelSmall,
                    color = offlineGreen,
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, "Close", modifier = Modifier.size(18.dp))
                }
            }
        }

        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        // Quick action chips — generic, not patient-specific
        if (uiState.chatMessages.isEmpty()) {
            val quickChips = listOf(
                "How to treat a patient step by step",
                "What first aid supplies do I need",
                "How to stabilize a patient",
                "Identify a medicine and dosage",
                "Signs of internal bleeding",
                "How to perform CPR",
            )
            Text(
                "Quick questions:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                quickChips.forEach { chip ->
                    AssistChip(
                        onClick = { inputText = chip },
                        label = { Text(chip, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
        }

        // Message list
        if (uiState.chatMessages.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(uiState.chatMessages) { msg ->
                    ChatBubble(msg)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(16.dp),
                    )
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        RoundedCornerShape(16.dp),
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send,
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !uiState.isChatGenerating) {
                                keyboardController?.hide()
                                onSend(inputText.trim())
                                inputText = ""
                            }
                        }
                    ),
                    maxLines = 4,
                    decorationBox = { inner ->
                        if (inputText.isEmpty()) {
                            Text(
                                "Ask about a patient or treatment…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }
                        inner()
                    },
                )
            }

            // Send / Stop button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (uiState.isChatGenerating) MaterialTheme.colorScheme.errorContainer
                                else if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = {
                    if (uiState.isChatGenerating) {
                        onCancel()
                    } else if (inputText.isNotBlank()) {
                        keyboardController?.hide()
                        onSend(inputText.trim())
                        inputText = ""
                    }
                }) {
                    Icon(
                        imageVector = if (uiState.isChatGenerating) Icons.Filled.Stop else Icons.Filled.Send,
                        contentDescription = if (uiState.isChatGenerating) "Stop" else "Send",
                        modifier = Modifier.size(20.dp),
                        tint = when {
                            uiState.isChatGenerating -> MaterialTheme.colorScheme.onErrorContainer
                            inputText.isNotBlank()   -> MaterialTheme.colorScheme.onPrimary
                            else                     -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: AegisChatMessage) {
    val isUser = msg.role == AegisChatRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isUser) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp,
                    ),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (msg.isStreaming && msg.text.isBlank()) {
                // Typing indicator
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    repeat(3) {
                        val inf = rememberInfiniteTransition(label = "dot$it")
                        val a by inf.animateFloat(
                            0.3f, 1f,
                            infiniteRepeatable(tween(600, delayMillis = it * 150), RepeatMode.Reverse),
                            label = "a$it",
                        )
                        Box(
                            Modifier.size(6.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = a), CircleShape)
                        )
                    }
                }
            } else {
                Text(
                    text = parseMarkdownToAnnotatedString(msg.text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Loading / Error views
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EngineLoadingView(isLoading: Boolean, modelName: String) {
    val inf = rememberInfiniteTransition(label = "load")
    val alpha by inf.animateFloat(0.4f, 1f, infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "a")
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (isLoading) CircularProgressIndicator(color = emergencyGradientStart, modifier = Modifier.size(56.dp))
            Text(
                if (isLoading) "Loading $modelName on GPU…" else "Initializing…",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            )
            Text("Gemma 4 • LiteRT-LM • 100% On-Device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EngineErrorView(message: String, onRetry: () -> Unit, onNavigateToModels: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text("⚠️", fontSize = 48.sp)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRetry) { Text("Retry") }
                Button(onClick = onNavigateToModels) { Text("Download Model") }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Main triage content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TriageContent(
    uiState: TriageUiState,
    symptoms: String,
    imageCount: Int,
    hasAudio: Boolean,
    onSymptomsChange: (String) -> Unit,
    onPickImages: () -> Unit,
    onClearImages: () -> Unit,
    onAudioCaptured: (ByteArray) -> Unit,
    onClearAudio: () -> Unit,
    onExecuteTriage: () -> Unit,
    onQuickAction: (String) -> Unit,
    onCancelTriage: () -> Unit,
    onClearQueue: () -> Unit,
    onRemovePatient: (String) -> Unit,
    onOpenChatForPatient: (TriagePatient) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Input card ────────────────────────────────────────────
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), elevation = GlassElevation.High) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("📋 Patient Assessment", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                    OutlinedTextField(
                        value = symptoms,
                        onValueChange = onSymptomsChange,
                        label = { Text("Observed symptoms") },
                        placeholder = { Text("e.g., Not breathing, pale skin, deep laceration on left arm") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2, maxLines = 4,
                        enabled = !uiState.isProcessing,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = emergencyGradientStart,
                            focusedLabelColor = emergencyGradientStart,
                        ),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = onPickImages,
                            enabled = !uiState.isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                        ) {
                            Icon(if (imageCount > 0) Icons.Filled.CheckCircle else Icons.Filled.Image, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (imageCount > 0) "$imageCount photo${if (imageCount > 1) "s" else ""}" else "Photos", style = MaterialTheme.typography.labelMedium)
                        }
                        if (imageCount > 0) {
                            IconButton(onClick = onClearImages, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Clear, "Clear", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        if (hasAudio) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Mic, null, tint = offlineGreen, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Audio ✓", style = MaterialTheme.typography.labelSmall, color = offlineGreen)
                                IconButton(onClick = onClearAudio, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Filled.Clear, "Clear audio", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                }
                            }
                        } else {
                            AudioRecorderButton(onAudioCaptured = onAudioCaptured)
                        }
                    }

                    Button(
                        onClick = if (uiState.isProcessing) onCancelTriage else onExecuteTriage,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isProcessing) MaterialTheme.colorScheme.error else emergencyGradientStart,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("CANCEL", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Filled.PlayArrow, null, Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("EXECUTE TRIAGE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // ── Quick Actions ─────────────────────────────────────────
        if (!uiState.isProcessing) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⚡ Quick Actions", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            "How to treat a patient step by step",
                            "What first aid supplies do I need",
                            "How to stabilize a patient",
                            "Identify a medicine and dosage",
                            "Signs of internal bleeding",
                            "How to perform CPR",
                        ).forEach { prompt ->
                            AssistChip(
                                onClick = { onQuickAction(prompt) },
                                label = { Text(prompt, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = { Icon(Icons.Filled.Chat, null, Modifier.size(14.dp)) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                            )
                        }
                    }
                }
            }
        }

        // ── Live Thinking ─────────────────────────────────────────
        if (uiState.isProcessing && uiState.currentThinking.isNotBlank()) {
            item { ThinkingCard(uiState.currentThinking) }
        }

        // ── Queue ─────────────────────────────────────────────────
        if (uiState.patientQueue.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("🏥 Triage Queue (${uiState.patientQueue.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onClearQueue) {
                        Icon(Icons.Filled.Delete, "Clear", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                    }
                }
            }
            items(uiState.patientQueue, key = { it.id }) { patient ->
                TriagePatientCard(
                    patient = patient,
                    onRemove = { onRemovePatient(patient.id) },
                    onOpenChat = { onOpenChatForPatient(patient) },
                )
            }
        }

        // ── Empty state ───────────────────────────────────────────
        if (uiState.patientQueue.isEmpty() && !uiState.isProcessing) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🚨", fontSize = 40.sp)
                        Text("No patients in queue", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Attach photos, record voice, describe symptoms", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingCard(text: String) {
    val t = rememberInfiniteTransition(label = "tp")
    val ba by t.animateFloat(0.3f, 0.8f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "b")
    val scrollState = rememberScrollState()
    
    LaunchedEffect(text) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize()
            .border(2.dp, Color(0xFFE8C866).copy(alpha = ba), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3D3520).copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🧠", fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text("Gemma 4 Reasoning", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE8C866))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = text, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.heightIn(max = 120.dp).verticalScroll(scrollState)
            )
        }
    }
}

@Composable
fun TriagePatientCard(
    patient: TriagePatient,
    onRemove: () -> Unit,
    onOpenChat: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
            Box(
                Modifier.width(6.dp)
                    .fillMaxHeight()
                    .background(patient.color.bgColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Column(Modifier.weight(1f).padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("#${patient.numberLabel}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(
                            Modifier.background(patient.color.bgColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("${patient.color.name} — ${patient.color.label}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = patient.color.textColor)
                        }
                    }
                    Text(timeFormat.format(Date(patient.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(8.dp))
                Text(patient.action, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), maxLines = if (expanded) 10 else 2, overflow = TextOverflow.Ellipsis)

                AnimatedVisibility(expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        if (patient.symptoms.isNotBlank()) {
                            Text("Symptoms: ${patient.symptoms}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                        }
                        Text("Triage Assessment:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(patient.reasoning, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    }
                }

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { expanded = !expanded }) {
                        Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, "Toggle", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (expanded) "Less" else "Details", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = onOpenChat,
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        ) {
                            Icon(Icons.Filled.Chat, null, Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Ask AI", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                        if (expanded) {
                            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.Delete, "Remove", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Image resize helper
// ─────────────────────────────────────────────────────────────────────────────

private fun resizeImageForModel(rawBytes: ByteArray, maxDim: Int = 768): ByteArray? {
    return try {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, opts)
        var sampleSize = 1
        val longestSide = maxOf(opts.outWidth, opts.outHeight)
        while (longestSide / (sampleSize * 2) >= maxDim) sampleSize *= 2
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts) ?: return null
        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
            val w = (bitmap.width * scale).toInt().coerceAtLeast(1)
            val h = (bitmap.height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, w, h, true).also { bitmap.recycle() }
        } else bitmap
        val out = java.io.ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        scaled.recycle()
        out.toByteArray()
    } catch (_: Exception) { null }
}

private fun parseMarkdownToAnnotatedString(text: String): androidx.compose.ui.text.AnnotatedString {
    val builder = androidx.compose.ui.text.AnnotatedString.Builder()
    var currentIndex = 0
    // Regex to match **bold** text
    val regex = Regex("\\*\\*(.*?)\\*\\*")
    val matches = regex.findAll(text)
    for (match in matches) {
        builder.append(text.substring(currentIndex, match.range.first))
        builder.withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
            builder.append(match.groupValues[1])
        }
        currentIndex = match.range.last + 1
    }
    builder.append(text.substring(currentIndex))
    return builder.toAnnotatedString()
}
