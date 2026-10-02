package com.aistudio.appstoreplus.qkrzv.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aistudio.appstoreplus.qkrzv.R
import com.aistudio.appstoreplus.qkrzv.model.DownloadProgress
import com.aistudio.appstoreplus.qkrzv.model.StoreItem
import com.aistudio.appstoreplus.qkrzv.ui.ScreenDestination
import com.aistudio.appstoreplus.qkrzv.ui.components.AppGridCard
import com.aistudio.appstoreplus.qkrzv.ui.components.InstallButton
import com.aistudio.appstoreplus.qkrzv.ui.components.SearchInputField
import com.aistudio.appstoreplus.qkrzv.ui.components.StoreTopBar
import com.aistudio.appstoreplus.qkrzv.ui.theme.BackgroundDark
import com.aistudio.appstoreplus.qkrzv.ui.theme.ButtonInstallBlue
import com.aistudio.appstoreplus.qkrzv.ui.theme.CardBorder
import com.aistudio.appstoreplus.qkrzv.ui.theme.CardDark
import com.aistudio.appstoreplus.qkrzv.ui.theme.CardElevated
import com.aistudio.appstoreplus.qkrzv.ui.theme.PrimaryBlue
import com.aistudio.appstoreplus.qkrzv.ui.theme.TextPrimary
import com.aistudio.appstoreplus.qkrzv.ui.theme.TextSecondary

@Composable
fun HomeScreen(
  storeItems: List<StoreItem>,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  downloadStates: Map<String, DownloadProgress>,
  onItemClick: (StoreItem) -> Unit,
  onInstallClick: (StoreItem) -> Unit,
  onNavigate: (ScreenDestination) -> Unit
) {
  val heroItem = storeItems.find { it.id == "gta-v" } ?: storeItems.firstOrNull { it.type == "game" }
  val mostDownloaded = storeItems.filter { it.type == "app" }.sortedByDescending { it.downloadCount }
  val featuredGames = storeItems.filter { it.type == "game" }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .verticalScroll(rememberScrollState())
      .padding(bottom = 90.dp)
  ) {
    // Top Bar
    StoreTopBar(
      onNotificationClick = { onNavigate(ScreenDestination.Updates) },
      onProfileClick = { onNavigate(ScreenDestination.Profile) },
      onMenuClick = { onNavigate(ScreenDestination.Profile) }
    )

    Spacer(modifier = Modifier.height(6.dp))

    // Search Input
    SearchInputField(
      query = searchQuery,
      onQueryChange = {
        onSearchChange(it)
        if (it.isNotBlank()) onNavigate(ScreenDestination.Apps)
      }
    )

    Spacer(modifier = Modifier.height(18.dp))

    // Hero Featured Banner (GTA V Banner matching image)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .height(190.dp)
        .clip(RoundedCornerShape(22.dp))
        .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
        .clickable {
          heroItem?.let { onItemClick(it) }
        }
        .testTag("hero_banner_card")
    ) {
      // Banner Image
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(R.drawable.hero_gta)
          .crossfade(true)
          .build(),
        contentDescription = "GTA V Featured",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      // Gradient shadow overlay
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Transparent,
                Color(0x99000000),
                Color(0xEE070A12)
              )
            )
          )
      )

      // Text & Action inside banner
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(18.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.Start
      ) {
        Text(
          text = "GTA V",
          fontSize = 26.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Color.White
        )
        Text(
          text = "الآن علي هاتفك",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFFE2E8F0)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
          onClick = {
            heroItem?.let { onInstallClick(it) }
          },
          colors = ButtonDefaults.buttonColors(containerColor = ButtonInstallBlue),
          shape = RoundedCornerShape(16.dp),
          contentPadding = PaddingValues(horizontal = 22.dp, vertical = 6.dp),
          modifier = Modifier.height(34.dp)
        ) {
          Text(
            text = "تثبيت",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 4 Action Buttons Row: [تحديثات] [تطبيقات] [ألعاب] [تصنيفات]
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      QuickActionButton(
        title = "تحديثات",
        icon = Icons.Default.SystemUpdate,
        bgColor = Color(0xFF2E192E),
        iconColor = Color(0xFFEC4899),
        onClick = { onNavigate(ScreenDestination.Updates) }
      )

      QuickActionButton(
        title = "تطبيقات",
        icon = Icons.Default.Apps,
        bgColor = Color(0xFF142442),
        iconColor = Color(0xFF3B82F6),
        onClick = { onNavigate(ScreenDestination.Apps) }
      )

      QuickActionButton(
        title = "ألعاب",
        icon = Icons.Default.SportsEsports,
        bgColor = Color(0xFF192548),
        iconColor = Color(0xFF6366F1),
        onClick = { onNavigate(ScreenDestination.Games) }
      )

      QuickActionButton(
        title = "تصنيفات",
        icon = Icons.Default.Download,
        bgColor = Color(0xFF0F3028),
        iconColor = Color(0xFF10B981),
        onClick = { onNavigate(ScreenDestination.Categories) }
      )
    }

    Spacer(modifier = Modifier.height(26.dp))

    // Section "الأكثر تحميلاً" (Most Downloaded)
    SectionHeader(
      title = "الأكثر تحميلاً",
      onMoreClick = { onNavigate(ScreenDestination.Apps) }
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Horizontal Carousel for Apps
    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(mostDownloaded) { item ->
        AppGridCard(
          item = item,
          downloadProgress = downloadStates[item.id],
          onClick = { onItemClick(item) },
          onInstallClick = { onInstallClick(item) }
        )
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Section "ألعاب مميزة" (Featured Games)
    SectionHeader(
      title = "ألعاب مميزة",
      onMoreClick = { onNavigate(ScreenDestination.Games) }
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Featured Games Carousel
    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(featuredGames) { game ->
        Box(
          modifier = Modifier
            .width(180.dp)
            .height(115.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .clickable { onItemClick(game) }
        ) {
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(game.bannerUrl.ifEmpty { R.drawable.screenshot_gta1 })
              .crossfade(true)
              .build(),
            contentDescription = game.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )

          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color.Transparent, Color(0xDD070A12))
                )
              )
          )

          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(10.dp),
            verticalArrangement = Arrangement.Bottom
          ) {
            Text(
              text = game.name,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1
            )
            Text(
              text = game.developer,
              fontSize = 11.sp,
              color = TextSecondary,
              maxLines = 1
            )
          }
        }
      }
    }
  }
}

@Composable
private fun QuickActionButton(
  title: String,
  icon: ImageVector,
  bgColor: Color,
  iconColor: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable { onClick() }
  ) {
    Box(
      modifier = Modifier
        .size(62.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(bgColor)
        .border(1.dp, iconColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = iconColor,
        modifier = Modifier.size(26.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = title,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      color = TextPrimary
    )
  }
}

@Composable
fun SectionHeader(
  title: String,
  onMoreClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      fontSize = 19.sp,
      fontWeight = FontWeight.Bold,
      color = TextPrimary
    )

    Text(
      text = "المزيد",
      fontSize = 13.sp,
      fontWeight = FontWeight.Medium,
      color = PrimaryBlue,
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .clickable { onMoreClick() }
        .padding(horizontal = 4.dp, vertical = 2.dp)
    )
  }
}
