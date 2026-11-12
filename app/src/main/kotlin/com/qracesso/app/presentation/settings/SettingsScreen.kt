package com.qracesso.app.presentation.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qracesso.app.R
import com.qracesso.app.presentation.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var semesterInput by remember(uiState.semesterLabel) {
        mutableStateOf(uiState.semesterLabel.orEmpty())
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            viewModel.onImageSelected(it)
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.action_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsRow(
                iconRes = R.drawable.ic_edit,
                title = stringResource(R.string.settings_change_qr_code),
                onClick = {
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            OutlinedTextField(
                value = semesterInput,
                onValueChange = { semesterInput = it },
                label = { Text(stringResource(R.string.settings_semester_label)) },
                placeholder = { Text(stringResource(R.string.settings_semester_placeholder)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            Button(
                onClick = {
                    viewModel.onSemesterLabelChanged(
                        semesterInput.ifBlank { null },
                        expirationWarningEnabled = true
                    )
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.action_save))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_brightness_boost),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.settings_brightness_boost_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.brightnessBoostEnabled,
                    onCheckedChange = viewModel::onToggleBrightnessBoost
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            SettingsRow(
                iconRes = R.drawable.ic_info,
                title = stringResource(R.string.settings_about),
                onClick = { showAboutDialog = true }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            SettingsRow(
                iconRes = R.drawable.ic_delete,
                title = stringResource(R.string.settings_delete_qr_code),
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { showDeleteConfirmation = true }
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.settings_delete_qr_code)) },
            text = { Text(stringResource(R.string.settings_delete_confirmation_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteQrCode()
                    showDeleteConfirmation = false
                    onBack()
                }) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.app_name)) },
            text = { Text(stringResource(R.string.settings_about_description)) },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    @DrawableRes iconRes: Int,
    title: String,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = null, tint = titleColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
    }
}
