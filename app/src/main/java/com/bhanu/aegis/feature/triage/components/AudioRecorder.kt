package com.bhanu.aegis.feature.triage.components

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.bhanu.aegis.core.theme.PillShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val SAMPLE_RATE = 16000
private const val MAX_DURATION_SECS = 30
private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

/**
 * Writes a standard 44-byte WAV header for 16kHz mono PCM 16-bit audio.
 * LiteRT-LM's miniaudio decoder requires a valid WAV container — raw PCM crashes with error -10.
 */
fun buildWavBytes(pcmBytes: ByteArray, sampleRate: Int = SAMPLE_RATE): ByteArray {
    val channels = 1
    val bitsPerSample = 16
    val byteRate = sampleRate * channels * bitsPerSample / 8
    val blockAlign = channels * bitsPerSample / 8
    val dataSize = pcmBytes.size
    val chunkSize = 36 + dataSize

    val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
        // RIFF chunk
        put("RIFF".toByteArray())
        putInt(chunkSize)
        put("WAVE".toByteArray())
        // fmt sub-chunk
        put("fmt ".toByteArray())
        putInt(16)                      // sub-chunk size
        putShort(1)                     // PCM = 1
        putShort(channels.toShort())
        putInt(sampleRate)
        putInt(byteRate)
        putShort(blockAlign.toShort())
        putShort(bitsPerSample.toShort())
        // data sub-chunk
        put("data".toByteArray())
        putInt(dataSize)
    }.array()

    return header + pcmBytes
}

/**
 * Full-featured audio recorder button with live waveform bars and countdown.
 * Outputs WAV bytes (not raw PCM) — required by LiteRT-LM's miniaudio decoder.
 */
@Composable
fun AudioRecorderButton(
    onAudioCaptured: (ByteArray) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isRecording by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var amplitude by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    var stopFlag by remember { mutableStateOf<java.util.concurrent.atomic.AtomicBoolean?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val flag = java.util.concurrent.atomic.AtomicBoolean(true)
            stopFlag = flag
            isRecording = true
            secondsElapsed = 0
            scope.launch(Dispatchers.IO) {
                val pcm = recordPcm(flag) { amp -> amplitude = amp }
                withContext(Dispatchers.Main) {
                    isRecording = false
                    stopFlag = null
                    amplitude = 0f
                    onAudioCaptured(buildWavBytes(pcm))
                }
            }
        }
    }

    DisposableEffect(Unit) { onDispose { stopFlag?.set(false) } }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording && secondsElapsed < MAX_DURATION_SECS) {
                delay(1000)
                secondsElapsed++
            }
        }
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (isRecording) 1.12f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "pulse",
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isRecording) {
            // Live waveform bars
            WaveformBars(amplitude = amplitude, modifier = Modifier.width(40.dp).height(28.dp))
            // Countdown
            Text(
                text = "${MAX_DURATION_SECS - secondsElapsed}s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Box(
            modifier = Modifier
                .scale(pulseScale)
                .size(44.dp)
                .clip(PillShape)
                .background(
                    if (isRecording) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.surfaceContainerHighest
                ),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = {
                if (isRecording) {
                    stopFlag?.set(false)
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }) {
                Icon(
                    imageVector = if (isRecording) Icons.Outlined.Stop else Icons.Outlined.Mic,
                    contentDescription = if (isRecording) "Stop recording" else "Record audio",
                    tint = if (isRecording) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

/** Animated waveform bars driven by live amplitude. */
@Composable
private fun WaveformBars(amplitude: Float, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val barCount = 5
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(barCount) { i ->
            val phase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 400 + i * 80,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bar_$i",
            )
            val normalizedAmp = (amplitude / 32767f).coerceIn(0.05f, 1f)
            val barHeight = (6 + phase * 18 * normalizedAmp).dp
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
            )
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun recordPcm(
    stopFlag: java.util.concurrent.atomic.AtomicBoolean,
    onAmplitude: (Float) -> Unit,
): ByteArray = withContext(Dispatchers.IO) {
    val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    val recorder = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        SAMPLE_RATE,
        CHANNEL_CONFIG,
        AUDIO_FORMAT,
        bufferSize,
    )
    recorder.startRecording()

    val output = ByteArrayOutputStream()
    val buffer = ShortArray(bufferSize / 2)
    val startMs = System.currentTimeMillis()

    while (stopFlag.get()) {
        val read = recorder.read(buffer, 0, buffer.size)
        if (read > 0) {
            // Compute peak amplitude for waveform
            var peak = 0f
            for (i in 0 until read) {
                val abs = Math.abs(buffer[i].toFloat())
                if (abs > peak) peak = abs
            }
            withContext(Dispatchers.Main) { onAmplitude(peak) }

            // Write little-endian PCM bytes
            for (i in 0 until read) {
                val s = buffer[i]
                output.write(s.toInt() and 0xFF)
                output.write((s.toInt() ushr 8) and 0xFF)
            }
        }
        if (System.currentTimeMillis() - startMs >= MAX_DURATION_SECS * 1000L) {
            stopFlag.set(false)
        }
    }

    try {
        recorder.stop()
        recorder.release()
    } catch (_: Exception) {}

    output.toByteArray()
}
