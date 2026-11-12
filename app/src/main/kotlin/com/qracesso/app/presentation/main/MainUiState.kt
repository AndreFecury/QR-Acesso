package com.qracesso.app.presentation.main

import android.graphics.Bitmap
import com.qracesso.app.domain.QrCodeState

data class MainUiState(
    val state: QrCodeState = QrCodeState.NO_QR_CODE,
    val qrBitmap: Bitmap? = null,
    val semesterLabel: String? = null,
    val savedAtEpochMillis: Long? = null,
    val multipleCandidates: List<Bitmap> = emptyList(),
    val fallbackOriginal: Bitmap? = null,
    val errorMessage: String? = null,
    val brightnessBoostEnabled: Boolean = true
)
