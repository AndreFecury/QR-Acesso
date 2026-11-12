package com.qracesso.app.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/**
 * Aumenta o brilho apenas da janela do próprio aplicativo (não o
 * brilho do sistema como um todo), o que não exige a permissão
 * especial WRITE_SETTINGS. O valor original é restaurado
 * automaticamente ao sair da tela.
 *
 * Limitação conhecida: em dispositivos com brilho automático
 * agressivo, o sistema pode reajustar o brilho alguns instantes
 * depois. Essa é uma limitação do Android, não do aplicativo.
 */
@Composable
fun BrightnessBoostEffect(enabled: Boolean, targetBrightness: Float = 1f) {
    val view = LocalView.current
    DisposableEffect(enabled) {
        val activity = view.context as? Activity
        val window = activity?.window
        val originalBrightness = window?.attributes?.screenBrightness

        if (enabled && window != null) {
            val params = window.attributes
            params.screenBrightness = targetBrightness
            window.attributes = params
        }

        onDispose {
            if (window != null && originalBrightness != null) {
                val params = window.attributes
                params.screenBrightness = originalBrightness
                window.attributes = params
            }
        }
    }
}
