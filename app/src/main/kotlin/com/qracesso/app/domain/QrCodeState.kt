package com.qracesso.app.domain

/**
 * Estados possíveis do aplicativo, conforme definido na especificação
 * do produto. A camada de apresentação reage a cada um deles.
 */
enum class QrCodeState {
    NO_QR_CODE,
    IMPORTING,
    PROCESSING,
    QR_CODE_READY,
    INVALID_IMAGE,
    MULTIPLE_QR_CODES,
    ERROR
}
