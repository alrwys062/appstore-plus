package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ScreenDestination
import com.example.ui.StoreViewModel
import com.example.ui.components.StoreBottomBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.GamesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.UpdatesScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          AppStorePlusApp()
        }
      }
    }
  }
}

@Composable
fun AppStorePlusApp(viewModel: StoreViewModel = viewModel()) {
  val context = LocalContext.current
  val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
  val storeItems by viewModel.storeItems.collectAsStateWithLifecycle()
  val adminItems by viewModel.adminItems.collectAsStateWithLifecycle()
  val categories by viewModel.categories.collectAsStateWithLifecycle()
  val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
  val filteredGames by viewModel.filteredGames.collectAsStateWithLifecycle()
  val updateItems by viewModel.updateItems.collectAsStateWithLifecycle()
  val downloadStates by viewModel.downloadStates.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedCatFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
  val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

  LaunchedEffect(toastMessage) {
    toastMessage?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.clearToast()
    }
  }

  // Handle back button when not on Home or Splash
  if (currentScreen !is ScreenDestination.Home && currentScreen !is ScreenDestination.Splash) {
    BackHandler {
      viewModel.navigateTo(ScreenDestination.Home)
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(BackgroundDark),
    containerColor = BackgroundDark,
    bottomBar = {
      if (currentScreen !is ScreenDestination.Splash &&
        currentScreen !is ScreenDestination.Admin &&
        currentScreen !is ScreenDestination.Detail
      ) {
        StoreBottomBar(
          currentScreen = currentScreen,
          updatesCount = updateItems.size.coerceAtLeast(4),
          onNavigate = { destination ->
            viewModel.navigateTo(destination)
          }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (val screen = currentScreen) {
        is ScreenDestination.Splash -> {
          SplashScreen(
            onStartClick = { viewModel.navigateTo(ScreenDestination.Home) },
            onSkipClick = { viewModel.navigateTo(ScreenDestination.Home) }
          )
        }

        is ScreenDestination.Home -> {
          HomeScreen(
            storeItems = storeItems,
            searchQuery = searchQuery,
            onSearchChange = { viewModel.searchQuery.value = it },
            downloadStates = downloadStates,
            onItemClick = { item -> viewModel.navigateTo(ScreenDestination.Detail(item)) },
            onInstallClick = { item -> viewModel.startDownload(item) },
            onNavigate = { destination -> viewModel.navigateTo(destination) }
          )
        }

        is ScreenDestination.Apps -> {
          AppsScreen(
            apps = filteredApps,
            searchQuery = searchQuery,
            onSearchChange = { viewModel.searchQuery.value = it },
            selectedCategory = selectedCatFilter,
            onCategorySelect = { viewModel.selectedCategoryFilter.value = it },
            downloadStates = downloadStates,
            onItemClick = { item -> viewModel.navigateTo(ScreenDestination.Detail(item)) },
            onInstallClick = { item -> viewModel.startDownload(item) }
          )
        }

        is ScreenDestination.Games -> {
          GamesScreen(
            games = filteredGames,
            searchQuery = searchQuery,
            onSearchChange = { viewModel.searchQuery.value = it },
            downloadStates = downloadStates,
            onItemClick = { item -> viewModel.navigateTo(ScreenDestination.Detail(item)) },
            onInstallClick = { item -> viewModel.startDownload(item) }
          )
        }

        is ScreenDestination.Categories -> {
          CategoriesScreen(
            categories = categories,
            onCategoryClick = { category ->
              viewModel.selectedCategoryFilter.value = category.name
              if (category.type == "game") {
                viewModel.navigateTo(ScreenDestination.Games)
              } else {
                viewModel.navigateTo(ScreenDestination.Apps)
              }
            }
          )
        }

        is ScreenDestination.Updates -> {
          UpdatesScreen(
            updateItems = updateItems,
            downloadStates = downloadStates,
            onItemClick = { item -> viewModel.navigateTo(ScreenDestination.Detail(item)) },
            onUpdateClick = { item -> viewModel.startDownload(item) }
          )
        }

        is ScreenDestination.Profile -> {
          ProfileScreen(
            user = currentUser,
            onSignInClick = { viewModel.signInWithGoogle() },
            onSignOutClick = { viewModel.signOut() },
            onNavigate = { destination -> viewModel.navigateTo(destination) }
          )
        }

        is ScreenDestination.Detail -> {
          DetailScreen(
            item = screen.item,
            downloadProgress = downloadStates[screen.item.id],
            onBack = { viewModel.navigateTo(ScreenDestination.Home) },
            onInstallClick = { item -> viewModel.startDownload(item) }
          )
        }

        is ScreenDestination.Admin -> {
          AdminDashboardScreen(
            items = adminItems,
            categories = categories,
            onBack = { viewModel.navigateTo(ScreenDestination.Profile) },
            onSaveItem = { item, onDone -> viewModel.saveStoreItem(item, onDone) },
            onDeleteItem = { id -> viewModel.deleteStoreItem(id) },
            onTogglePublish = { id, current -> viewModel.togglePublish(id, current) },
            onToggleFeatured = { id, current -> viewModel.toggleFeatured(id, current) },
            onSaveCategory = { cat, onDone -> viewModel.saveCategory(cat, onDone) },
            onDeleteCategory = { id -> viewModel.deleteCategory(id) }
          )
        }
      }
    }
  }
}
