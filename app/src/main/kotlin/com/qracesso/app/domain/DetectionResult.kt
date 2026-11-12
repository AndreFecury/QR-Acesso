package com.qracesso.app.domain

import android.graphics.Bitmap

/**
 * Resultado da tentativa de detectar um QR Code em uma imagem
 * selecionada pelo usuário.
 */
sealed interface DetectionResult {

    /** Exatamente um QR Code foi encontrado e recortado. */
    data class SingleQrCode(val cropped: Bitmap) : DetectionResult

    /** Mais de um QR Code foi encontrado; o usuário precisa escolher. */
    data class MultipleQrCodes(val candidates: List<Bitmap>) : DetectionResult

    /**
     * Nenhum QR Code foi encontrado automaticamente. A imagem original
     * é preservada para permitir que o usuário a utilize mesmo assim,
     * caso a detecção tenha falhado por limitação da biblioteca.
     */
    data class NotFound(val original: Bitmap) : DetectionResult

    /** A imagem não pôde nem ao menos ser processada. */
    data object InvalidImage : DetectionResult
}
