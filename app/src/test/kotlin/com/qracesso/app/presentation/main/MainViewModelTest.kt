package com.qracesso.app.presentation.main

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.qracesso.app.MainDispatcherRule
import com.qracesso.app.domain.DetectionResult
import com.qracesso.app.domain.ImageUriLoader
import com.qracesso.app.domain.QrCodeDetector
import com.qracesso.app.domain.QrCodeMetadata
import com.qracesso.app.domain.QrCodeRepository
import com.qracesso.app.domain.QrCodeState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context by lazy { ApplicationProvider.getApplicationContext<android.content.Context>() }
    private val fakeUri: Uri = Uri.parse("content://fake/image.png")

    private fun sampleBitmap(): Bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)

    private class FakeRepository : QrCodeRepository {
        var stored: Bitmap? = null
        var metadata: QrCodeMetadata? = null
        var brightnessBoost = true
        var failNextSave = false

        override suspend fun getCurrentQrCode(): Bitmap? = stored
        override suspend fun getMetadata(): QrCodeMetadata? = metadata
        override suspend fun hasQrCode(): Boolean = stored != null

        override suspend fun replaceQrCode(
            bitmap: Bitmap,
            semesterLabel: String?,
            expirationWarningEnabled: Boolean
        ): Boolean {
            if (failNextSave) {
                failNextSave = false
                return false
            }
            stored = bitmap
            metadata = QrCodeMetadata(System.currentTimeMillis(), semesterLabel, expirationWarningEnabled)
            return true
        }

        override suspend fun updateSemesterLabel(semesterLabel: String?, expirationWarningEnabled: Boolean) {
            metadata = metadata?.copy(
                semesterLabel = semesterLabel,
                expirationWarningEnabled = expirationWarningEnabled
            )
        }

        override suspend fun deleteQrCode(): Boolean {
            stored = null
            metadata = null
            return true
        }

        override suspend fun isBrightnessBoostEnabled(): Boolean = brightnessBoost
        override suspend fun setBrightnessBoostEnabled(enabled: Boolean) {
            brightnessBoost = enabled
        }
    }

    private class FakeDetector(private val result: DetectionResult) : QrCodeDetector {
        override suspend fun detect(source: Bitmap): DetectionResult = result
    }

    private class FakeImageLoader(private val bitmap: Bitmap?) : ImageUriLoader {
        override fun load(uri: Uri): Bitmap? = bitmap
    }

    @Test
    fun `estado inicial sem QR Code salvo e NO_QR_CODE`() = runTest {
        val viewModel = MainViewModel(
            FakeRepository(),
            FakeDetector(DetectionResult.InvalidImage),
            FakeImageLoader(null),
            context
        )

        assertEquals(QrCodeState.NO_QR_CODE, viewModel.uiState.value.state)
    }

    @Test
    fun `importar imagem com um unico QR Code resulta em QR_CODE_READY`() = runTest {
        val viewModel = MainViewModel(
            FakeRepository(),
            FakeDetector(DetectionResult.SingleQrCode(sampleBitmap())),
            FakeImageLoader(sampleBitmap()),
            context
        )

        viewModel.onImageSelected(fakeUri)

        assertEquals(QrCodeState.QR_CODE_READY, viewModel.uiState.value.state)
        assertNotNull(viewModel.uiState.value.qrBitmap)
    }

    @Test
    fun `imagem sem QR Code resulta em INVALID_IMAGE com fallback disponivel`() = runTest {
        val viewModel = MainViewModel(
            FakeRepository(),
            FakeDetector(DetectionResult.NotFound(sampleBitmap())),
            FakeImageLoader(sampleBitmap()),
            context
        )

        viewModel.onImageSelected(fakeUri)

        assertEquals(QrCodeState.INVALID_IMAGE, viewModel.uiState.value.state)
        assertNotNull(viewModel.uiState.value.fallbackOriginal)
    }

    @Test
    fun `imagem que nao pode ser lida resulta em INVALID_IMAGE`() = runTest {
        val viewModel = MainViewModel(
            FakeRepository(),
            FakeDetector(DetectionResult.InvalidImage),
            FakeImageLoader(null),
            context
        )

        viewModel.onImageSelected(fakeUri)

        assertEquals(QrCodeState.INVALID_IMAGE, viewModel.uiState.value.state)
    }

    @Test
    fun `multiplos QR Codes resulta em MULTIPLE_QR_CODES com candidatos`() = runTest {
        val candidates = listOf(sampleBitmap(), sampleBitmap())
        val viewModel = MainViewModel(
            FakeRepository(),
            FakeDetector(DetectionResult.MultipleQrCodes(candidates)),
            FakeImageLoader(sampleBitmap()),
            context
        )

        viewModel.onImageSelected(fakeUri)

        assertEquals(QrCodeState.MULTIPLE_QR_CODES, viewModel.uiState.value.state)
        assertEquals(2, viewModel.uiState.value.multipleCandidates.size)
    }

    @Test
    fun `escolher um candidato salva o QR Code e volta para QR_CODE_READY`() = runTest {
        val chosen = sampleBitmap()
        val viewModel = MainViewModel(
            FakeRepository(),
            FakeDetector(DetectionResult.MultipleQrCodes(listOf(chosen, sampleBitmap()))),
            FakeImageLoader(sampleBitmap()),
            context
        )

        viewModel.onImageSelected(fakeUri)
        viewModel.onCandidateChosen(chosen)

        assertEquals(QrCodeState.QR_CODE_READY, viewModel.uiState.value.state)
    }

    @Test
    fun `falha ao salvar mantem o QR Code anterior`() = runTest {
        val repository = FakeRepository().apply {
            stored = sampleBitmap()
            metadata = QrCodeMetadata(0L, "2026/1", true)
        }
        val viewModel = MainViewModel(
            repository,
            FakeDetector(DetectionResult.SingleQrCode(sampleBitmap())),
            FakeImageLoader(sampleBitmap()),
            context
        )
        repository.failNextSave = true

        viewModel.onImageSelected(fakeUri)

        assertEquals(QrCodeState.QR_CODE_READY, viewModel.uiState.value.state)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `excluir QR Code volta o estado para NO_QR_CODE`() = runTest {
        val repository = FakeRepository().apply {
            stored = sampleBitmap()
            metadata = QrCodeMetadata(0L, "2026/1", true)
        }
        val viewModel = MainViewModel(
            repository,
            FakeDetector(DetectionResult.InvalidImage),
            FakeImageLoader(null),
            context
        )

        viewModel.onDeleteQrCode()

        assertEquals(QrCodeState.NO_QR_CODE, viewModel.uiState.value.state)
        assertNull(viewModel.uiState.value.qrBitmap)
    }
}
