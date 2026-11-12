package com.qracesso.app.presentation.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qracesso.app.R
import com.qracesso.app.domain.QrCodeState
import com.qracesso.app.util.BrightnessBoostEffect
import com.qracesso.app.util.KeepScreenOnEffect

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(viewModel::onImageSelected)
    }

    fun launchPicker() {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (uiState.state) {
                QrCodeState.NO_QR_CODE -> EmptyStateContent(
                    modifier = Modifier.align(Alignment.Center),
                    onSelectImage = ::launchPicker
                )

                QrCodeState.IMPORTING, QrCodeState.PROCESSING -> LoadingContent(
                    modifier = Modifier.align(Alignment.Center)
                )

                QrCodeState.QR_CODE_READY -> {
                    KeepScreenOnEffect()
                    BrightnessBoostEffect(enabled = uiState.brightnessBoostEnabled)
                    QrCodeContent(
                        modifier = Modifier.fillMaxSize(),
                        qrBitmap = uiState.qrBitmap,
                        semesterLabel = uiState.semesterLabel,
                        onOpenSettings = onOpenSettings
                    )
                }

                QrCodeState.INVALID_IMAGE -> InvalidImageContent(
                    modifier = Modifier.align(Alignment.Center),
                    message = uiState.errorMessage ?: stringResource(R.string.error_invalid_image),
                    canUseOriginal = uiState.fallbackOriginal != null,
                    onRetry = {
                        viewModel.onCancelImport()
                        launchPicker()
                    },
                    onUseOriginal = viewModel::onUseOriginalAnyway
                )

                QrCodeState.MULTIPLE_QR_CODES -> MultipleQrCodesContent(
                    modifier = Modifier.fillMaxSize(),
                    candidates = uiState.multipleCandidates,
                    onChoose = viewModel::onCandidateChosen,
                    onCancel = viewModel::onCancelImport
                )

                QrCodeState.ERROR -> ErrorContent(
                    modifier = Modifier.align(Alignment.Center),
                    message = uiState.errorMessage ?: stringResource(R.string.error_generic),
                    onRetry = viewModel::onDismissError
                )
            }
        }
    }
}

@Composable
private fun EmptyStateContent(modifier: Modifier = Modifier, onSelectImage: () -> Unit) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.empty_state_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_state_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onSelectImage) {
            Text(stringResource(R.string.action_select_image))
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.processing_image),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QrCodeContent(
    modifier: Modifier = Modifier,
    qrBitmap: android.graphics.Bitmap?,
    semesterLabel: String?,
    onOpenSettings: () -> Unit
) {
    Box(modifier = modifier) {
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_settings),
                contentDescription = stringResource(R.string.action_settings)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.qr_code_content_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }

            if (!semesterLabel.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = semesterLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InvalidImageContent(
    modifier: Modifier = Modifier,
    message: String,
    canUseOriginal: Boolean,
    onRetry: () -> Unit,
    onUseOriginal: () -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.action_try_another_image))
        }
        if (canUseOriginal) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onUseOriginal) {
                Text(stringResource(R.string.action_use_original_anyway))
            }
        }
    }
}

@Composable
private fun ErrorContent(modifier: Modifier = Modifier, message: String, onRetry: () -> Unit) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.action_dismiss))
        }
    }
}

@Composable
private fun MultipleQrCodesContent(
    modifier: Modifier = Modifier,
    candidates: List<android.graphics.Bitmap>,
    onChoose: (android.graphics.Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    Column(modifier = modifier.padding(24.dp)) {
        Text(
            text = stringResource(R.string.multiple_qr_codes_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(candidates) { candidate ->
                Image(
                    bitmap = candidate.asImageBitmap(),
                    contentDescription = stringResource(R.string.qr_code_candidate_content_description),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                        .clickable { onChoose(candidate) }
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = onCancel,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.action_cancel))
        }
    }
}
