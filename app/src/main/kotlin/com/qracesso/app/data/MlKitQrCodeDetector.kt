package com.qracesso.app.data

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.qracesso.app.domain.DetectionResult
import com.qracesso.app.domain.QrCodeDetector
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val CROP_PADDING_RATIO = 0.06

/**
 * Implementação de [QrCodeDetector] baseada no Google ML Kit, restrita
 * ao formato QR_CODE. Usa o modelo empacotado no próprio aplicativo
 * (dependência `com.google.mlkit:barcode-scanning`), e não a variante
 * que depende do Google Play services, para que a detecção funcione
 * totalmente offline mesmo na primeira utilização.
 */
class MlKitQrCodeDetector : QrCodeDetector {

    private val scanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    override suspend fun detect(source: Bitmap): DetectionResult {
        val barcodes = try {
            scanBitmap(source)
        } catch (_: Exception) {
            return DetectionResult.InvalidImage
        }

        val boxes = barcodes.mapNotNull { it.boundingBox }

        return when (boxes.size) {
            0 -> DetectionResult.NotFound(source)
            1 -> DetectionResult.SingleQrCode(cropWithPadding(source, boxes.first()))
            else -> DetectionResult.MultipleQrCodes(boxes.map { cropWithPadding(source, it) })
        }
    }

    private suspend fun scanBitmap(bitmap: Bitmap): List<Barcode> =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (continuation.isActive) continuation.resume(barcodes)
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resumeWithException(exception)
                }
        }

    /**
     * Recorta a região do QR Code preservando os pixels originais, sem
     * filtros ou reamostragem agressiva — apenas com uma pequena
     * margem de segurança para não cortar a "quiet zone" do código,
     * importante para leitura confiável pela catraca.
     */
    private fun cropWithPadding(source: Bitmap, box: Rect): Bitmap {
        val paddingX = (box.width() * CROP_PADDING_RATIO).toInt()
        val paddingY = (box.height() * CROP_PADDING_RATIO).toInt()

        val left = (box.left - paddingX).coerceIn(0, source.width - 1)
        val top = (box.top - paddingY).coerceIn(0, source.height - 1)
        val right = (box.right + paddingX).coerceIn(left + 1, source.width)
        val bottom = (box.bottom + paddingY).coerceIn(top + 1, source.height)

        return Bitmap.createBitmap(source, left, top, right - left, bottom - top)
    }
}
