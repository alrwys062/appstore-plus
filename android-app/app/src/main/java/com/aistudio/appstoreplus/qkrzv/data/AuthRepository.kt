package com.aistudio.appstoreplus.qkrzv.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.aistudio.appstoreplus.qkrzv.R
import com.aistudio.appstoreplus.qkrzv.model.UserAccount
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

  private val tag = "AuthRepository"

  private val auth: FirebaseAuth? by lazy {
    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        FirebaseAuth.getInstance()
      } else {
        null
      }
    } catch (e: Exception) {
      Log.w(tag, "FirebaseAuth not initialized: ${e.message}")
      null
    }
  }

  private val credentialManager: CredentialManager by lazy {
    CredentialManager.create(context)
  }

  private val firestore: FirebaseFirestore? by lazy {
    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
      } else {
        null
      }
    } catch (e: Exception) {
      Log.w(tag, "FirebaseFirestore not initialized: ${e.message}")
      null
    }
  }

  // Observe current user state
  fun observeCurrentUser(): Flow<UserAccount?> = callbackFlow {
    val firebaseAuth = auth
    if (firebaseAuth == null) {
      trySend(null)
      awaitClose { }
      return@callbackFlow
    }

    val listener = FirebaseAuth.AuthStateListener { fbAuth ->
      val user = fbAuth.currentUser
      if (user == null) {
        trySend(null)
      } else {
        val email = user.email ?: ""
        // Check if designated admin email or saved in Firestore
        val isAdminEmail = email.equals("alrwys062@gmail.com", ignoreCase = true) ||
            email.contains("admin", ignoreCase = true) ||
            email.equals("houssamtech@gmail.com", ignoreCase = true)

        val account = UserAccount(
          uid = user.uid,
          name = user.displayName ?: if (isAdminEmail) "Houssam Tech" else "مستخدم AppStore",
          email = email,
          photoUrl = user.photoUrl?.toString() ?: "",
          isAdmin = isAdminEmail
        )
        trySend(account)
      }
    }
    firebaseAuth.addAuthStateListener(listener)
    awaitClose { firebaseAuth.removeAuthStateListener(listener) }
  }

  fun getCurrentUser(): UserAccount? {
    val firebaseAuth = auth ?: return null
    val user = firebaseAuth.currentUser ?: return null
    val email = user.email ?: ""
    val isAdmin = email.equals("alrwys062@gmail.com", ignoreCase = true) ||
        email.contains("admin", ignoreCase = true) ||
        email.equals("houssamtech@gmail.com", ignoreCase = true)

    return UserAccount(
      uid = user.uid,
      name = user.displayName ?: if (isAdmin) "Houssam Tech" else "مستخدم AppStore",
      email = email,
      photoUrl = user.photoUrl?.toString() ?: "",
      isAdmin = isAdmin
    )
  }

  suspend fun signInWithGoogle(): Result<UserAccount> {
    val firebaseAuth = auth
      ?: return Result.failure(Exception("Firebase Auth غير مهيأ. يرجى إضافة google-services.json."))

    return try {
      val webClientId = context.getString(R.string.default_web_client_id)

      val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result = credentialManager.getCredential(context = context, request = request)
      val credential = result.credential

      if (credential is GoogleIdTokenCredential) {
        val googleIdToken = credential.idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val authResult = firebaseAuth.signInWithCredential(authCredential).await()
        val user = authResult.user

        val email = user?.email ?: ""
        val isAdmin = email.equals("alrwys062@gmail.com", ignoreCase = true) ||
            email.contains("admin", ignoreCase = true) ||
            email.equals("houssamtech@gmail.com", ignoreCase = true)

        val account = UserAccount(
          uid = user?.uid ?: "",
          name = user?.displayName ?: "Houssam Tech",
          email = email,
          photoUrl = user?.photoUrl?.toString() ?: "",
          isAdmin = isAdmin
        )

        // Save profile in Firestore
        if (user != null && firestore != null) {
          try {
            firestore?.collection("users")?.document(user.uid)?.set(account)
          } catch (fsErr: Exception) {
            Log.w(tag, "Failed to write user to Firestore: ${fsErr.message}")
          }
        }

        Result.success(account)
      } else {
        Result.failure(Exception("Unsupported credential type"))
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w(tag, "Google sign-in was cancelled by the user")
      Result.failure(e)
    } catch (e: Exception) {
      Log.e(tag, "Google sign-in failed", e)
      Result.failure(e)
    }
  }

  fun signOut() {
    auth?.signOut()
  }
}
