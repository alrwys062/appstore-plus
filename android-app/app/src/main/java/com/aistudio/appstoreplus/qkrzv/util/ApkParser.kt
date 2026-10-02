package com.aistudio.appstoreplus.qkrzv.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import com.aistudio.appstoreplus.qkrzv.model.ApkMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

object ApkParser {

  private const val TAG = "ApkParser"

  suspend fun parseApkUri(context: Context, uri: Uri): ApkMetadata = withContext(Dispatchers.IO) {
    try {
      val tempFile = File(context.cacheDir, "uploaded_${System.currentTimeMillis()}.apk")
      val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

      if (inputStream == null) {
        return@withContext ApkMetadata(
          isValid = false,
          errorMessage = "تعذر قراءة ملف الـ APK المختار من الذاكرة."
        )
      }

      FileOutputStream(tempFile).use { output ->
        inputStream.copyTo(output)
      }

      parseApkFile(context, tempFile)
    } catch (e: Exception) {
      Log.e(TAG, "Error reading APK uri: $uri", e)
      ApkMetadata(
        isValid = false,
        errorMessage = "خطأ أثناء معالجة ملف APK: ${e.localizedMessage}"
      )
    }
  }

  suspend fun parseCurrentInstalledApk(context: Context): ApkMetadata = withContext(Dispatchers.IO) {
    try {
      val baseApkPath = context.applicationInfo.sourceDir
      val apkFile = File(baseApkPath)
      if (apkFile.exists()) {
        parseApkFile(context, apkFile)
      } else {
        ApkMetadata(
          isValid = false,
          errorMessage = "ملف APK الأساسي غير متوفر في مسار النظام."
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error reading base APK", e)
      ApkMetadata(
        isValid = false,
        errorMessage = "خطأ: ${e.localizedMessage}"
      )
    }
  }

  fun parseApkFile(context: Context, apkFile: File): ApkMetadata {
    try {
      val pm = context.packageManager
      val flags = PackageManager.GET_META_DATA or PackageManager.GET_ACTIVITIES
      val packageInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, flags)

      if (packageInfo == null) {
        return ApkMetadata(
          isValid = false,
          errorMessage = "الملف غير صالح أو ليس حزمة APK معتمدة لنظام Android."
        )
      }

      val appInfo = packageInfo.applicationInfo ?: return ApkMetadata(
        isValid = false,
        errorMessage = "تعذر قراءة بيانات التطبيق من حزمة APK."
      )
      appInfo.sourceDir = apkFile.absolutePath
      appInfo.publicSourceDir = apkFile.absolutePath

      // 1. App Name
      val appName = try {
        appInfo.loadLabel(pm).toString().takeIf { it.isNotBlank() }
          ?: packageInfo.packageName.substringAfterLast(".")
      } catch (e: Exception) {
        packageInfo.packageName
      }

      // 2. Package Name
      val packageName = packageInfo.packageName ?: ""

      // 3. Version Name
      val versionName = packageInfo.versionName ?: "1.0.0"

      // 4. Version Code
      val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.longVersionCode.toInt()
      } else {
        @Suppress("DEPRECATION")
        packageInfo.versionCode
      }

      // 5. File Size
      val fileSize = formatFileSize(apkFile.length())

      // 6. Minimum & Target SDK
      val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        "Android API ${appInfo.minSdkVersion} (${getAndroidVersionName(appInfo.minSdkVersion)})"
      } else {
        "Android 7.0+ (API 24)"
      }

      val targetSdk = "Android API ${appInfo.targetSdkVersion} (${getAndroidVersionName(appInfo.targetSdkVersion)})"

      // 7. CPU Architecture / ABIs
      val cpuArchitecture = detectCpuArchitecture(apkFile)

      // 8. Extract Icon
      val iconUri = try {
        val iconDrawable = appInfo.loadIcon(pm)
        saveIconToFile(context, iconDrawable, packageName)
      } catch (e: Exception) {
        Log.w(TAG, "Failed to load icon from APK", e)
        ""
      }

      return ApkMetadata(
        appName = appName,
        packageName = packageName,
        versionName = versionName,
        versionCode = versionCode,
        fileSize = fileSize,
        minSdk = minSdk,
        targetSdk = targetSdk,
        cpuArchitecture = cpuArchitecture,
        iconUri = iconUri,
        isValid = true
      )
    } catch (e: Exception) {
      Log.e(TAG, "Failed parsing APK file", e)
      return ApkMetadata(
        isValid = false,
        errorMessage = "فشل استخراج بيانات الـ APK: ${e.localizedMessage}"
      )
    }
  }

  private fun detectCpuArchitecture(apkFile: File): String {
    val abis = mutableSetOf<String>()
    try {
      ZipFile(apkFile).use { zip ->
        val entries = zip.entries()
        while (entries.hasMoreElements()) {
          val entry = entries.nextElement()
          val name = entry.name
          if (name.startsWith("lib/")) {
            val parts = name.split("/")
            if (parts.size >= 2) {
              val abi = parts[1]
              if (abi.isNotBlank()) abis.add(abi)
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error reading APK zip entries for ABIs", e)
    }

    return if (abis.isEmpty()) {
      "Universal (بدون مكتبات أصلية C++ / جميع المعالجات)"
    } else {
      abis.joinToString(", ")
    }
  }

  private fun saveIconToFile(context: Context, drawable: Drawable, packageName: String): String {
    val bitmap = when (drawable) {
      is BitmapDrawable -> drawable.bitmap
      else -> {
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 144
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 144
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bmp
      }
    }

    val iconFile = File(context.cacheDir, "extracted_${packageName}_${System.currentTimeMillis()}.png")
    FileOutputStream(iconFile).use { out ->
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return iconFile.absolutePath
  }

  private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0

    return when {
      gb >= 1.0 -> String.format("%.2f GB", gb)
      mb >= 1.0 -> String.format("%.1f MB", mb)
      else -> String.format("%.0f KB", kb)
    }
  }

  private fun getAndroidVersionName(apiLevel: Int): String {
    return when (apiLevel) {
      21 -> "5.0 Lollipop"
      22 -> "5.1 Lollipop"
      23 -> "6.0 Marshmallow"
      24 -> "7.0 Nougat"
      25 -> "7.1 Nougat"
      26 -> "8.0 Oreo"
      27 -> "8.1 Oreo"
      28 -> "9.0 Pie"
      29 -> "Android 10"
      30 -> "Android 11"
      31, 32 -> "Android 12"
      33 -> "Android 13"
      34 -> "Android 14"
      35 -> "Android 15"
      36 -> "Android 16"
      else -> "Android SDK $apiLevel"
    }
  }
}
