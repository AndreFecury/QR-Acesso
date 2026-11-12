package com.qracesso.app.data

import android.graphics.Bitmap
import com.qracesso.app.domain.QrCodeMetadata
import com.qracesso.app.domain.QrCodeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementação de [QrCodeRepository] apoiada em [QrCodeStorage].
 * Todo o trabalho de I/O roda em [Dispatchers.IO] para nunca bloquear
 * a thread principal.
 */
class QrCodeRepositoryImpl(private val storage: QrCodeStorage) : QrCodeRepository {

    override suspend fun getCurrentQrCode(): Bitmap? = withContext(Dispatchers.IO) {
        storage.loadQrCode()
    }

    override suspend fun getMetadata(): QrCodeMetadata? = withContext(Dispatchers.IO) {
        storage.loadMetadata()
    }

    override suspend fun hasQrCode(): Boolean = withContext(Dispatchers.IO) {
        storage.hasQrCode()
    }

    override suspend fun replaceQrCode(
        bitmap: Bitmap,
        semesterLabel: String?,
        expirationWarningEnabled: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val saved = storage.saveQrCode(bitmap)
        if (saved) {
            storage.saveMetadata(
                QrCodeMetadata(
                    savedAtEpochMillis = System.currentTimeMillis(),
                    semesterLabel = semesterLabel,
                    expirationWarningEnabled = expirationWarningEnabled
                )
            )
        }
        saved
    }

    override suspend fun updateSemesterLabel(
        semesterLabel: String?,
        expirationWarningEnabled: Boolean
    ) = withContext(Dispatchers.IO) {
        val current = storage.loadMetadata() ?: return@withContext
        storage.saveMetadata(
            current.copy(
                semesterLabel = semesterLabel,
                expirationWarningEnabled = expirationWarningEnabled
            )
        )
    }

    override suspend fun deleteQrCode(): Boolean = withContext(Dispatchers.IO) {
        storage.deleteQrCode()
    }

    override suspend fun isBrightnessBoostEnabled(): Boolean = withContext(Dispatchers.IO) {
        storage.isBrightnessBoostEnabled()
    }

    override suspend fun setBrightnessBoostEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        storage.setBrightnessBoostEnabled(enabled)
    }
}
