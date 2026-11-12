package com.qracesso.app

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.qracesso.app.domain.DetectionResult
import com.qracesso.app.domain.ImageUriLoader
import com.qracesso.app.domain.QrCodeDetector
import com.qracesso.app.domain.QrCodeMetadata
import com.qracesso.app.domain.QrCodeRepository
import com.qracesso.app.presentation.main.MainScreen
import com.qracesso.app.presentation.main.MainViewModel
import com.qracesso.app.presentation.theme.QRAcessoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class EmptyRepository : QrCodeRepository {
        override suspend fun getCurrentQrCode(): Bitmap? = null
        override suspend fun getMetadata(): QrCodeMetadata? = null
        override suspend fun hasQrCode(): Boolean = false
        override suspend fun replaceQrCode(
            bitmap: Bitmap,
            semesterLabel: String?,
            expirationWarningEnabled: Boolean
        ) = true
        override suspend fun updateSemesterLabel(semesterLabel: String?, expirationWarningEnabled: Boolean) {}
        override suspend fun deleteQrCode(): Boolean = true
        override suspend fun isBrightnessBoostEnabled(): Boolean = true
        override suspend fun setBrightnessBoostEnabled(enabled: Boolean) {}
    }

    private class NoopDetector : QrCodeDetector {
        override suspend fun detect(source: Bitmap): DetectionResult = DetectionResult.InvalidImage
    }

    private class NoopImageLoader : ImageUriLoader {
        override fun load(uri: Uri): Bitmap? = null
    }

    @Test
    fun estadoVazioMostraBotaoDeSelecionarImagem() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val viewModel = MainViewModel(EmptyRepository(), NoopDetector(), NoopImageLoader(), context)

        composeTestRule.setContent {
            QRAcessoTheme {
                MainScreen(viewModel = viewModel, onOpenSettings = {})
            }
        }

        composeTestRule.onNodeWithText("Selecionar imagem").assertExists()
    }
}
