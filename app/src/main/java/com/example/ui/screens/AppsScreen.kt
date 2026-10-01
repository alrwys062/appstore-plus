package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadProgress
import com.example.model.StoreItem
import com.example.ui.components.AppListItem
import com.example.ui.components.SearchInputField
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ButtonInstallBlue
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardDark
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AppsScreen(
  apps: List<StoreItem>,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  selectedCategory: String,
  onCategorySelect: (String) -> Unit,
  downloadStates: Map<String, DownloadProgress>,
  onItemClick: (StoreItem) -> Unit,
  onInstallClick: (StoreItem) -> Unit
) {
  val categories = listOf("الكل", "أدوات", "ترفيه", "تعليم", "تواصل", "تصوير", "اجتماعي", "أعمال", "صحي")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
  ) {
    // Header Title
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      Text(
        text = "التطبيقات",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    // Search bar
    SearchInputField(
      query = searchQuery,
      onQueryChange = onSearchChange,
      placeholder = "ابحث عن تطبيقات..."
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Categories Horizontal Filter Chips
    LazyRow(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(categories) { cat ->
        val isSelected = cat == selectedCategory
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) ButtonInstallBlue else CardDark)
            .border(1.dp, if (isSelected) PrimaryBlue else CardBorder, RoundedCornerShape(20.dp))
            .clickable { onCategorySelect(cat) }
            .padding(horizontal = 18.dp, vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = cat,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextSecondary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Apps List
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      items(apps) { app ->
        AppListItem(
          item = app,
          downloadProgress = downloadStates[app.id],
          onClick = { onItemClick(app) },
          onInstallClick = { onInstallClick(app) }
        )
      }
      if (apps.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "لا توجد تطبيقات مطابقة للبحث",
              color = TextSecondary,
              fontSize = 15.sp
            )
          }
        }
      }
    }
  }
}
