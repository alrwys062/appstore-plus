package com.aistudio.appstoreplus.qkrzv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aistudio.appstoreplus.qkrzv.R
import com.aistudio.appstoreplus.qkrzv.ui.theme.PrimaryGradientEnd
import com.aistudio.appstoreplus.qkrzv.ui.theme.PrimaryGradientStart
import com.aistudio.appstoreplus.qkrzv.ui.theme.TextPrimary
import com.aistudio.appstoreplus.qkrzv.ui.theme.TextSecondary

@Composable
fun SplashScreen(
  onStartClick: () -> Unit,
  onSkipClick: () -> Unit
) {
  Box(modifier = Modifier.fillMaxSize()) {
    // Ambient Background Image
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current)
        .data(R.drawable.splash_bg)
        .crossfade(true)
        .build(),
      contentDescription = "AppStore Plus Onboarding",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )

    // Dark gradient overlay for readability
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color.Transparent,
              Color(0x80070A12),
              Color(0xF0070A12)
            ),
            startY = 300f
          )
        )
    )

    // Content
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 40.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Area: Glowing Logo & App Title
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 48.dp)
      ) {
        // App Logo
        Box(
          modifier = Modifier
            .size(92.dp)
            .clip(RoundedCornerShape(26.dp)),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
              .data(R.drawable.app_logo)
              .crossfade(true)
              .build(),
            contentDescription = "AppStore Plus Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "AppStore Plus",
          fontSize = 32.sp,
          fontWeight = FontWeight.ExtraBold,
          color = TextPrimary,
          letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "كل ما تحتاجه من تطبيقات وألعاب\nفي مكان واحد",
          fontSize = 17.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFFE2E8F0),
          textAlign = TextAlign.Center,
          lineHeight = 26.sp
        )
      }

      // Bottom Area: "ابدأ الآن" & "تخطي"
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Button(
          onClick = onStartClick,
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("splash_start_button"),
          colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
          shape = RoundedCornerShape(28.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.horizontalGradient(
                  colors = listOf(PrimaryGradientStart, PrimaryGradientEnd)
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "ابدأ الآن",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "تخطي",
          fontSize = 15.sp,
          fontWeight = FontWeight.Medium,
          color = TextSecondary,
          modifier = Modifier
            .clickable { onSkipClick() }
            .padding(8.dp)
            .testTag("splash_skip_button")
        )

        Spacer(modifier = Modifier.height(10.dp))
      }
    }
  }
}
