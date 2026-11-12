package com.qracesso.app.data

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import com.qracesso.app.domain.ImageUriLoader
import com.qracesso.app.util.BitmapLoader

/** Implementação real de [ImageUriLoader], apoiada em um ContentResolver. */
class AndroidImageUriLoader(private val contentResolver: ContentResolver) : ImageUriLoader {
    override fun load(uri: Uri): Bitmap? = BitmapLoader.loadFrom(contentResolver, uri)
}
