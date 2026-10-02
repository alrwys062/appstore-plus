package com.aistudio.appstoreplus.qkrzv.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.appstoreplus.qkrzv.model.CategoryItem
import com.aistudio.appstoreplus.qkrzv.ui.components.getCategoryIcon
import com.aistudio.appstoreplus.qkrzv.ui.theme.BackgroundDark
import com.aistudio.appstoreplus.qkrzv.ui.theme.CardBorder
import com.aistudio.appstoreplus.qkrzv.ui.theme.CardDark
import com.aistudio.appstoreplus.qkrzv.ui.theme.TextPrimary

@Composable
fun CategoriesScreen(
  categories: List<CategoryItem>,
  onCategoryClick: (CategoryItem) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
  ) {
    // Header Title
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 18.dp),
      contentAlignment = Alignment.CenterStart
    ) {
      Text(
        text = "التصنيفات",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    // 2-Column Grid matching reference image
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      items(categories) { cat ->
        val iconColor = try {
          Color(android.graphics.Color.parseColor(cat.colorHex))
        } catch (e: Exception) {
          Color(0xFF3B82F6)
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable { onCategoryClick(cat) }
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = getCategoryIcon(cat.iconName),
              contentDescription = cat.name,
              tint = iconColor,
              modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = cat.name,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextPrimary
            )
          }
        }
      }
    }
  }
}
