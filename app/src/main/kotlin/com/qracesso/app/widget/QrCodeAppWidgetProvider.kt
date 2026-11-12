package com.qracesso.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.qracesso.app.MainActivity
import com.qracesso.app.R
import com.qracesso.app.data.QrCodeStorage

/**
 * Widget minimalista: exibe uma miniatura do QR Code salvo e, ao ser
 * tocado, abre o aplicativo. Não executa nenhum trabalho em segundo
 * plano contínuo (updatePeriodMillis = 0 em qr_widget_info.xml);
 * atualiza apenas quando o sistema solicita ou quando [requestUpdate]
 * é chamado explicitamente após uma alteração do QR Code.
 */
class QrCodeAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val storage = QrCodeStorage(context)
        val qrBitmap = storage.loadQrCode()

        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_qr_code)

            if (qrBitmap != null) {
                views.setImageViewBitmap(R.id.widget_qr_image, qrBitmap)
                views.setViewVisibility(R.id.widget_qr_image, View.VISIBLE)
                views.setViewVisibility(R.id.widget_empty_label, View.GONE)
            } else {
                views.setViewVisibility(R.id.widget_qr_image, View.GONE)
                views.setViewVisibility(R.id.widget_empty_label, View.VISIBLE)
            }

            val openAppIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    companion object {
        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, QrCodeAppWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(componentName)
            if (ids.isNotEmpty()) {
                val intent = Intent(context, QrCodeAppWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
