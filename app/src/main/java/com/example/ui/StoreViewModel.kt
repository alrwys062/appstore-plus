package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.data.DownloadManager
import com.example.data.StoreRepository
import com.example.model.CategoryItem
import com.example.model.DownloadProgress
import com.example.model.StoreItem
import com.example.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenDestination {
  object Splash : ScreenDestination()
  object Home : ScreenDestination()
  object Games : ScreenDestination()
  object Apps : ScreenDestination()
  object Updates : ScreenDestination()
  object Categories : ScreenDestination()
  object Profile : ScreenDestination()
  data class Detail(val item: StoreItem) : ScreenDestination()
  object Admin : ScreenDestination()
}

class StoreViewModel(application: Application) : AndroidViewModel(application) {

  private val storeRepo = StoreRepository(application)
  private val authRepo = AuthRepository(application)

  val currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Splash)
  val searchQuery = MutableStateFlow("")
  val selectedCategoryFilter = MutableStateFlow("الكل")

  val currentUser: StateFlow<UserAccount?> = authRepo.observeCurrentUser()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepo.getCurrentUser())

  val downloadStates: StateFlow<Map<String, DownloadProgress>> = DownloadManager.downloads

  val storeItems: StateFlow<List<StoreItem>> = storeRepo.getStoreItems()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val adminItems: StateFlow<List<StoreItem>> = storeRepo.getAllItemsForAdmin()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val categories: StateFlow<List<CategoryItem>> = storeRepo.getCategories()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  init {
    viewModelScope.launch {
      storeRepo.seedInitialDataIfEmpty()
    }
  }

  // Filtered Apps list based on search and category filter
  val filteredApps = combine(storeItems, searchQuery, selectedCategoryFilter) { items, query, cat ->
    items.filter { item ->
      item.type == "app" &&
          (cat == "الكل" || item.category == cat) &&
          (query.isBlank() || item.name.contains(query, ignoreCase = true) || item.developer.contains(query, ignoreCase = true))
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Filtered Games list
  val filteredGames = combine(storeItems, searchQuery) { items, query ->
    items.filter { item ->
      item.type == "game" &&
          (query.isBlank() || item.name.contains(query, ignoreCase = true) || item.developer.contains(query, ignoreCase = true))
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Updates list
  val updateItems = storeItems.combine(storeItems) { items, _ ->
    items.filter { it.isUpdateAvailable }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun navigateTo(destination: ScreenDestination) {
    currentScreen.value = destination
  }

  fun startDownload(item: StoreItem) {
    DownloadManager.startDownload(item)
    _toastMessage.value = "بدأ تحميل ${item.name}..."
  }

  fun cancelDownload(appId: String) {
    DownloadManager.cancelDownload(appId)
  }

  fun signInWithGoogle() {
    viewModelScope.launch {
      val result = authRepo.signInWithGoogle()
      result.onSuccess {
        _toastMessage.value = "تم تسجيل الدخول بنجاح!"
      }.onFailure { e ->
        _toastMessage.value = "فشل تسجيل الدخول: ${e.localizedMessage}"
      }
    }
  }

  fun signOut() {
    authRepo.signOut()
    _toastMessage.value = "تم تسجيل الخروج"
  }

  fun clearToast() {
    _toastMessage.value = null
  }

  // Admin Operations
  fun saveStoreItem(item: StoreItem, onDone: () -> Unit) {
    viewModelScope.launch {
      val success = storeRepo.saveStoreItem(item)
      if (success) {
        _toastMessage.value = "تم حفظ التطبيق/اللعبة بنجاح في المتجر"
        onDone()
      } else {
        _toastMessage.value = "حدث خطأ أثناء الحفظ"
      }
    }
  }

  fun deleteStoreItem(id: String) {
    viewModelScope.launch {
      val success = storeRepo.deleteStoreItem(id)
      if (success) {
        _toastMessage.value = "تم حذف العنصر بنجاح"
      } else {
        _toastMessage.value = "فشل حذف العنصر"
      }
    }
  }

  fun togglePublish(id: String, currentStatus: Boolean) {
    viewModelScope.launch {
      storeRepo.togglePublish(id, currentStatus)
    }
  }

  fun toggleFeatured(id: String, currentStatus: Boolean) {
    viewModelScope.launch {
      storeRepo.toggleFeatured(id, currentStatus)
    }
  }

  fun saveCategory(category: CategoryItem, onDone: () -> Unit) {
    viewModelScope.launch {
      val success = storeRepo.saveCategory(category)
      if (success) {
        _toastMessage.value = "تم حفظ التصنيف بنجاح"
        onDone()
      }
    }
  }

  fun deleteCategory(id: String) {
    viewModelScope.launch {
      storeRepo.deleteCategory(id)
    }
  }
}
