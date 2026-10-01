package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.ApkMetadata
import com.example.model.CategoryItem
import com.example.model.StoreItem
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ButtonInstallBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevated
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.ApkParser
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(
  items: List<StoreItem>,
  categories: List<CategoryItem>,
  onBack: () -> Unit,
  onSaveItem: (StoreItem, () -> Unit) -> Unit,
  onDeleteItem: (String) -> Unit,
  onTogglePublish: (String, Boolean) -> Unit,
  onToggleFeatured: (String, Boolean) -> Unit,
  onSaveCategory: (CategoryItem, () -> Unit) -> Unit,
  onDeleteCategory: (String) -> Unit
) {
  BackHandler { onBack() }

  var selectedTab by remember { mutableIntStateOf(0) }
  var showItemDialog by remember { mutableStateOf(false) }
  var editingItem by remember { mutableStateOf<StoreItem?>(null) }
  var showCategoryDialog by remember { mutableStateOf(false) }

  val totalApps = items.count { it.type == "app" }
  val totalGames = items.count { it.type == "game" }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.testTag("admin_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "رجوع",
          tint = TextPrimary
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "لوحة تحكم المشرف",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    // Stats Grid Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      AdminStatCard(
        title = "تطبيقات",
        value = "$totalApps",
        icon = Icons.Default.Apps,
        color = Color(0xFF3B82F6),
        modifier = Modifier.weight(1f)
      )
      AdminStatCard(
        title = "ألعاب",
        value = "$totalGames",
        icon = Icons.Default.SportsEsports,
        color = Color(0xFF8B5CF6),
        modifier = Modifier.weight(1f)
      )
      AdminStatCard(
        title = "تصنيفات",
        value = "${categories.size}",
        icon = Icons.Default.Category,
        color = Color(0xFF10B981),
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Navigation Tabs
    val tabs = listOf("التطبيقات والألعاب", "التصنيفات")
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = CardDark,
      contentColor = PrimaryBlue,
      edgePadding = 16.dp,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = PrimaryBlue
        )
      }
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = {
            Text(
              text = title,
              color = if (selectedTab == index) PrimaryBlue else TextSecondary,
              fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
            )
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Actions Bar: Add Button
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (selectedTab == 0) "إدارة عناصر المتجر (${items.size})" else "إدارة التصنيفات (${categories.size})",
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary
      )

      Button(
        onClick = {
          if (selectedTab == 0) {
            editingItem = null
            showItemDialog = true
          } else {
            showCategoryDialog = true
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ButtonInstallBlue),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = Modifier.testTag("admin_add_button")
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (selectedTab == 0) "إضافة جديد (تحليل APK)" else "إضافة تصنيف",
          fontSize = 12.sp,
          color = Color.White
        )
      }
    }

    // Tab Contents
    if (selectedTab == 0) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(items) { item ->
          AdminItemCard(
            item = item,
            onEdit = {
              editingItem = item
              showItemDialog = true
            },
            onDelete = { onDeleteItem(item.id) },
            onTogglePublish = { onTogglePublish(item.id, item.isPublished) },
            onToggleFeatured = { onToggleFeatured(item.id, item.isFeatured) }
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(categories) { cat ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(CardDark)
              .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = cat.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "رمز: ${cat.iconName} • عناصر: ${cat.itemCount}",
                fontSize = 12.sp,
                color = TextSecondary
              )
            }
            IconButton(onClick = { onDeleteCategory(cat.id) }) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "حذف التصنيف",
                tint = Color(0xFFEF4444)
              )
            }
          }
        }
      }
    }
  }

  // Add / Edit Item Dialog with Automatic APK Extraction
  if (showItemDialog) {
    StoreItemFormDialog(
      item = editingItem,
      categories = categories.map { it.name },
      onDismiss = { showItemDialog = false },
      onSave = { savedItem ->
        onSaveItem(savedItem) {
          showItemDialog = false
        }
      }
    )
  }

  // Add Category Dialog
  if (showCategoryDialog) {
    CategoryFormDialog(
      onDismiss = { showCategoryDialog = false },
      onSave = { cat ->
        onSaveCategory(cat) {
          showCategoryDialog = false
        }
      }
    )
  }
}

@Composable
private fun AdminStatCard(
  title: String,
  value: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(CardDark)
      .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
      .padding(12.dp)
  ) {
    Column {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = color,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      Text(
        text = title,
        fontSize = 11.sp,
        color = TextSecondary
      )
    }
  }
}

@Composable
private fun AdminItemCard(
  item: StoreItem,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onTogglePublish: () -> Unit,
  onToggleFeatured: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CardDark)
      .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(BackgroundDark)
    ) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(item.iconUrl.ifEmpty { R.drawable.app_logo })
          .crossfade(true)
          .build(),
        contentDescription = item.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = item.name,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        maxLines = 1
      )
      Text(
        text = "${item.category} • ${item.version} (${item.fileSize})",
        fontSize = 11.sp,
        color = TextSecondary
      )
      if (item.packageName.isNotBlank()) {
        Text(
          text = item.packageName,
          fontSize = 10.sp,
          color = TextMuted,
          maxLines = 1
        )
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = if (item.isPublished) "منشور" else "مسودة (غير منشور)",
          fontSize = 10.sp,
          color = if (item.isPublished) Color(0xFF10B981) else Color(0xFFF59E0B)
        )
        if (item.isFeatured) {
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "★ مميز", fontSize = 10.sp, color = Color(0xFF38BDF8))
        }
      }
    }

    IconButton(onClick = onTogglePublish, modifier = Modifier.size(32.dp)) {
      Icon(
        imageVector = if (item.isPublished) Icons.Default.Visibility else Icons.Default.VisibilityOff,
        contentDescription = "نشر/إلغاء النشر",
        tint = if (item.isPublished) Color(0xFF10B981) else TextMuted,
        modifier = Modifier.size(18.dp)
      )
    }

    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
      Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = "تعديل",
        tint = PrimaryBlue,
        modifier = Modifier.size(18.dp)
      )
    }

    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
      Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = "حذف",
        tint = Color(0xFFEF4444),
        modifier = Modifier.size(18.dp)
      )
    }
  }
}

@Composable
fun StoreItemFormDialog(
  item: StoreItem?,
  categories: List<String>,
  onDismiss: () -> Unit,
  onSave: (StoreItem) -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // Detected from APK automatically
  var detectedApk by remember { mutableStateOf<ApkMetadata?>(null) }
  var isAnalyzingApk by remember { mutableStateOf(false) }
  var apkError by remember { mutableStateOf<String?>(null) }

  var name by remember { mutableStateOf(item?.name ?: "") }
  var packageName by remember { mutableStateOf(item?.packageName ?: "") }
  var version by remember { mutableStateOf(item?.version ?: "1.0.0") }
  var versionCode by remember { mutableIntStateOf(item?.versionCode ?: 1) }
  var fileSize by remember { mutableStateOf(item?.fileSize ?: "45 MB") }
  var iconUrl by remember { mutableStateOf(item?.iconUrl ?: "") }
  var minSdk by remember { mutableStateOf(item?.minSdk ?: "") }
  var targetSdk by remember { mutableStateOf(item?.targetSdk ?: "") }
  var cpuArch by remember { mutableStateOf(item?.cpuArchitecture ?: "Universal") }

  // Manually entered by Admin
  var developer by remember { mutableStateOf(item?.developer ?: "") }
  var type by remember { mutableStateOf(item?.type ?: "app") }
  var category by remember { mutableStateOf(item?.category ?: if (categories.isNotEmpty()) categories.first() else "تطبيقات") }
  var description by remember { mutableStateOf(item?.description ?: "") }
  var modInfo by remember { mutableStateOf(item?.modInfo ?: "") }
  var downloadUrl by remember { mutableStateOf(item?.downloadUrl ?: "") }
  var isPublished by remember { mutableStateOf(item?.isPublished ?: true) }
  var isFeatured by remember { mutableStateOf(item?.isFeatured ?: false) }

  // File Picker for APK files
  val apkPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      isAnalyzingApk = true
      apkError = null
      coroutineScope.launch {
        val metadata = ApkParser.parseApkUri(context, uri)
        isAnalyzingApk = false
        if (metadata.isValid) {
          detectedApk = metadata
          name = metadata.appName
          packageName = metadata.packageName
          version = metadata.versionName
          versionCode = metadata.versionCode
          fileSize = metadata.fileSize
          minSdk = metadata.minSdk
          targetSdk = metadata.targetSdk
          cpuArch = metadata.cpuArchitecture
          if (metadata.iconUri.isNotBlank()) {
            iconUrl = metadata.iconUri
          }
          Toast.makeText(context, "تم تحليل APK واستخراج كافة البيانات بنجاح!", Toast.LENGTH_SHORT).show()
        } else {
          apkError = metadata.errorMessage ?: "فشل تحليل ملف APK."
        }
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (item != null) "تعديل تطبيق/لعبة" else "إضافة تطبيق مع تحليل APK التلقائي",
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // === SECTION 1: AUTOMATIC APK UPLOAD & DETECTION ===
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, PrimaryBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(14.dp)
        ) {
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "استخراج بيانات APK تلقائياً",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "اختر ملف APK ليتم ملء الاسم والحزمة والإصدار والحجم والأيقونة تلقائياً",
                  fontSize = 11.sp,
                  color = TextSecondary
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isAnalyzingApk) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  color = PrimaryBlue,
                  strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "جاري فحص وتحليل حزمة الـ APK...",
                  fontSize = 13.sp,
                  color = PrimaryBlue
                )
              }
            } else {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = {
                    apkPickerLauncher.launch("application/vnd.android.package-archive")
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = ButtonInstallBlue),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.weight(1f),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("رفع ملف APK", fontSize = 12.sp, color = Color.White)
                }

                OutlinedButton(
                  onClick = {
                    isAnalyzingApk = true
                    apkError = null
                    coroutineScope.launch {
                      val metadata = ApkParser.parseCurrentInstalledApk(context)
                      isAnalyzingApk = false
                      if (metadata.isValid) {
                        detectedApk = metadata
                        name = metadata.appName
                        packageName = metadata.packageName
                        version = metadata.versionName
                        versionCode = metadata.versionCode
                        fileSize = metadata.fileSize
                        minSdk = metadata.minSdk
                        targetSdk = metadata.targetSdk
                        cpuArch = metadata.cpuArchitecture
                        if (metadata.iconUri.isNotBlank()) {
                          iconUrl = metadata.iconUri
                        }
                        Toast.makeText(context, "تم تحليل الحزمة النموذجية بنجاح!", Toast.LENGTH_SHORT).show()
                      } else {
                        apkError = metadata.errorMessage
                      }
                    }
                  },
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.weight(1f),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                  Text("حزمة تجريبية", fontSize = 11.sp, color = TextPrimary)
                }
              }
            }

            if (apkError != null) {
              Spacer(modifier = Modifier.height(8.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Error,
                  contentDescription = null,
                  tint = Color(0xFFEF4444),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = apkError ?: "",
                  color = Color(0xFFEF4444),
                  fontSize = 11.sp
                )
              }
            }

            // Summary Card of Detected Data
            if (detectedApk != null) {
              Spacer(modifier = Modifier.height(10.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFF0F291E))
                  .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                  .padding(10.dp)
              ) {
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = Color(0xFF10B981),
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "البيانات المستخرجة تلقائياً من APK:",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF10B981)
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(text = "• التطبيق: $name", fontSize = 11.sp, color = TextPrimary)
                  Text(text = "• الحزمة: $packageName", fontSize = 11.sp, color = TextSecondary)
                  Text(text = "• الإصدار: $version (كود: $versionCode)", fontSize = 11.sp, color = TextSecondary)
                  Text(text = "• حجم الملف: $fileSize", fontSize = 11.sp, color = TextSecondary)
                  if (minSdk.isNotBlank()) Text(text = "• الحد الأدنى: $minSdk", fontSize = 11.sp, color = TextSecondary)
                  if (targetSdk.isNotBlank()) Text(text = "• المستهدف: $targetSdk", fontSize = 11.sp, color = TextSecondary)
                  if (cpuArch.isNotBlank()) Text(text = "• معمارية المعالج (ABI): $cpuArch", fontSize = 11.sp, color = TextSecondary)
                }
              }
            }
          }
        }

        // === SECTION 2: REVIEW DETECTED APK FIELDS ===
        Text(
          text = "مراجعة بيانات APK المستخرجة (يمكن التعديل عند الحاجة):",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextSecondary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (iconUrl.isNotBlank()) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            ) {
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(iconUrl)
                  .crossfade(true)
                  .build(),
                contentDescription = "أيقونة التطبيق",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
          }
          Column(modifier = Modifier.weight(1f)) {
            AdminTextField(value = name, onValueChange = { name = it }, label = "اسم التطبيق (تلقائي)")
          }
        }

        AdminTextField(value = packageName, onValueChange = { packageName = it }, label = "اسم الحزمة Package Name (تلقائي)")
        
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(modifier = Modifier.weight(1f)) {
            AdminTextField(value = version, onValueChange = { version = it }, label = "الإصدار Version (تلقائي)")
          }
          Box(modifier = Modifier.weight(1f)) {
            AdminTextField(value = fileSize, onValueChange = { fileSize = it }, label = "حجم الملف Size (تلقائي)")
          }
        }

        if (minSdk.isNotBlank() || targetSdk.isNotBlank()) {
          AdminTextField(value = minSdk, onValueChange = { minSdk = it }, label = "التوافق Minimum SDK (تلقائي)")
        }

        if (cpuArch.isNotBlank()) {
          AdminTextField(value = cpuArch, onValueChange = { cpuArch = it }, label = "معمارية المعالج ABI (تلقائي)")
        }

        // === SECTION 3: STORE INFORMATION (MANUALLY ENTERED BY ADMIN) ===
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "بيانات المتجر (مُدخلة يدوياً بواسطة المشرف):",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = PrimaryBlue
        )

        // App or Game
        Row(verticalAlignment = Alignment.CenterVertically) {
          RadioButton(
            selected = type == "app",
            onClick = { type = "app" },
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
          )
          Text("تطبيق", color = TextPrimary, fontSize = 13.sp)
          Spacer(modifier = Modifier.width(16.dp))
          RadioButton(
            selected = type == "game",
            onClick = { type = "game" },
            colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
          )
          Text("لعبة", color = TextPrimary, fontSize = 13.sp)
        }

        AdminTextField(value = developer, onValueChange = { developer = it }, label = "المطور (Developer)")
        AdminTextField(value = category, onValueChange = { category = it }, label = "التصنيف (Category)")
        AdminTextField(value = modInfo, onValueChange = { modInfo = it }, label = "معلومات التعديل / الميزات (Mod Information)")
        AdminTextField(value = description, onValueChange = { description = it }, label = "الوصف (Description)", singleLine = false)
        AdminTextField(value = downloadUrl, onValueChange = { downloadUrl = it }, label = "رابط ملف التحميل (Download URL)")

        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = isPublished,
            onCheckedChange = { isPublished = it },
            colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
          )
          Text("نشر مباشرة في المتجر للجمهور", color = TextPrimary, fontSize = 13.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = isFeatured,
            onCheckedChange = { isFeatured = it },
            colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
          )
          Text("عرض في العناصر المميزة (Featured)", color = TextPrimary, fontSize = 13.sp)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank() || packageName.isBlank()) {
            Toast.makeText(context, "يرجى توفير اسم التطبيق واسم الحزمة (عبر رفع APK أو كتابتها)", Toast.LENGTH_SHORT).show()
            return@Button
          }

          val newItem = (item ?: StoreItem()).copy(
            name = name,
            packageName = packageName,
            developer = developer.ifEmpty { "مطور مستقل" },
            type = type,
            category = category.ifEmpty { "تطبيقات" },
            version = version,
            versionCode = versionCode,
            fileSize = fileSize,
            iconUrl = iconUrl,
            downloadUrl = downloadUrl,
            description = description.ifEmpty { "تطبيق رائع متوفر الآن للتحميل على متجر AppStore Plus." },
            minSdk = minSdk,
            targetSdk = targetSdk,
            cpuArchitecture = cpuArch,
            modInfo = modInfo,
            compatibility = minSdk.ifEmpty { "Android 7.0+" },
            isPublished = isPublished,
            isFeatured = isFeatured
          )
          onSave(newItem)
        },
        colors = ButtonDefaults.buttonColors(containerColor = ButtonInstallBlue)
      ) {
        Text("نشر في المتجر", color = Color.White)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("إلغاء", color = TextSecondary)
      }
    },
    containerColor = CardElevated
  )
}

@Composable
fun CategoryFormDialog(
  onDismiss: () -> Unit,
  onSave: (CategoryItem) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var iconName by remember { mutableStateOf("grid") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("إضافة تصنيف جديد", fontWeight = FontWeight.Bold, color = TextPrimary) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AdminTextField(value = name, onValueChange = { name = it }, label = "اسم التصنيف")
        AdminTextField(value = iconName, onValueChange = { iconName = it }, label = "رمز الأيقونة (grid, gamepad, play, chat...)")
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onSave(CategoryItem(name = name, iconName = iconName, colorHex = "#3B82F6"))
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ButtonInstallBlue)
      ) {
        Text("إضافة", color = Color.White)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("إلغاء", color = TextSecondary)
      }
    },
    containerColor = CardElevated
  )
}

@Composable
private fun AdminTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  singleLine: Boolean = true
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label, fontSize = 12.sp) },
    singleLine = singleLine,
    modifier = Modifier.fillMaxWidth(),
    colors = OutlinedTextFieldDefaults.colors(
      focusedTextColor = TextPrimary,
      unfocusedTextColor = TextPrimary,
      focusedBorderColor = PrimaryBlue,
      unfocusedBorderColor = CardBorder,
      focusedLabelColor = PrimaryBlue,
      unfocusedLabelColor = TextMuted
    )
  )
}
