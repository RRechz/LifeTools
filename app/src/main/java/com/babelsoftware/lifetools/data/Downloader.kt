// File: data/Downloader.kt (veya update/Downloader.kt)
package com.babelsoftware.lifetools.data

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface Downloader {
    suspend fun downloadFile(
        url: String,
        fileName: String,
        description: String,
        onProgress: (Long, Long) -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}


class AndroidDownloader(
    private val context: Context
) : Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override suspend fun downloadFile(
        url: String,
        fileName: String,
        description: String,
        onProgress: (Long, Long) -> Unit,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val request = DownloadManager.Request(Uri.parse(url))
                    .setTitle(fileName)
                    .setDescription(description)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)

                val downloadId = downloadManager.enqueue(request)

                var downloading = true

                while (downloading) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val bytesDownloaded =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)).toLong()
                        val bytesTotal =
                            cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)).toLong()

                        onProgress(bytesDownloaded, bytesTotal)

                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                            downloading = false
                        }
                    }
                    cursor.close()
                    Thread.sleep(500)
                }

                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.localizedMessage ?: "İndirme hatası")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "İndirme hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}