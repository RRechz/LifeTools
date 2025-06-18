package com.babelsoftware.lifetools // Paket adınız farklı olabilir

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
import com.babelsoftware.lifetools.ui.movie.MovieWidgetWorker

class MovieWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.babelsoftware.lifetools.ACTION_MOVIE_REFRESH"
        const val PREFS_NAME = "com.babelsoftware.lifetools.MovieWidgetProvider"
        const val PREF_PREFIX_KEY = "widget_movie_rec_"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        appWidgetIds.forEach { appWidgetId ->
            val savedRecommendation = prefs.getString("$PREF_PREFIX_KEY$appWidgetId", null)
            val views = RemoteViews(context.packageName, R.layout.movie_widget_layout)

            views.setTextViewText(
                R.id.widget_question_text,
                savedRecommendation ?: "Film önerisi için dokun"
            )

            val refreshIntent = Intent(context, MovieWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
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

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ACTION_REFRESH) {
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
        val workData = workDataOf(AppWidgetManager.EXTRA_APPWIDGET_ID to appWidgetId)

        // **İŞTE KULLANILDIĞI YER BURASI**
        val workRequest = OneTimeWorkRequestBuilder<MovieWidgetWorker>()
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}