package com.qracesso.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.qracesso.app.domain.QrCodeMetadata
import java.io.File
import java.io.FileOutputStream

private const val PREFS_NAME = "qr_acesso_prefs"
private const val KEY_SAVED_AT = "saved_at"
private const val KEY_SEMESTER = "semester_label"
private const val KEY_EXPIRATION_WARNING = "expiration_warning_enabled"
private const val KEY_BRIGHTNESS_BOOST = "brightness_boost_enabled"
private const val QR_FILE_NAME = "qr_code.png"
private const val QR_TEMP_FILE_NAME = "qr_code.tmp.png"

/**
 * Responsável apenas por ler e gravar o arquivo do QR Code e seus
 * metadados no armazenamento privado do aplicativo (filesDir e
 * SharedPreferences em modo privado). Não conhece regras de negócio;
 * isso fica a cargo de [QrCodeRepositoryImpl].
 *
 * O arquivo é salvo em PNG (sem perdas) no armazenamento interno do
 * app — nunca no armazenamento público — e nunca é registrado em log.
 */
class QrCodeStorage(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val qrFile: File
        get() = File(context.filesDir, QR_FILE_NAME)

    private val tempFile: File
        get() = File(context.filesDir, QR_TEMP_FILE_NAME)

    fun hasQrCode(): Boolean = qrFile.exists()

    fun loadQrCode(): Bitmap? {
        if (!qrFile.exists()) return null
        return BitmapFactory.decodeFile(qrFile.absolutePath)
    }

    /**
     * Grava o novo QR Code de forma seguraprimeiro em um arquivo
     * temporário e só então substitui o arquivo anterior via rename
     * atômico. Isso garante que uma falha durante a gravação nunca
     * destrua um QR Code válido já existente (requisito da seção 15
     * da especificação: nunca apagar o QR Code antigo antes de o novo
     * ser validado).
     */
    fun saveQrCode(bitmap: Bitmap): Boolean {
        return try {
            FileOutputStream(tempFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                output.flush()
            }
            val moved = tempFile.renameTo(qrFile)
            if (!moved) tempFile.delete()
            moved
        } catch (_: Exception) {
            tempFile.delete()
            false
        }
    }

    fun deleteQrCode(): Boolean {
        val deleted = if (qrFile.exists()) qrFile.delete() else true
        prefs.edit()
            .remove(KEY_SAVED_AT)
            .remove(KEY_SEMESTER)
            .remove(KEY_EXPIRATION_WARNING)
            .apply()
        return deleted
    }

    fun saveMetadata(metadata: QrCodeMetadata) {
        prefs.edit()
            .putLong(KEY_SAVED_AT, metadata.savedAtEpochMillis)
            .putString(KEY_SEMESTER, metadata.semesterLabel)
            .putBoolean(KEY_EXPIRATION_WARNING, metadata.expirationWarningEnabled)
            .apply()
    }

    fun loadMetadata(): QrCodeMetadata? {
        if (!prefs.contains(KEY_SAVED_AT)) return null
        return QrCodeMetadata(
            savedAtEpochMillis = prefs.getLong(KEY_SAVED_AT, 0L),
            semesterLabel = prefs.getString(KEY_SEMESTER, null),
            expirationWarningEnabled = prefs.getBoolean(KEY_EXPIRATION_WARNING, true)
        )
    }

    fun isBrightnessBoostEnabled(): Boolean =
        prefs.getBoolean(KEY_BRIGHTNESS_BOOST, true)

    fun setBrightnessBoostEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BRIGHTNESS_BOOST, enabled).apply()
    }
}
