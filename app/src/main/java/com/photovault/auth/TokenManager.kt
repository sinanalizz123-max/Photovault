package com.photovault.auth

import android.content.Context
import android.content.SharedPreferences
import com.photovault.util.PrefsManager
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Manages OAuth access tokens and refresh tokens securely.
 *
 * STORAGE: Uses EncryptedSharedPreferences (AES256-GCM).
 * Tokens are NEVER logged or stored in plain text.
 *
 * TOKEN LIFECYCLE:
 * ─────────────────────────────────────────────────────────────
 * 1. User signs in → we get a serverAuthCode
 * 2. Exchange serverAuthCode → access_token + refresh_token
 *    (This exchange uses your CLIENT ID + CLIENT SECRET)
 *    NOTE: For Android, use your WEB client credentials for this,
 *          NOT the Android client ID (Android client has no secret)
 * 3. access_token expires after 1 hour
 * 4. Use refresh_token to get a new access_token silently
 * 5. Store everything encrypted locally
 * ─────────────────────────────────────────────────────────────
 */
class TokenManager(context: Context) {

    private val securePrefs: SharedPreferences = PrefsManager.getSecure(context)

    companion object {
        private const val TOKEN_EXCHANGE_URL = "https://oauth2.googleapis.com/token"
        const val GOOGLE_TOKEN_REFRESH_URL = "https://oauth2.googleapis.com/token"

        // Keys for EncryptedSharedPreferences
        private fun accessTokenKey(accountId: String) = "access_token_$accountId"
        private fun refreshTokenKey(accountId: String) = "refresh_token_$accountId"
        private fun expiryKey(accountId: String) = "token_expiry_$accountId"
    }

    fun saveTokens(accountId: String, accessToken: String, refreshToken: String, expiresInSeconds: Long) {
        val expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000) - 60_000 // 1 min buffer
        securePrefs.edit().apply {
            putString(accessTokenKey(accountId), accessToken)
            putString(refreshTokenKey(accountId), refreshToken)
            putLong(expiryKey(accountId), expiryTime)
            apply()
        }
    }

    fun getAccessToken(accountId: String): String? {
        return securePrefs.getString(accessTokenKey(accountId), null)
    }

    fun getRefreshToken(accountId: String): String? {
        return securePrefs.getString(refreshTokenKey(accountId), null)
    }

    fun isTokenExpired(accountId: String): Boolean {
        val expiry = securePrefs.getLong(expiryKey(accountId), 0L)
        return System.currentTimeMillis() >= expiry
    }

    fun clearTokens(accountId: String) {
        securePrefs.edit().apply {
            remove(accessTokenKey(accountId))
            remove(refreshTokenKey(accountId))
            remove(expiryKey(accountId))
            apply()
        }
    }

    /**
     * Exchange serverAuthCode for access_token + refresh_token.
     *
     * This is called ONCE after first sign-in.
     * Subsequent token refreshes use refresh_token only.
     *
     * IMPORTANT: clientId and clientSecret here are your WEB application
     * credentials from Google Cloud Console (not Android credentials).
     * The clientSecret for WEB type is safe to embed in Android because:
     * - It's used for server-side token exchange
     * - Android sign-in itself uses the Android credential (no secret needed)
     */
    suspend fun exchangeAuthCode(
        serverAuthCode: String,
        clientId: String,      // your WEB client ID from Google Cloud Console
        clientSecret: String,  // your WEB client secret (set in BuildConfig)
        redirectUri: String = ""
    ): TokenExchangeResult {
        return try {
            val url = URL(TOKEN_EXCHANGE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.doOutput = true

            val body = "code=$serverAuthCode" +
                    "&client_id=$clientId" +
                    "&client_secret=$clientSecret" +
                    "&redirect_uri=$redirectUri" +
                    "&grant_type=authorization_code"

            OutputStreamWriter(connection.outputStream).use { it.write(body) }

            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)

            if (responseCode == 200) {
                TokenExchangeResult.Success(
                    accessToken = json.getString("access_token"),
                    refreshToken = json.optString("refresh_token"),
                    expiresIn = json.getLong("expires_in")
                )
            } else {
                TokenExchangeResult.Failure("HTTP $responseCode: ${json.optString("error_description")}")
            }
        } catch (e: Exception) {
            TokenExchangeResult.Failure(e.message ?: "Unknown error")
        }
    }

    /**
     * Refresh an expired access token using the stored refresh token.
     * Called automatically before every API call if token is expired.
     */
    suspend fun refreshAccessToken(
        accountId: String,
        clientId: String,
        clientSecret: String
    ): TokenExchangeResult {
        val refreshToken = getRefreshToken(accountId)
            ?: return TokenExchangeResult.Failure("No refresh token stored for account $accountId")

        return try {
            val url = URL(GOOGLE_TOKEN_REFRESH_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.doOutput = true

            val body = "refresh_token=$refreshToken" +
                    "&client_id=$clientId" +
                    "&client_secret=$clientSecret" +
                    "&grant_type=refresh_token"

            OutputStreamWriter(connection.outputStream).use { it.write(body) }

            val responseCode = connection.responseCode
            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)

            if (responseCode == 200) {
                val newToken = json.getString("access_token")
                val expiresIn = json.getLong("expires_in")
                // Save new access token (refresh token doesn't change)
                saveTokens(accountId, newToken, refreshToken, expiresIn)
                TokenExchangeResult.Success(
                    accessToken = newToken,
                    refreshToken = refreshToken,
                    expiresIn = expiresIn
                )
            } else {
                if (responseCode == 401) {
                    // Refresh token invalid — user must re-sign in
                    clearTokens(accountId)
                    TokenExchangeResult.NeedsReLogin
                } else {
                    TokenExchangeResult.Failure("Token refresh failed: HTTP $responseCode")
                }
            }
        } catch (e: Exception) {
            TokenExchangeResult.Failure(e.message ?: "Unknown error")
        }
    }
}

sealed class TokenExchangeResult {
    data class Success(val accessToken: String, val refreshToken: String, val expiresIn: Long) : TokenExchangeResult()
    data class Failure(val message: String) : TokenExchangeResult()
    object NeedsReLogin : TokenExchangeResult()
}
