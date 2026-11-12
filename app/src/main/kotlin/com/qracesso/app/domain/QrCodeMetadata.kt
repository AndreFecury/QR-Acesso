package com.qracesso.app.domain

/**
 * Metadados associados ao QR Code armazenado. Não contém o conteúdo
 * decodificado do código — apenas informações auxiliares definidas
 * pelo próprio usuário.
 */
data class QrCodeMetadata(
    val savedAtEpochMillis: Long,
    val semesterLabel: String?,
    val expirationWarningEnabled: Boolean
)
