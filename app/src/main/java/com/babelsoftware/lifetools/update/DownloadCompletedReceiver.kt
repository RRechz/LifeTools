// File: update/DownloadCompletedReceiver.kt
package com.babelsoftware.lifetools.update

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.babelsoftware.lifetools.MainActivity
import com.babelsoftware.lifetools.R

class DownloadCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE || context == null) {
            return
        }

        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) return

        val downloadManager = context.getSystemService(DownloadManager::class.java)
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusIndex != -1) {
                val status = cursor.getInt(statusIndex)
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val uri = downloadManager.getUriForDownloadedFile(downloadId)
                    if (uri != null) {
                        // DÜZELTME: downloadId'yi de fonksiyona geçiriyoruz
                        showInstallNotification(context, uri, downloadId)
                    }
                } else if (status == DownloadManager.STATUS_FAILED) {
                    Toast.makeText(context, "Güncelleme indirilemedi.", Toast.LENGTH_LONG).show()
                }
            }
        }
        cursor.close()
    }

    private fun showInstallNotification(context: Context, fileUri: Uri, downloadId: Long) { // <<<--- DÜZELTME: downloadId parametre olarak eklendi
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "app_update_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Uygulama Güncellemeleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yeni uygulama sürümleri indirildiğinde bildirim gösterir."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val installIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_INSTALL_UPDATE"
            data = fileUri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            downloadId.toInt(), // Artık bu değişkene erişebiliyoruz
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_system_update) // drawable'a bir ikon ekleyin
            .setContentTitle("Güncelleme İndirildi")
            .setContentText("Kurulumu başlatmak için dokunun.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(downloadId.toInt(), notification) // Artık bu değişkene de erişebiliyoruz
    }
}