package com.qracesso.app

import android.app.Application
import com.qracesso.app.data.AndroidImageUriLoader
import com.qracesso.app.data.MlKitQrCodeDetector
import com.qracesso.app.data.QrCodeRepositoryImpl
import com.qracesso.app.data.QrCodeStorage
import com.qracesso.app.domain.ImageUriLoader
import com.qracesso.app.domain.QrCodeDetector
import com.qracesso.app.domain.QrCodeRepository

/**
 * Composição manual e simples das dependências (sem framework de DI):
 * proporcional ao tamanho do projeto, conforme a seção 30 da
 * especificação.
 */
class QRAcessoApplication : Application() {

    lateinit var repository: QrCodeRepository
        private set

    lateinit var detector: QrCodeDetector
        private set

    lateinit var imageUriLoader: ImageUriLoader
        private set

    override fun onCreate() {
        super.onCreate()
        val storage = QrCodeStorage(applicationContext)
        repository = QrCodeRepositoryImpl(storage)
        detector = MlKitQrCodeDetector()
        imageUriLoader = AndroidImageUriLoader(contentResolver)
    }
}
