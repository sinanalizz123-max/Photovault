package com.photovault.auth

import android.content.Context
import com.photovault.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Direct Google Photos REST API client.
 * All calls are authenticated using the stored access token.
 *
 * TOKEN AUTO-REFRESH:
 * Before every call, checks if token is expired.
 * If expired → refreshes silently using refresh_token.
 * If refresh fails → throws AuthExpiredException → UI prompts re-login.
 *
 * API REFERENCE: https://developers.google.com/photos/library/reference/rest
 */
class PhotosApiClient(private val context: Context) {

    private val tokenManager = TokenManager(context)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS
            else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    companion object {
        private const val BASE_URL = "https://photoslibrary.googleapis.com/v1"
        private const val UPLOAD_URL = "https://photoslibrary.googleapis.com/v1/uploads"
        const val MAX_BATCH_SIZE = 50
    }

    // ─── STEP 1: Upload bytes → get uploadToken ────────────────────────────
    /**
     * Uploads raw file bytes to Google and returns an uploadToken.
     * The file is NOT added to the library yet — that's Step 2.
     *
     * For large videos, this can take a while.
     * Progress is tracked via okhttp interceptor in UploadWorker.
     */
    suspend fun uploadBytes(
        accountId: String,
        file: File,
        mimeType: String,
        fileName: String
    ): UploadBytesResult {
        val token = getValidToken(accountId) ?: return UploadBytesResult.AuthError

        val request = Request.Builder()
            .url(UPLOAD_URL)
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-type", mimeType)
            .addHeader("X-Goog-Upload-Protocol", "raw")
            .addHeader("X-Goog-Upload-File-Name", fileName)
            .post(file.readBytes().toRequestBody(mimeType.toMediaType()))
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            when (response.code) {
                200 -> UploadBytesResult.Success(response.body?.string() ?: "")
                401 -> UploadBytesResult.AuthError
                429 -> UploadBytesResult.QuotaExceeded
                else -> UploadBytesResult.ApiError("HTTP ${response.code}")
            }
        } catch (e: Exception) {
            UploadBytesResult.NetworkError(e.message ?: "Network error")
        }
    }

    // ─── STEP 2: Create media items in library ─────────────────────────────
    /**
     * Takes uploadTokens and creates actual media items in Google Photos.
     * Can batch up to 50 items per call.
     *
     * If albumId is provided, items are added to that album directly.
     */
    suspend fun createMediaItems(
        accountId: String,
        uploads: List<MediaUploadRequest>,
        albumId: String? = null
    ): CreateMediaResult {
        val token = getValidToken(accountId) ?: return CreateMediaResult.AuthError

        val newMediaItems = JSONArray()
        uploads.forEach { upload ->
            newMediaItems.put(JSONObject().apply {
                put("description", upload.description ?: "")
                put("simpleMediaItem", JSONObject().apply {
                    put("fileName", upload.fileName)
                    put("uploadToken", upload.uploadToken)
                })
            })
        }

        val body = JSONObject().apply {
            if (albumId != null) put("albumId", albumId)
            put("newMediaItems", newMediaItems)
        }

        val request = Request.Builder()
            .url("$BASE_URL/mediaItems:batchCreate")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            when (response.code) {
                200 -> {
                    val json = JSONObject(responseBody)
                    val results = json.getJSONArray("newMediaItemResults")
                    val mediaIds = mutableListOf<String>()
                    for (i in 0 until results.length()) {
                        val item = results.getJSONObject(i)
                        val mediaItem = item.optJSONObject("mediaItem")
                        if (mediaItem != null) {
                            mediaIds.add(mediaItem.getString("id"))
                        }
                    }
                    CreateMediaResult.Success(mediaIds)
                }
                401 -> CreateMediaResult.AuthError
                429 -> CreateMediaResult.QuotaExceeded
                else -> CreateMediaResult.ApiError("HTTP ${response.code}: $responseBody")
            }
        } catch (e: Exception) {
            CreateMediaResult.NetworkError(e.message ?: "Network error")
        }
    }

    // ─── ALBUMS ────────────────────────────────────────────────────────────
    /**
     * Create a new album in Google Photos.
     * Returns the album ID.
     */
    suspend fun createAlbum(accountId: String, albumTitle: String): AlbumResult {
        val token = getValidToken(accountId) ?: return AlbumResult.AuthError

        val body = JSONObject().apply {
            put("album", JSONObject().apply {
                put("title", albumTitle)
            })
        }

        val request = Request.Builder()
            .url("$BASE_URL/albums")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            when (response.code) {
                200 -> {
                    val json = JSONObject(responseBody)
                    AlbumResult.Success(
                        albumId = json.getString("id"),
                        albumTitle = json.getString("title")
                    )
                }
                401 -> AlbumResult.AuthError
                429 -> AlbumResult.QuotaExceeded
                else -> AlbumResult.ApiError("HTTP ${response.code}")
            }
        } catch (e: Exception) {
            AlbumResult.NetworkError(e.message ?: "Network error")
        }
    }

    /**
     * List only albums created by THIS app.
     * We can only see/modify albums we created (API restriction).
     */
    suspend fun listAppCreatedAlbums(accountId: String): List<PhotoAlbum> {
        val token = getValidToken(accountId) ?: return emptyList()

        val albums = mutableListOf<PhotoAlbum>()
        var pageToken: String? = null

        do {
            val urlBuilder = "$BASE_URL/albums?excludeNonAppCreatedData=true&pageSize=50"
                .let { if (pageToken != null) "$it&pageToken=$pageToken" else it }

            val request = Request.Builder()
                .url(urlBuilder)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) break

            val json = JSONObject(response.body?.string() ?: "{}")
            val albumsArray = json.optJSONArray("albums") ?: break

            for (i in 0 until albumsArray.length()) {
                val a = albumsArray.getJSONObject(i)
                albums.add(PhotoAlbum(
                    id = a.getString("id"),
                    title = a.getString("title"),
                    mediaItemsCount = a.optLong("mediaItemsCount", 0),
                    coverPhotoUrl = a.optString("coverPhotoBaseUrl")
                ))
            }
            pageToken = json.optString("nextPageToken").takeIf { it.isNotEmpty() }
        } while (pageToken != null)

        return albums
    }

    // ─── TOKEN HELPER ──────────────────────────────────────────────────────
    /**
     * Returns a valid access token, refreshing if expired.
     * Returns null if refresh fails (re-login needed).
     */
    private suspend fun getValidToken(accountId: String): String? {
        if (tokenManager.isTokenExpired(accountId)) {
            val refreshResult = tokenManager.refreshAccessToken(
                accountId = accountId,
                clientId = context.getString(com.photovault.R.string.google_web_client_id),
                clientSecret = context.getString(com.photovault.R.string.google_web_client_secret)
            )
            return when (refreshResult) {
                is TokenExchangeResult.Success -> refreshResult.accessToken
                else -> null
            }
        }
        return tokenManager.getAccessToken(accountId)
    }
}

// ─── Data classes ──────────────────────────────────────────────────────────
data class MediaUploadRequest(
    val uploadToken: String,
    val fileName: String,
    val description: String? = null
)

data class PhotoAlbum(
    val id: String,
    val title: String,
    val mediaItemsCount: Long,
    val coverPhotoUrl: String?
)

sealed class UploadBytesResult {
    data class Success(val uploadToken: String) : UploadBytesResult()
    object AuthError : UploadBytesResult()
    object QuotaExceeded : UploadBytesResult()
    data class NetworkError(val message: String) : UploadBytesResult()
    data class ApiError(val message: String) : UploadBytesResult()
}

sealed class CreateMediaResult {
    data class Success(val mediaItemIds: List<String>) : CreateMediaResult()
    object AuthError : CreateMediaResult()
    object QuotaExceeded : CreateMediaResult()
    data class NetworkError(val message: String) : CreateMediaResult()
    data class ApiError(val message: String) : CreateMediaResult()
}

sealed class AlbumResult {
    data class Success(val albumId: String, val albumTitle: String) : AlbumResult()
    object AuthError : AlbumResult()
    object QuotaExceeded : AlbumResult()
    data class NetworkError(val message: String) : AlbumResult()
    data class ApiError(val message: String) : AlbumResult()
}
