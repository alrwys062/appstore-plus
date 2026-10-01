package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.model.UserAccount
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

  private val tag = "AuthRepository"
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()
  private val credentialManager: CredentialManager = CredentialManager.create(context)

  private val firestore: FirebaseFirestore by lazy {
    val dbId = context.getString(R.string.firestore_database_id)
    FirebaseFirestore.getInstance(dbId)
  }

  // Observe current user state
  fun observeCurrentUser(): Flow<UserAccount?> = callbackFlow {
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
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
  }

  fun getCurrentUser(): UserAccount? {
    val user = auth.currentUser ?: return null
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
        val authResult = auth.signInWithCredential(authCredential).await()
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
        if (user != null) {
          firestore.collection("users").document(user.uid).set(account)
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
    auth.signOut()
  }
}
