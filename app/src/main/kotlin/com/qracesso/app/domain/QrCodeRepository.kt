package com.qracesso.app.domain

import android.graphics.Bitmap

/**
 * Contrato de persistência do QR Code. A implementação concreta vive
 * na camada de dados; o domínio e a apresentação dependem apenas
 * desta interface, o que mantém o projeto testável e as camadas
 * fracamente acopladas.
 */
interface QrCodeRepository {

    suspend fun getCurrentQrCode(): Bitmap?

    suspend fun getMetadata(): QrCodeMetadata?

    suspend fun hasQrCode(): Boolean

    /**
     * Substitui o QR Code atual apenas se a gravação for bem-sucedida.
     * Retorna `false` sem apagar o QR Code anterior em caso de falha.
     */
    suspend fun replaceQrCode(
        bitmap: Bitmap,
        semesterLabel: String?,
        expirationWarningEnabled: Boolean
    ): Boolean

    suspend fun updateSemesterLabel(semesterLabel: String?, expirationWarningEnabled: Boolean)

    suspend fun deleteQrCode(): Boolean

    suspend fun isBrightnessBoostEnabled(): Boolean

    suspend fun setBrightnessBoostEnabled(enabled: Boolean)
}
