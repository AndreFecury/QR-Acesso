package com.qracesso.app.domain

import android.graphics.Bitmap
import android.net.Uri

/**
 * Abstrai a leitura de uma imagem a partir de uma URI de conteúdo
 * (normalmente devolvida pelo Photo Picker). Extraída como interface
 * para permitir testar a camada de apresentação sem depender de um
 * ContentResolver real.
 */
fun interface ImageUriLoader {
    fun load(uri: Uri): Bitmap?
}
