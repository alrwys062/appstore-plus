package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DownloadProgress
import com.example.model.StoreItem
import com.example.ui.components.AppListItem
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun UpdatesScreen(
  updateItems: List<StoreItem>,
  downloadStates: Map<String, DownloadProgress>,
  onItemClick: (StoreItem) -> Unit,
  onUpdateClick: (StoreItem) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
  ) {
    // Header Section
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
      Text(
        text = "التحديثات",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "التطبيقات والألعاب التي تحتاج إلى تحديث",
        fontSize = 13.sp,
        color = TextSecondary
      )
    }

    // List of Updates
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      items(updateItems) { item ->
        AppListItem(
          item = item,
          downloadProgress = downloadStates[item.id],
          onClick = { onItemClick(item) },
          onInstallClick = { onUpdateClick(item) }
        )
      }

      if (updateItems.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp)
          ) {
            Text(
              text = "جميع تطبيقاتك محدثة إلى آخر إصدار!",
              color = TextSecondary,
              fontSize = 15.sp
            )
          }
        }
      }
    }
  }
}
