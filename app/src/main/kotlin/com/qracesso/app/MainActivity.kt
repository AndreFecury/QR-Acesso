package com.qracesso.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.qracesso.app.presentation.main.MainViewModel
import com.qracesso.app.presentation.navigation.QRAcessoNavHost
import com.qracesso.app.presentation.theme.QRAcessoTheme

/**
 * Única Activity do aplicativo. A tela principal é composta
 * diretamente na inicialização — sem splash screen, sem login, sem
 * etapas intermediárias — para que o QR Code fique visível o mais
 * rápido possível.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val app = application as QRAcessoApplication
        MainViewModel.factory(
            repository = app.repository,
            detector = app.detector,
            imageLoader = app.imageUriLoader,
            appContext = applicationContext
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QRAcessoTheme {
                QRAcessoNavHost(viewModel = viewModel)
            }
        }
    }
}
