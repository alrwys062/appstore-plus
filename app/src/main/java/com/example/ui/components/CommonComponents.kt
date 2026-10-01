package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.DownloadProgress
import com.example.model.DownloadStatus
import com.example.model.StoreItem
import com.example.ui.ScreenDestination
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ButtonInstallBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevated
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StoreTopBar(
  onNotificationClick: () -> Unit = {},
  onProfileClick: () -> Unit = {},
  onMenuClick: () -> Unit = {}
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Notification Icon on the left
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(CardDark)
        .clickable { onNotificationClick() },
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.Notifications,
        contentDescription = "التنبيهات",
        tint = TextPrimary,
        modifier = Modifier.size(20.dp)
      )
    }

    // Right Side: Options & Avatar
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(CardDark)
          .clickable { onMenuClick() },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.MoreHoriz,
          contentDescription = "خيارات إضافية",
          tint = TextPrimary,
          modifier = Modifier.size(20.dp)
        )
      }

      // User Profile Avatar
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .border(1.5.dp, PrimaryBlue, CircleShape)
          .clickable { onProfileClick() }
          .testTag("profile_avatar_button")
      ) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(R.drawable.user_avatar)
            .crossfade(true)
            .build(),
          contentDescription = "الصورة الشخصية",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

@Composable
fun SearchInputField(
  query: String,
  onQueryChange: (String) -> Unit,
  placeholder: String = "ابحث عن تطبيقات أو ألعاب...",
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
  ) {
    OutlinedTextField(
      value = query,
      onValueChange = onQueryChange,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("store_search_input"),
      placeholder = {
        Text(
          text = placeholder,
          color = TextMuted,
          fontSize = 14.sp
        )
      },
      trailingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "بحث",
          tint = TextSecondary,
          modifier = Modifier.size(22.dp)
        )
      },
      shape = RoundedCornerShape(26.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = CardDark,
        unfocusedContainerColor = CardDark,
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = CardBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
      ),
      singleLine = true
    )
  }
}

@Composable
fun InstallButton(
  item: StoreItem,
  downloadProgress: DownloadProgress?,
  onInstallClick: () -> Unit,
  modifier: Modifier = Modifier,
  isSmall: Boolean = true
) {
  val isDownloading = downloadProgress?.status == DownloadStatus.DOWNLOADING
  val isCompleted = downloadProgress?.status == DownloadStatus.COMPLETED

  val animatedProgress by animateFloatAsState(
    targetValue = downloadProgress?.progress ?: 0f,
    label = "progress"
  )

  Button(
    onClick = onInstallClick,
    modifier = modifier
      .testTag("install_button_${item.id}"),
    colors = ButtonDefaults.buttonColors(
      containerColor = if (isCompleted) Color(0xFF10B981) else ButtonInstallBlue
    ),
    shape = RoundedCornerShape(if (isSmall) 18.dp else 24.dp),
    contentPadding = PaddingValues(horizontal = if (isSmall) 14.dp else 24.dp, vertical = if (isSmall) 4.dp else 12.dp)
  ) {
    if (isDownloading) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "${(animatedProgress * 100).toInt()}%",
          fontSize = if (isSmall) 11.sp else 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
          progress = { animatedProgress },
          modifier = Modifier
            .width(if (isSmall) 44.dp else 80.dp)
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp)),
          color = Color.White,
          trackColor = Color.White.copy(alpha = 0.3f)
        )
      }
    } else if (isCompleted) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "فتح",
          fontSize = if (isSmall) 12.sp else 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    } else {
      Text(
        text = if (item.isUpdateAvailable) "تحديث" else "تثبيت",
        fontSize = if (isSmall) 12.sp else 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }
  }
}

// App Card for "الأكثر تحميلاً" (Most Downloaded) horizontal list
@Composable
fun AppGridCard(
  item: StoreItem,
  downloadProgress: DownloadProgress?,
  onClick: () -> Unit,
  onInstallClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .width(88.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .padding(4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // App Icon with smooth rounded border
    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(CardDark)
        .border(1.dp, CardBorder, RoundedCornerShape(18.dp)),
      contentAlignment = Alignment.Center
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

    Spacer(modifier = Modifier.height(6.dp))

    // App Name
    Text(
      text = item.name,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      color = TextPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center
    )

    // Rating / Downloads
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
      modifier = Modifier.padding(top = 2.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Star,
        contentDescription = null,
        tint = Color(0xFFF59E0B),
        modifier = Modifier.size(11.dp)
      )
      Spacer(modifier = Modifier.width(2.dp))
      Text(
        text = item.downloads,
        fontSize = 11.sp,
        color = TextSecondary
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Install Button
    InstallButton(
      item = item,
      downloadProgress = downloadProgress,
      onInstallClick = onInstallClick,
      modifier = Modifier.fillMaxWidth().height(30.dp),
      isSmall = true
    )
  }
}

// App Row Item for Apps list and Updates list
@Composable
fun AppListItem(
  item: StoreItem,
  downloadProgress: DownloadProgress?,
  onClick: () -> Unit,
  onInstallClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .padding(vertical = 10.dp, horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // App Icon
    Box(
      modifier = Modifier
        .size(56.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(CardDark)
        .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
      contentAlignment = Alignment.Center
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

    Spacer(modifier = Modifier.width(14.dp))

    // Name & Info
    Column(
      modifier = Modifier.weight(1f)
    ) {
      Text(
        text = item.name,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = item.developer,
        fontSize = 12.sp,
        color = TextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(4.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (item.isUpdateAvailable) {
          Text(
            text = item.version,
            fontSize = 11.sp,
            color = Color(0xFF38BDF8),
            fontWeight = FontWeight.Medium
          )
        } else {
          Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = item.downloads,
            fontSize = 11.sp,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• ${item.fileSize}",
            fontSize = 11.sp,
            color = TextMuted
          )
        }
      }
    }

    Spacer(modifier = Modifier.width(10.dp))

    // Install / Update Button
    InstallButton(
      item = item,
      downloadProgress = downloadProgress,
      onInstallClick = onInstallClick,
      modifier = Modifier.height(34.dp),
      isSmall = true
    )
  }
}

// 5-Tab Bottom Navigation Bar matching reference design
@Composable
fun StoreBottomBar(
  currentScreen: ScreenDestination,
  updatesCount: Int = 4,
  onNavigate: (ScreenDestination) -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(BackgroundDark)
      .border(0.5.dp, CardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
      .padding(vertical = 6.dp, horizontal = 12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      BottomNavItem(
        title = "الرئيسية",
        icon = Icons.Default.Home,
        isSelected = currentScreen is ScreenDestination.Home,
        onClick = { onNavigate(ScreenDestination.Home) }
      )

      BottomNavItem(
        title = "الألعاب",
        icon = Icons.Default.SportsEsports,
        isSelected = currentScreen is ScreenDestination.Games,
        onClick = { onNavigate(ScreenDestination.Games) }
      )

      BottomNavItem(
        title = "التطبيقات",
        icon = Icons.Default.Apps,
        isSelected = currentScreen is ScreenDestination.Apps,
        onClick = { onNavigate(ScreenDestination.Apps) }
      )

      BottomNavItem(
        title = "التحديثات",
        icon = Icons.Default.SystemUpdate,
        isSelected = currentScreen is ScreenDestination.Updates,
        badgeCount = updatesCount,
        onClick = { onNavigate(ScreenDestination.Updates) }
      )

      BottomNavItem(
        title = "المزيد",
        icon = Icons.Default.Menu,
        isSelected = currentScreen is ScreenDestination.Profile || currentScreen is ScreenDestination.Admin,
        onClick = { onNavigate(ScreenDestination.Profile) }
      )
    }
  }
}

@Composable
private fun BottomNavItem(
  title: String,
  icon: ImageVector,
  isSelected: Boolean,
  badgeCount: Int = 0,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 6.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(contentAlignment = Alignment.TopEnd) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = if (isSelected) PrimaryBlue else TextMuted,
        modifier = Modifier.size(24.dp)
      )
      if (badgeCount > 0) {
        Box(
          modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(PrimaryBlue),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = badgeCount.toString(),
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = title,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) PrimaryBlue else TextMuted
    )
  }
}

fun getCategoryIcon(iconName: String): ImageVector {
  return when (iconName) {
    "gamepad" -> Icons.Default.SportsEsports
    "grid" -> Icons.Default.Apps
    "play" -> Icons.Default.PlayArrow
    "wrench" -> Icons.Default.Build
    "chat" -> Icons.Default.Chat
    "camera" -> Icons.Default.CameraAlt
    "people" -> Icons.Default.People
    "school" -> Icons.Default.School
    "heart" -> Icons.Default.Favorite
    "business" -> Icons.Default.Business
    else -> Icons.Default.Apps
  }
}
