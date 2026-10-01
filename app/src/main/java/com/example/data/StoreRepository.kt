package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.CategoryItem
import com.example.model.StoreItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StoreRepository(private val context: Context) {

  private val tag = "StoreRepository"

  private val firestore: FirebaseFirestore by lazy {
    val dbId = context.getString(R.string.firestore_database_id)
    FirebaseFirestore.getInstance(dbId)
  }

  private val appsCollection = firestore.collection("apps")
  private val categoriesCollection = firestore.collection("categories")

  // Observe all published items for the store
  fun getStoreItems(): Flow<List<StoreItem>> = callbackFlow {
    // Send initial default items immediately so UI is never blank
    trySend(getDefaultItems())

    val listener = appsCollection
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(tag, "Listen failed for store items, using fallback catalog: ${error.message}")
          trySend(getDefaultItems())
          return@addSnapshotListener
        }
        if (snapshot != null && !snapshot.isEmpty) {
          val items = snapshot.documents.mapNotNull { doc ->
            doc.toObject(StoreItem::class.java)?.copy(id = doc.id)
          }.filter { it.isPublished }
          
          if (items.isNotEmpty()) {
            trySend(items)
          } else {
            trySend(getDefaultItems())
          }
        } else {
          trySend(getDefaultItems())
        }
      }
    awaitClose { listener.remove() }
  }

  // Observe all items for Admin Dashboard (including unpublished)
  fun getAllItemsForAdmin(): Flow<List<StoreItem>> = callbackFlow {
    trySend(getDefaultItems())

    val listener = appsCollection
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(tag, "Listen failed for admin items: ${error.message}")
          trySend(getDefaultItems())
          return@addSnapshotListener
        }
        if (snapshot != null && !snapshot.isEmpty) {
          val items = snapshot.documents.mapNotNull { doc ->
            doc.toObject(StoreItem::class.java)?.copy(id = doc.id)
          }
          if (items.isNotEmpty()) {
            trySend(items)
          } else {
            trySend(getDefaultItems())
          }
        } else {
          trySend(getDefaultItems())
        }
      }
    awaitClose { listener.remove() }
  }

  // Observe categories
  fun getCategories(): Flow<List<CategoryItem>> = callbackFlow {
    trySend(getDefaultCategories())

    val listener = categoriesCollection
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(tag, "Listen failed for categories: ${error.message}")
          trySend(getDefaultCategories())
          return@addSnapshotListener
        }
        if (snapshot != null && !snapshot.isEmpty) {
          val items = snapshot.documents.mapNotNull { doc ->
            doc.toObject(CategoryItem::class.java)?.copy(id = doc.id)
          }
          if (items.isNotEmpty()) {
            trySend(items)
          } else {
            trySend(getDefaultCategories())
          }
        } else {
          trySend(getDefaultCategories())
        }
      }
    awaitClose { listener.remove() }
  }

  // Get item by ID
  suspend fun getItemById(id: String): StoreItem? {
    return try {
      val doc = appsCollection.document(id).get().await()
      doc.toObject(StoreItem::class.java)?.copy(id = doc.id)
        ?: getDefaultItems().find { it.id == id }
    } catch (e: Exception) {
      Log.e(tag, "Failed to get item $id from Firestore", e)
      getDefaultItems().find { it.id == id }
    }
  }

  // Admin: Add or Edit Store Item
  suspend fun saveStoreItem(item: StoreItem): Boolean {
    return try {
      val docRef = if (item.id.isNotBlank()) {
        appsCollection.document(item.id)
      } else {
        appsCollection.document()
      }
      val itemToSave = item.copy(id = docRef.id)
      docRef.set(itemToSave).await()
      Log.i(tag, "Saved store item ${itemToSave.name} to Firestore")
      true
    } catch (e: Exception) {
      Log.e(tag, "Error saving item to Firestore", e)
      false
    }
  }

  // Admin: Delete Store Item
  suspend fun deleteStoreItem(id: String): Boolean {
    return try {
      appsCollection.document(id).delete().await()
      true
    } catch (e: Exception) {
      Log.e(tag, "Error deleting item $id", e)
      false
    }
  }

  // Admin: Toggle Published
  suspend fun togglePublish(id: String, currentStatus: Boolean): Boolean {
    return try {
      appsCollection.document(id).update("isPublished", !currentStatus).await()
      true
    } catch (e: Exception) {
      Log.e(tag, "Error updating publish status", e)
      false
    }
  }

  // Admin: Toggle Featured
  suspend fun toggleFeatured(id: String, currentStatus: Boolean): Boolean {
    return try {
      appsCollection.document(id).update("isFeatured", !currentStatus).await()
      true
    } catch (e: Exception) {
      Log.e(tag, "Error updating featured status", e)
      false
    }
  }

  // Admin: Save Category
  suspend fun saveCategory(category: CategoryItem): Boolean {
    return try {
      val docRef = if (category.id.isNotBlank()) {
        categoriesCollection.document(category.id)
      } else {
        categoriesCollection.document()
      }
      docRef.set(category.copy(id = docRef.id)).await()
      true
    } catch (e: Exception) {
      Log.e(tag, "Error saving category", e)
      false
    }
  }

  // Admin: Delete Category
  suspend fun deleteCategory(id: String): Boolean {
    return try {
      categoriesCollection.document(id).delete().await()
      true
    } catch (e: Exception) {
      Log.e(tag, "Error deleting category $id", e)
      false
    }
  }

  // Seed default data if database is empty
  suspend fun seedInitialDataIfEmpty() {
    try {
      val existingApps = appsCollection.limit(1).get().await()
      if (existingApps.isEmpty) {
        Log.i(tag, "Firestore apps empty, seeding default catalog")
        val defaultItems = getDefaultItems()
        for (item in defaultItems) {
          try {
            appsCollection.document(item.id).set(item).await()
          } catch (itemErr: Exception) {
            Log.w(tag, "Failed to seed item ${item.id}: ${itemErr.message}")
          }
        }
      }

      val existingCategories = categoriesCollection.limit(1).get().await()
      if (existingCategories.isEmpty) {
        val defaultCategories = getDefaultCategories()
        for (cat in defaultCategories) {
          try {
            categoriesCollection.document(cat.id).set(cat).await()
          } catch (catErr: Exception) {
            Log.w(tag, "Failed to seed category ${cat.id}: ${catErr.message}")
          }
        }
      }
    } catch (e: Exception) {
      Log.w(tag, "Initial data seeding check failed (using local defaults): ${e.message}")
    }
  }

  fun getDefaultItems(): List<StoreItem> {
    return listOf(
      StoreItem(
        id = "gta-v",
        name = "Grand Theft Auto V",
        packageName = "com.rockstargames.gtav",
        developer = "Rockstar Games",
        type = "game",
        category = "ألعاب",
        categoryTags = listOf("عالم مفتوح", "أكشن", "مغامرة"),
        iconUrl = "android.resource://${context.packageName}/drawable/hero_gta",
        bannerUrl = "android.resource://${context.packageName}/drawable/hero_gta",
        rating = 4.5,
        downloads = "2.1 GB",
        downloadCount = 50000000L,
        fileSize = "2.1 GB",
        version = "v1.0.8",
        versionCode = 108,
        description = "استمتع بتجربة لا مثيل لها في عالم مفتوح ضخم مليء بالإثارة والمغامرات. حيث يمكنك استكشاف المدينة والقيام بالمهام وعيش حياة الجريمة بكل تفاصيلها.",
        screenshots = listOf(
          "android.resource://${context.packageName}/drawable/screenshot_gta1",
          "android.resource://${context.packageName}/drawable/screenshot_gta2"
        ),
        downloadUrl = "https://example.com/games/gtav.apk",
        isPublished = true,
        isFeatured = true,
        isUpdateAvailable = false,
        updatedDate = "2026-09-15",
        compatibility = "Android 9.0+",
        minSdk = "Android API 28 (9.0 Pie)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "arm64-v8a",
        modInfo = "نسخة معدلة مع رسوميات فائقة الدقة مفتوحة لكافة الأجهزة"
      ),
      StoreItem(
        id = "whatsapp",
        name = "WhatsApp Messenger",
        packageName = "com.whatsapp",
        developer = "Meta Platforms",
        type = "app",
        category = "تواصل",
        categoryTags = listOf("مراسلة", "مكالمات", "أمان"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6b/WhatsApp.svg/512px-WhatsApp.svg.png",
        bannerUrl = "",
        rating = 4.5,
        downloads = "2.8B",
        downloadCount = 2800000000L,
        fileSize = "58 MB",
        version = "v2.25.27.78",
        versionCode = 22527,
        description = "تطبيق واتساب يتيح لك مراسلة أصدقائك وعائلتك بسهولة وأمان. مع إمكانية إرسال الصور والفيديوهات والملفات والمحادثات المشفرة تماماً.",
        screenshots = listOf(
          "android.resource://${context.packageName}/drawable/screenshot_wa1"
        ),
        downloadUrl = "https://example.com/apps/whatsapp.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = true,
        updatedDate = "2026-10-01",
        compatibility = "Android 5.0+",
        minSdk = "Android API 21 (5.0 Lollipop)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal (all ABIs)"
      ),
      StoreItem(
        id = "instagram",
        name = "Instagram",
        packageName = "com.instagram.android",
        developer = "Meta Platforms",
        type = "app",
        category = "اجتماعي",
        categoryTags = listOf("صور", "فيديو", "ريلز"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/Instagram_icon.png/512px-Instagram_icon.png",
        bannerUrl = "",
        rating = 4.6,
        downloads = "1.5B",
        downloadCount = 1500000000L,
        fileSize = "64 MB",
        version = "v308.0.0",
        versionCode = 30800,
        description = "تواصل مع الأصدقاء وشارك الصور والفيديوهات والقصص اليومية مع ملايين المبدعين حول العالم.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/instagram.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = true,
        updatedDate = "2026-09-28",
        compatibility = "Android 7.0+",
        minSdk = "Android API 24 (7.0 Nougat)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal"
      ),
      StoreItem(
        id = "tiktok",
        name = "TikTok",
        packageName = "com.zhiliaoapp.musically",
        developer = "TikTok Pte. Ltd.",
        type = "app",
        category = "ترفيه",
        categoryTags = listOf("فيديوهات قصيرة", "موسيقى", "مؤثرات"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/a/a9/TikTok_logo.svg/512px-TikTok_logo.svg.png",
        bannerUrl = "",
        rating = 4.4,
        downloads = "1.2B",
        downloadCount = 1200000000L,
        fileSize = "85 MB",
        version = "v37.5.0",
        versionCode = 3750,
        description = "شاهد واستكشف مقاطع فيديو قصيرة ومسلية وصنع محتواك الخاص بمؤثرات صوتية وموسيقى رائعة.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/tiktok.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = true,
        updatedDate = "2026-09-25",
        compatibility = "Android 6.0+",
        minSdk = "Android API 23 (6.0 Marshmallow)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal"
      ),
      StoreItem(
        id = "capcut",
        name = "CapCut",
        packageName = "com.lemon.lvoverseas",
        developer = "Bytedance Pte. Ltd.",
        type = "app",
        category = "تصوير",
        categoryTags = listOf("مونتاج", "فيديو", "تصميم"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/66/Capcut_logo.png/512px-Capcut_logo.png",
        bannerUrl = "",
        rating = 4.7,
        downloads = "892M",
        downloadCount = 892000000L,
        fileSize = "112 MB",
        version = "v12.4.0",
        versionCode = 1240,
        description = "محرر فيديو احترافي وسهل الاستخدام لصناعة مقاطع فيديو سينمائية بمؤثرات وانتقالات مذهلة.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/capcut.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = false,
        updatedDate = "2026-09-20",
        compatibility = "Android 7.0+",
        minSdk = "Android API 24 (7.0 Nougat)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "arm64-v8a, armeabi-v7a",
        modInfo = "نسخة Pro مفتوحة لجميع القوالب والمؤثرات بدون علامة مائية"
      ),
      StoreItem(
        id = "telegram",
        name = "Telegram",
        packageName = "org.telegram.messenger",
        developer = "Telegram FZ-LLC",
        type = "app",
        category = "تواصل",
        categoryTags = listOf("سريع", "آمن", "سحابي"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/Telegram_logo.svg/512px-Telegram_logo.svg.png",
        bannerUrl = "",
        rating = 4.8,
        downloads = "948M",
        downloadCount = 948000000L,
        fileSize = "48 MB",
        version = "v10.6.1",
        versionCode = 1061,
        description = "تراسل فوري عالي السرعة وبسيط وسهل الاستخدام، مع إمكانية إنشاء قنوات ومجموعات غير محدودة.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/telegram.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = false,
        updatedDate = "2026-09-18",
        compatibility = "Android 6.0+",
        minSdk = "Android API 23 (6.0 Marshmallow)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal"
      ),
      StoreItem(
        id = "spotify",
        name = "Spotify",
        packageName = "com.spotify.music",
        developer = "Spotify AB",
        type = "app",
        category = "ترفيه",
        categoryTags = listOf("موسيقى", "بودكاست", "صوتيات"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Spotify_logo_without_text.svg/512px-Spotify_logo_without_text.svg.png",
        bannerUrl = "",
        rating = 4.5,
        downloads = "692M",
        downloadCount = 692000000L,
        fileSize = "39 MB",
        version = "v8.9.18",
        versionCode = 8918,
        description = "استمع إلى أغانيك وقوائمك الموسيقية المفضلة وبودكاست بجودة صوت فائقة النقاء.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/spotify.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = false,
        updatedDate = "2026-09-10",
        compatibility = "Android 7.0+",
        minSdk = "Android API 24 (7.0 Nougat)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal",
        modInfo = "نسخة Premium بدون إعلانات مع تخطي غير محدود للأغاني"
      ),
      StoreItem(
        id = "youtube",
        name = "YouTube",
        packageName = "com.google.android.youtube",
        developer = "Google LLC",
        type = "app",
        category = "ترفيه",
        categoryTags = listOf("فيديوهات", "بث مباشر", "موسيقى"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/09/YouTube_full-color_icon_%282017%29.svg/512px-YouTube_full-color_icon_%282017%29.svg.png",
        bannerUrl = "",
        rating = 4.6,
        downloads = "5B+",
        downloadCount = 5000000000L,
        fileSize = "42 MB",
        version = "v19.16.36",
        versionCode = 191636,
        description = "شاهد أحدث مقاطع الفيديو والموسيقى وقنوات المحتوى المفضلة لديك.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/youtube.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = true,
        updatedDate = "2026-10-01",
        compatibility = "Android 8.0+",
        minSdk = "Android API 26 (8.0 Oreo)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal"
      ),
      StoreItem(
        id = "chrome",
        name = "Google Chrome",
        packageName = "com.android.chrome",
        developer = "Google LLC",
        type = "app",
        category = "أدوات",
        categoryTags = listOf("متصفح", "إنترنت", "بحث"),
        iconUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e1/Google_Chrome_icon_%28February_2022%29.svg/512px-Google_Chrome_icon_%28February_2022%29.svg.png",
        bannerUrl = "",
        rating = 4.5,
        downloads = "10B+",
        downloadCount = 10000000000L,
        fileSize = "75 MB",
        version = "v131.0.6778.200",
        versionCode = 131067,
        description = "متصفح إنترنت سريع وآمن وسهل الاستخدام من قوقل مع مزامنة علامات التبويب والحماية.",
        screenshots = emptyList(),
        downloadUrl = "https://example.com/apps/chrome.apk",
        isPublished = true,
        isFeatured = false,
        isUpdateAvailable = true,
        updatedDate = "2026-10-01",
        compatibility = "Android 8.0+",
        minSdk = "Android API 26 (8.0 Oreo)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "Universal"
      ),
      StoreItem(
        id = "cyberpunk",
        name = "Cyberpunk 2077 Mobile",
        packageName = "com.cdprojektred.cyberpunk",
        developer = "CD PROJEKT RED",
        type = "game",
        category = "ألعاب",
        categoryTags = listOf("أكشن", "RPG", "مستقبل"),
        iconUrl = "android.resource://${context.packageName}/drawable/app_logo",
        bannerUrl = "android.resource://${context.packageName}/drawable/screenshot_gta2",
        rating = 4.7,
        downloads = "15M",
        downloadCount = 15000000L,
        fileSize = "3.2 GB",
        version = "v2.1.0",
        versionCode = 210,
        description = "لعبة تقمص أدوار وأكشن ومغامرات تدور أحداثها في مدينة نايت سيتي المستقبلية المليئة بالتقنيات والمهام الحماسية.",
        screenshots = listOf("android.resource://${context.packageName}/drawable/screenshot_gta2"),
        downloadUrl = "https://example.com/games/cyberpunk.apk",
        isPublished = true,
        isFeatured = true,
        isUpdateAvailable = false,
        updatedDate = "2026-09-01",
        compatibility = "Android 10.0+",
        minSdk = "Android API 29 (Android 10)",
        targetSdk = "Android API 34 (Android 14)",
        cpuArchitecture = "arm64-v8a",
        modInfo = "أموال ونقاط مهارة غير محدودة مع رسوميات فائقة"
      )
    )
  }

  fun getDefaultCategories(): List<CategoryItem> {
    return listOf(
      CategoryItem(id = "cat_apps", name = "تطبيقات", iconName = "grid", colorHex = "#3B82F6", itemCount = 480, type = "app"),
      CategoryItem(id = "cat_games", name = "ألعاب", iconName = "gamepad", colorHex = "#8B5CF6", itemCount = 320, type = "game"),
      CategoryItem(id = "cat_ent", name = "ترفيه", iconName = "play", colorHex = "#EC4899", itemCount = 190, type = "all"),
      CategoryItem(id = "cat_tools", name = "أدوات", iconName = "wrench", colorHex = "#0EA5E9", itemCount = 260, type = "app"),
      CategoryItem(id = "cat_comm", name = "تواصل", iconName = "chat", colorHex = "#10B981", itemCount = 145, type = "app"),
      CategoryItem(id = "cat_photo", name = "تصوير", iconName = "camera", colorHex = "#A855F7", itemCount = 110, type = "app"),
      CategoryItem(id = "cat_social", name = "اجتماعي", iconName = "people", colorHex = "#F43F5E", itemCount = 95, type = "app"),
      CategoryItem(id = "cat_edu", name = "تعليم", iconName = "school", colorHex = "#6366F1", itemCount = 130, type = "app"),
      CategoryItem(id = "cat_health", name = "صحي", iconName = "heart", colorHex = "#FB7185", itemCount = 75, type = "app"),
      CategoryItem(id = "cat_biz", name = "أعمال", iconName = "business", colorHex = "#F59E0B", itemCount = 88, type = "app")
    )
  }
}
