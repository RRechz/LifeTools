package com.babelsoftware.lifetools.ui.movie

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.babelsoftware.lifetools.MovieWidgetProvider
import com.babelsoftware.lifetools.R

class MovieWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val widgetId = inputData.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

    override suspend fun doWork(): Result {
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return Result.failure()

        // 1. DEĞİŞİKLİK: Doğru ViewModel'i kullan
        val viewModel = MovieViewModel()
        val appWidgetManager = AppWidgetManager.getInstance(context)

        updateWidget(appWidgetManager, "Film aranıyor...", true)

        // 2. DEĞİŞİKLİK: Yeni eklediğimiz fonksiyonu çağır
        val recommendation = viewModel.fetchSingleMovieRecommendationForWidget()

        return if (recommendation != null) {
            updateWidget(appWidgetManager, recommendation, false)
            saveRecommendationToPrefs(recommendation) // Kaydetme fonksiyonunu yeniden adlandıralım
            Result.success()
        } else {
            updateWidget(appWidgetManager, "Öneri alınamadı. Tekrar dene.", false)
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

    private fun saveRecommendationToPrefs(recommendation: String) {
        val prefs = context.getSharedPreferences(MovieWidgetProvider.PREFS_NAME, Context.MODE_PRIVATE).edit()
        prefs.putString("${MovieWidgetProvider.PREF_PREFIX_KEY}$widgetId", recommendation)
        prefs.apply()
    }
}