package com.aistudio.appstoreplus.qkrzv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.aistudio.appstoreplus.qkrzv.R
import com.aistudio.appstoreplus.qkrzv.model.UserAccount
import com.aistudio.appstoreplus.qkrzv.ui.ScreenDestination
import com.aistudio.appstoreplus.qkrzv.ui.theme.BackgroundDark
import com.aistudio.appstoreplus.qkrzv.ui.theme.ButtonInstallBlue
import com.aistudio.appstoreplus.qkrzv.ui.theme.CardBorder
import com.aistudio.appstoreplus.qkrzv.ui.theme.CardDark
import com.aistudio.appstoreplus.qkrzv.ui.theme.DarkLogoutBg
import com.aistudio.appstoreplus.qkrzv.ui.theme.DarkLogoutBorder
import com.aistudio.appstoreplus.qkrzv.ui.theme.DarkLogoutText
import com.aistudio.appstoreplus.qkrzv.ui.theme.PrimaryBlue
import com.aistudio.appstoreplus.qkrzv.ui.theme.TextMuted
import com.aistudio.appstoreplus.qkrzv.ui.theme.TextPrimary
import com.aistudio.appstoreplus.qkrzv.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
  user: UserAccount?,
  onSignInClick: () -> Unit,
  onSignOutClick: () -> Unit,
  onNavigate: (ScreenDestination) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 16.dp)
      .padding(bottom = 90.dp)
  ) {
    // Header Title
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
    ) {
      Text(
        text = "حسابي",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
    }

    // Profile Card matching reference design
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(CardDark)
        .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
        .padding(18.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Circular Anime Avatar
        Box(
          modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .border(2.dp, PrimaryBlue, CircleShape)
        ) {
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(user?.photoUrl?.ifEmpty { null } ?: R.drawable.user_avatar)
              .crossfade(true)
              .build(),
            contentDescription = "الصورة الشخصية",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
          Text(
            text = user?.name ?: "Houssam Tech",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = user?.email?.ifEmpty { "houssamtech@gmail.com" } ?: "houssamtech@gmail.com",
            fontSize = 13.sp,
            color = TextSecondary
          )
          if (user?.isAdmin == true) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "مشرف النظام (Admin)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF38BDF8)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(22.dp))

    // Grouped Menu List
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(CardDark)
        .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
    ) {
      MenuItemRow(
        title = "الملف الشخصي",
        icon = Icons.Default.Person,
        onClick = {}
      )
      MenuDivider()

      MenuItemRow(
        title = "المفضلة",
        icon = Icons.Default.Bookmark,
        onClick = { onNavigate(ScreenDestination.Apps) }
      )
      MenuDivider()

      MenuItemRow(
        title = "الإعدادات",
        icon = Icons.Default.Settings,
        onClick = {}
      )
      MenuDivider()

      MenuItemRow(
        title = "المساعدة والدعم",
        icon = Icons.AutoMirrored.Filled.Help,
        onClick = {}
      )
      MenuDivider()

      MenuItemRow(
        title = "حول التطبيق",
        icon = Icons.Default.Info,
        onClick = {}
      )

      // Admin Dashboard Option
      MenuDivider()
      MenuItemRow(
        title = "لوحة تحكم المشرف (Admin Dashboard)",
        icon = Icons.Default.AdminPanelSettings,
        iconTint = Color(0xFF38BDF8),
        onClick = { onNavigate(ScreenDestination.Admin) }
      )
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Sign In / Logout Button
    if (user != null) {
      Button(
        onClick = onSignOutClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("logout_button"),
        colors = ButtonDefaults.buttonColors(containerColor = DarkLogoutBg),
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkLogoutBorder)
      ) {
        Text(
          text = "تسجيل الخروج",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = DarkLogoutText
        )
      }
    } else {
      Button(
        onClick = onSignInClick,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("google_signin_button"),
        colors = ButtonDefaults.buttonColors(containerColor = ButtonInstallBlue),
        shape = RoundedCornerShape(26.dp)
      ) {
        Text(
          text = "تسجيل الدخول عبر Google",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}

@Composable
private fun MenuItemRow(
  title: String,
  icon: ImageVector,
  iconTint: Color = TextPrimary,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 18.dp, vertical = 15.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = iconTint,
        modifier = Modifier.size(22.dp)
      )
      Spacer(modifier = Modifier.width(14.dp))
      Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
      )
    }

    Icon(
      imageVector = Icons.Default.ChevronLeft,
      contentDescription = null,
      tint = TextMuted,
      modifier = Modifier.size(18.dp)
    )
  }
}

@Composable
private fun MenuDivider() {
  Divider(
    color = CardBorder.copy(alpha = 0.6f),
    thickness = 0.8.dp,
    modifier = Modifier.padding(horizontal = 14.dp)
  )
}
