package com.qracesso.app.domain

import android.graphics.Bitmap

/**
 * Abstrai a biblioteca concreta de detecção de QR Code. Isso permite
 * substituir a implementação (por exemplo, no ML Kit) por um dublê em
 * testes, sem que a camada de apresentação precise conhecer detalhes
 * da biblioteca utilizada.
 */
interface QrCodeDetector {
    suspend fun detect(source: Bitmap): DetectionResult
}
