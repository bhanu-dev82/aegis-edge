package com.bhanu.aegis.feature.modelmanager

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.bhanu.aegis.core.data.repository.ModelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Returns a lambda that opens the system file picker for .litertlm files.
 * On selection, the file is imported into the models directory.
 *
 * Usage:
 *   val pickModel = rememberModelFilePicker(repository) { model -> /* imported */ }
 *   Button(onClick = pickModel) { Text("Import .litertlm") }
 */
@Composable
fun rememberModelFilePicker(
    modelRepository: ModelRepository,
    displayName: String,
    onImported: (com.bhanu.aegis.core.data.model.ChakuliModel) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            val imported = withContext(Dispatchers.IO) {
                try {
                    // Copy from content URI to a temp file
                    val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "model.litertlm"

                    if (!fileName.endsWith(".litertlm")) return@withContext null

                    val tempFile = File(context.cacheDir, fileName)
                    inputStream.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    modelRepository.importLocalModel(tempFile, displayName.ifEmpty { fileName })
                } catch (e: Exception) {
                    null
                }
            }
            imported?.let { onImported(it) }
        }
    }

    return { launcher.launch(arrayOf("application/octet-stream")) }
}
