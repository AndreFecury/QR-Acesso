package com.qracesso.app.data

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QrCodeRepositoryTest {

    private lateinit var repository: QrCodeRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        repository = QrCodeRepositoryImpl(QrCodeStorage(context))
    }

    private fun sampleBitmap(color: Int = Color.BLACK): Bitmap {
        val bitmap = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        return bitmap
    }

    @Test
    fun `sem QR Code salvo, hasQrCode retorna falso`() = runTest {
        assertFalse(repository.hasQrCode())
        assertNull(repository.getCurrentQrCode())
    }

    @Test
    fun `apos salvar, o QR Code persiste e pode ser recuperado`() = runTest {
        val saved = repository.replaceQrCode(sampleBitmap(), "2026/2", true)

        assertTrue(saved)
        assertTrue(repository.hasQrCode())
        assertNotNull(repository.getCurrentQrCode())
        assertEquals("2026/2", repository.getMetadata()?.semesterLabel)
    }

    @Test
    fun `substituir o QR Code atualiza os metadados`() = runTest {
        repository.replaceQrCode(sampleBitmap(Color.BLACK), "2026/1", true)
        repository.replaceQrCode(sampleBitmap(Color.WHITE), "2026/2", true)

        assertEquals("2026/2", repository.getMetadata()?.semesterLabel)
    }

    @Test
    fun `excluir remove o QR Code e os metadados`() = runTest {
        repository.replaceQrCode(sampleBitmap(), "2026/2", true)

        val deleted = repository.deleteQrCode()

        assertTrue(deleted)
        assertFalse(repository.hasQrCode())
        assertNull(repository.getMetadata())
    }

    @Test
    fun `preferencia de brilho eh persistida`() = runTest {
        repository.setBrightnessBoostEnabled(false)
        assertFalse(repository.isBrightnessBoostEnabled())

        repository.setBrightnessBoostEnabled(true)
        assertTrue(repository.isBrightnessBoostEnabled())
    }
}
