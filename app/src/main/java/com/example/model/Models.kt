package com.example.model

enum class ItemType {
  APP,
  GAME
}

data class StoreItem(
  val id: String = "",
  val name: String = "",
  val packageName: String = "",
  val developer: String = "",
  val type: String = "app", // "app" or "game"
  val category: String = "تطبيقات",
  val categoryTags: List<String> = emptyList(),
  val iconUrl: String = "",
  val bannerUrl: String = "",
  val rating: Double = 4.5,
  val downloads: String = "1M",
  val downloadCount: Long = 1000000L,
  val fileSize: String = "50 MB",
  val version: String = "1.0.0",
  val versionCode: Int = 1,
  val description: String = "",
  val screenshots: List<String> = emptyList(),
  val downloadUrl: String = "",
  val isPublished: Boolean = true,
  val isFeatured: Boolean = false,
  val isUpdateAvailable: Boolean = false,
  val updatedDate: String = "",
  val compatibility: String = "Android 8.0+",
  val minSdk: String = "",
  val targetSdk: String = "",
  val cpuArchitecture: String = "Universal",
  val modInfo: String = "",
  val authorId: String = ""
)

data class ApkMetadata(
  val appName: String = "",
  val packageName: String = "",
  val versionName: String = "",
  val versionCode: Int = 0,
  val fileSize: String = "",
  val minSdk: String = "",
  val targetSdk: String = "",
  val cpuArchitecture: String = "Universal (all ABIs)",
  val iconUri: String = "",
  val isValid: Boolean = true,
  val errorMessage: String? = null
)

data class CategoryItem(
  val id: String = "",
  val name: String = "",
  val iconName: String = "",
  val colorHex: String = "#3B82F6",
  val itemCount: Int = 0,
  val type: String = "all" // "app", "game", "all"
)

data class UserAccount(
  val uid: String = "",
  val name: String = "مستخدم AppStore",
  val email: String = "",
  val photoUrl: String = "",
  val isAdmin: Boolean = false,
  val favorites: List<String> = emptyList()
)

enum class DownloadStatus {
  IDLE,
  QUEUED,
  DOWNLOADING,
  PAUSED,
  COMPLETED,
  FAILED
}

data class DownloadProgress(
  val appId: String = "",
  val progress: Float = 0f,
  val status: DownloadStatus = DownloadStatus.IDLE,
  val downloadedSize: String = "0 MB",
  val totalSize: String = "0 MB"
)
