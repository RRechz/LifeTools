// File: data/Downloader.kt (veya update/Downloader.kt)
package com.babelsoftware.lifetools.data // veya com.babelsoftware.lifetools.update

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri

interface Downloader {
    fun downloadFile(url: String, fileName: String, description: String): Long
}

class AndroidDownloader(
    private val context: Context
) : Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String, fileName: String, description: String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("application/vnd.android.package-archive") // APK dosyası olduğunu belirtiyoruz
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(fileName) // Bildirim başlığı
            .setDescription(description) // Bildirim açıklaması
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName) // Uygulamaya özel 'Downloads' klasörüne kaydet

        return downloadManager.enqueue(request) // İndirme işlemini sıraya koy ve ID'sini döndür
    }
}