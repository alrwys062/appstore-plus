package com.example.data

import com.example.model.DownloadProgress
import com.example.model.DownloadStatus
import com.example.model.StoreItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object DownloadManager {

  private val scope = CoroutineScope(Dispatchers.Default)
  private val activeJobs = mutableMapOf<String, Job>()

  private val _downloads = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
  val downloads: StateFlow<Map<String, DownloadProgress>> = _downloads.asStateFlow()

  fun startDownload(item: StoreItem) {
    val appId = item.id
    if (activeJobs[appId]?.isActive == true) return

    val current = _downloads.value.toMutableMap()
    current[appId] = DownloadProgress(
      appId = appId,
      progress = 0.05f,
      status = DownloadStatus.DOWNLOADING,
      downloadedSize = "0 MB",
      totalSize = item.fileSize
    )
    _downloads.value = current

    val job = scope.launch {
      var progress = 0.05f
      while (progress < 1.0f) {
        delay(300)
        progress += 0.08f
        if (progress > 1.0f) progress = 1.0f

        val updated = _downloads.value.toMutableMap()
        updated[appId] = DownloadProgress(
          appId = appId,
          progress = progress,
          status = if (progress >= 1.0f) DownloadStatus.COMPLETED else DownloadStatus.DOWNLOADING,
          downloadedSize = "${(progress * 50).toInt()} MB",
          totalSize = item.fileSize
        )
        _downloads.value = updated
      }
    }
    activeJobs[appId] = job
  }

  fun cancelDownload(appId: String) {
    activeJobs[appId]?.cancel()
    activeJobs.remove(appId)
    val current = _downloads.value.toMutableMap()
    current.remove(appId)
    _downloads.value = current
  }

  fun getProgressForApp(appId: String): DownloadProgress? {
    return _downloads.value[appId]
  }
}
