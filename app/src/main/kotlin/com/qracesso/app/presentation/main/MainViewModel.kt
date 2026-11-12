package com.qracesso.app.presentation.main

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.qracesso.app.domain.DetectionResult
import com.qracesso.app.domain.ImageUriLoader
import com.qracesso.app.domain.QrCodeDetector
import com.qracesso.app.domain.QrCodeRepository
import com.qracesso.app.domain.QrCodeState
import com.qracesso.app.widget.QrCodeAppWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: QrCodeRepository,
    private val detector: QrCodeDetector,
    private val imageLoader: ImageUriLoader,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadPersistedQrCode()
    }

    private fun loadPersistedQrCode() {
        viewModelScope.launch {
            val bitmap = repository.getCurrentQrCode()
            val metadata = repository.getMetadata()
            val brightnessBoost = repository.isBrightnessBoostEnabled()
            _uiState.update {
                it.copy(
                    state = if (bitmap != null) QrCodeState.QR_CODE_READY else QrCodeState.NO_QR_CODE,
                    qrBitmap = bitmap,
                    semesterLabel = metadata?.semesterLabel,
                    savedAtEpochMillis = metadata?.savedAtEpochMillis,
                    brightnessBoostEnabled = brightnessBoost
                )
            }
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(state = QrCodeState.IMPORTING) }

            val original = imageLoader.load(uri)
            if (original == null) {
                _uiState.update {
                    it.copy(
                        state = QrCodeState.INVALID_IMAGE,
                        errorMessage = "Não foi possível utilizar essa imagem."
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(state = QrCodeState.PROCESSING) }

            when (val result = detector.detect(original)) {
                is DetectionResult.SingleQrCode -> persistNewQrCode(result.cropped)

                is DetectionResult.MultipleQrCodes -> _uiState.update {
                    it.copy(
                        state = QrCodeState.MULTIPLE_QR_CODES,
                        multipleCandidates = result.candidates
                    )
                }

                is DetectionResult.NotFound -> _uiState.update {
                    it.copy(
                        state = QrCodeState.INVALID_IMAGE,
                        errorMessage = "Não encontramos um QR Code nessa imagem.",
                        fallbackOriginal = result.original
                    )
                }

                DetectionResult.InvalidImage -> _uiState.update {
                    it.copy(
                        state = QrCodeState.INVALID_IMAGE,
                        errorMessage = "Não foi possível utilizar essa imagem."
                    )
                }
            }
        }
    }

    fun onCandidateChosen(bitmap: Bitmap) {
        viewModelScope.launch { persistNewQrCode(bitmap) }
    }

    /** Permite usar a captura original quando a detecção automática falha. */
    fun onUseOriginalAnyway() {
        val original = _uiState.value.fallbackOriginal ?: return
        viewModelScope.launch { persistNewQrCode(original) }
    }

    fun onCancelImport() {
        val hadPreviousQrCode = _uiState.value.qrBitmap != null
        _uiState.update {
            it.copy(
                state = if (hadPreviousQrCode) QrCodeState.QR_CODE_READY else QrCodeState.NO_QR_CODE,
                multipleCandidates = emptyList(),
                fallbackOriginal = null,
                errorMessage = null
            )
        }
    }

    private suspend fun persistNewQrCode(bitmap: Bitmap) {
        val currentSemester = _uiState.value.semesterLabel
        val saved = repository.replaceQrCode(bitmap, currentSemester, expirationWarningEnabled = true)

        if (saved) {
            _uiState.update {
                it.copy(
                    state = QrCodeState.QR_CODE_READY,
                    qrBitmap = bitmap,
                    savedAtEpochMillis = System.currentTimeMillis(),
                    multipleCandidates = emptyList(),
                    fallbackOriginal = null,
                    errorMessage = null
                )
            }
            QrCodeAppWidgetProvider.requestUpdate(appContext)
        } else {
            val hadPreviousQrCode = _uiState.value.qrBitmap != null
            _uiState.update {
                it.copy(
                    state = if (hadPreviousQrCode) QrCodeState.QR_CODE_READY else QrCodeState.ERROR,
                    errorMessage = "Não foi possível salvar o QR Code. O código anterior foi mantido.",
                    multipleCandidates = emptyList(),
                    fallbackOriginal = null
                )
            }
        }
    }

    fun onDeleteQrCode() {
        viewModelScope.launch {
            repository.deleteQrCode()
            QrCodeAppWidgetProvider.requestUpdate(appContext)
            _uiState.update { MainUiState(brightnessBoostEnabled = it.brightnessBoostEnabled) }
        }
    }

    fun onSemesterLabelChanged(label: String?, expirationWarningEnabled: Boolean) {
        viewModelScope.launch {
            repository.updateSemesterLabel(label, expirationWarningEnabled)
            _uiState.update { it.copy(semesterLabel = label) }
        }
    }

    fun onToggleBrightnessBoost(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBrightnessBoostEnabled(enabled)
            _uiState.update { it.copy(brightnessBoostEnabled = enabled) }
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        fun factory(
            repository: QrCodeRepository,
            detector: QrCodeDetector,
            imageLoader: ImageUriLoader,
            appContext: Context
        ) = viewModelFactory {
            initializer { MainViewModel(repository, detector, imageLoader, appContext) }
        }
    }
}
