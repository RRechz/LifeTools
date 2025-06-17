package com.babelsoftware.lifetools.ui.truthordare

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.babelsoftware.lifetools.R
import com.babelsoftware.lifetools.TruthWidgetProvider
import com.babelsoftware.lifetools.ui.truthordare.TruthQuestionsViewModel

class TruthWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    // Worker'a hangi widget'ın güncelleneceği bilgisini dışarıdan alacağız.
    private val widgetId = inputData.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

    override suspend fun doWork(): Result {
        // Geçersiz bir widget ID'si varsa işi bitir
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return Result.failure()
        }

        val viewModel = TruthQuestionsViewModel()
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // "Yükleniyor..." durumunu göster
        updateWidget(appWidgetManager, "Soru üretiliyor...", true)

        val question = viewModel.fetchSingleQuestionForWidget()

        return if (question != null) {
            // Soruyu widget'da göster
            updateWidget(appWidgetManager, question, false)
            // Soruyu kalıcı olarak kaydet
            saveQuestionToPrefs(question)
            Result.success()
        } else {
            // Hata durumunu göster
            updateWidget(appWidgetManager, "Soru alınamadı. Tekrar dene.", false)
            Result.failure()
        }
    }

    private fun updateWidget(appWidgetManager: AppWidgetManager, text: String, isLoading: Boolean) {
        val views = RemoteViews(context.packageName, R.layout.truth_widget_layout)
        views.setTextViewText(R.id.widget_question_text, text)
        views.setViewVisibility(R.id.widget_refresh_button, if (isLoading) View.GONE else View.VISIBLE)
        // Artık sadece belirli bir widget'ı güncelliyoruz
        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun saveQuestionToPrefs(question: String) {
        val prefs = context.getSharedPreferences(TruthWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE).edit()
        // Her widget'ın sorusunu kendi ID'si ile eşleşen bir anahtarla kaydediyoruz
        prefs.putString("${TruthWidgetProvider.PREF_PREFIX_KEY}$widgetId", question)
        prefs.apply()
    }
}