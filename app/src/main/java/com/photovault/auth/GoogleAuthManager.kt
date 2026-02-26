package com.photovault.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.photovault.R

/**
 * Manages Google OAuth 2.0 sign-in for multiple Gmail accounts.
 *
 * HOW TO GET YOUR CLIENT ID:
 * ─────────────────────────────────────────────────────────────
 * 1. Go to https://console.cloud.google.com/
 * 2. Create or select a project
 * 3. Enable "Photos Library API" under APIs & Services → Library
 * 4. Go to APIs & Services → Credentials
 * 5. Click "Create Credentials" → "OAuth 2.0 Client IDs"
 * 6. Application type: Android
 * 7. Package name: com.photovault
 * 8. SHA-1 fingerprint: run this in terminal →
 *    keytool -keystore ~/.android/debug.keystore -list -v -alias androiddebugkey -storepass android
 * 9. Copy the generated Client ID
 * 10. Paste it in res/values/strings.xml as google_oauth_client_id
 * ─────────────────────────────────────────────────────────────
 *
 * SCOPES REQUIRED:
 * - photoslibrary          → read/list your own photos
 * - photoslibrary.appendonly → create new media items (upload)
 * - photoslibrary.readonly.appcreateddata → read only what THIS app uploaded
 */
class GoogleAuthManager(private val context: Context) {

    // Scopes needed for Google Photos API
    companion object {
        val PHOTOS_READ_SCOPE = Scope("https://www.googleapis.com/auth/photoslibrary.readonly.appcreateddata")
        val PHOTOS_APPEND_SCOPE = Scope("https://www.googleapis.com/auth/photoslibrary.appendonly")
        val PHOTOS_ALBUM_SCOPE = Scope("https://www.googleapis.com/auth/photoslibrary")
        val EMAIL_SCOPE = Scope("email")
        val PROFILE_SCOPE = Scope("profile")
    }

    /**
     * Build a GoogleSignInClient configured for Photos API access.
     * Each account gets its own separate client instance.
     *
     * serverClientId = your Web Application OAuth Client ID from Google Cloud Console
     * (needed to request offline access / refresh token)
     */
    fun buildSignInClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(
                PHOTOS_APPEND_SCOPE,
                PHOTOS_READ_SCOPE,
                PHOTOS_ALBUM_SCOPE
            )
            // This is your CLIENT ID from Google Cloud Console
            // You MUST set this in res/values/strings.xml
            .requestServerAuthCode(context.getString(R.string.google_oauth_client_id), true)
            .requestIdToken(context.getString(R.string.google_oauth_client_id))
            .build()

        return GoogleSignIn.getClient(context, options)
    }

    /**
     * Get the sign-in Intent to launch via startActivityForResult / ActivityResultLauncher
     */
    fun getSignInIntent(): Intent {
        return buildSignInClient().signInIntent
    }

    /**
     * Parse the result from the Google Sign-In activity.
     * Returns AccountAuthResult with account info and auth code.
     */
    fun handleSignInResult(result: ActivityResult): AccountAuthResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            AccountAuthResult.Success(
                email = account.email ?: "",
                displayName = account.displayName ?: "",
                photoUrl = account.photoUrl?.toString(),
                idToken = account.idToken,
                serverAuthCode = account.serverAuthCode,
                googleAccount = account
            )
        } catch (e: ApiException) {
            AccountAuthResult.Failure(
                errorCode = e.statusCode,
                message = "Sign-in failed: ${e.message}"
            )
        }
    }

    /**
     * Check if an account is already signed in.
     */
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    /**
     * Sign out a specific account.
     * For multi-account, we re-launch sign-in flow to add another.
     */
    fun signOut(onComplete: () -> Unit) {
        buildSignInClient().signOut().addOnCompleteListener { onComplete() }
    }

    /**
     * Revoke access completely (removes from Google account permissions page too).
     */
    fun revokeAccess(onComplete: () -> Unit) {
        buildSignInClient().revokeAccess().addOnCompleteListener { onComplete() }
    }
}

sealed class AccountAuthResult {
    data class Success(
        val email: String,
        val displayName: String,
        val photoUrl: String?,
        val idToken: String?,
        val serverAuthCode: String?,
        val googleAccount: GoogleSignInAccount
    ) : AccountAuthResult()

    data class Failure(
        val errorCode: Int,
        val message: String
    ) : AccountAuthResult()
}
