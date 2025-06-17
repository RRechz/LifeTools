package com.babelsoftware.lifetools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.ui.truthordare.TruthWidgetWorker

class TruthWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.babelsoftware.lifetools.ACTION_REFRESH"
        const val PREFS_NAME = "com.babelsoftware.lifetools.TruthWidgetProvider"
        const val PREF_PREFIX_KEY = "widget_question_"
    }

    /**
     * onUpdate, artık hafızayı kontrol edecek. Hafızada soru varsa arayüzü ellemeyecek.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        appWidgetIds.forEach { appWidgetId ->
            val savedQuestion = prefs.getString("$PREF_PREFIX_KEY$appWidgetId", null)
            val views = RemoteViews(context.packageName, R.layout.truth_widget_layout)

            // Hafızadaki soruyu veya başlangıç metnini ayarla
            views.setTextViewText(
                R.id.widget_question_text,
                savedQuestion ?: "Yeni soru için dokun"
            )

            // Yenileme butonu için PendingIntent'i her zaman ayarla
            val refreshIntent = Intent(context, TruthWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
                // Hangi widget'ın tıklandığını bilmemiz için ID'sini intent'e ekliyoruz
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refreshIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )
            views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    /**
     * onReceive, artık Worker'ı tetiklerken hangi widget ID'sinin
     * güncelleneceği bilgisini de gönderecek.
     */
    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ACTION_REFRESH) {
            // Hangi widget'ın butonuna tıklandığını intent'ten al
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                enqueueUpdateWorker(context, appWidgetId)
            }
        }
    }

    private fun enqueueUpdateWorker(context: Context, appWidgetId: Int) {
        // Worker'a hangi widget'ı güncellemesi gerektiğini Data olarak iletiyoruz
        val workData = workDataOf(AppWidgetManager.EXTRA_APPWIDGET_ID to appWidgetId)

        val workRequest = OneTimeWorkRequestBuilder<TruthWidgetWorker>()
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}