package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.DownloadProgress
import com.example.model.StoreItem
import com.example.ui.components.AppListItem
import com.example.ui.components.InstallButton
import com.example.ui.components.SearchInputField
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GamesScreen(
  games: List<StoreItem>,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  downloadStates: Map<String, DownloadProgress>,
  onItemClick: (StoreItem) -> Unit,
  onInstallClick: (StoreItem) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
  ) {
    // Header
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
      Text(
        text = "الألعاب",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    // Search bar
    SearchInputField(
      query = searchQuery,
      onQueryChange = onSearchChange,
      placeholder = "ابحث عن ألعاب..."
    )

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      // Featured Game Big Cards
      item {
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          items(games) { game ->
            Box(
              modifier = Modifier
                .width(260.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .clickable { onItemClick(game) }
            ) {
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(game.bannerUrl.ifEmpty { R.drawable.hero_gta })
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
                      colors = listOf(Color.Transparent, Color(0x99070A12), Color(0xEE070A12))
                    )
                  )
              )

              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(14.dp),
                verticalArrangement = Arrangement.Bottom
              ) {
                Text(
                  text = game.name,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(vertical = 4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "${game.rating} • ${game.developer}",
                    fontSize = 12.sp,
                    color = TextSecondary
                  )
                }
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
          text = "جميع الألعاب المتاحة",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      items(games) { game ->
        AppListItem(
          item = game,
          downloadProgress = downloadStates[game.id],
          onClick = { onItemClick(game) },
          onInstallClick = { onInstallClick(game) }
        )
      }
    }
  }
}
