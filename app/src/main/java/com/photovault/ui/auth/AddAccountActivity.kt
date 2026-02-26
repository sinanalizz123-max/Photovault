package com.photovault.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.photovault.R
import com.photovault.auth.AccountAuthResult
import com.photovault.auth.GoogleAuthManager
import com.photovault.auth.TokenExchangeResult
import com.photovault.auth.TokenManager
import com.photovault.database.AppDatabase
import com.photovault.database.entity.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Launches Google Sign-In to add a new Gmail account.
 *
 * SETUP — see AUTH_SETUP.md for full instructions:
 *   1. Go to https://console.cloud.google.com
 *   2. Enable "Photos Library API"
 *   3. Create Android + Web OAuth credentials
 *   4. Create res/values/secrets.xml (see secrets_template.xml)
 */
class AddAccountActivity : AppCompatActivity() {

    private lateinit var authManager: GoogleAuthManager
    private lateinit var tokenManager: TokenManager

    private lateinit var tvStatus: TextView
    private lateinit var btnSignIn: Button
    private lateinit var progressBar: ProgressBar

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> handleSignInResult(result) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(64, 64, 64, 64)
            setBackgroundColor(resources.getColor(android.R.color.background_dark, theme))
        }

        tvStatus = TextView(this).apply {
            text = "Connect a Gmail account\nto start uploading photos"
            textSize = 17f
            gravity = android.view.Gravity.CENTER
            setTextColor(resources.getColor(R.color.accent_blue, theme))
            setPadding(0, 0, 0, 48)
        }

        progressBar = ProgressBar(this).apply { visibility = View.GONE }

        btnSignIn = Button(this).apply {
            text = "Sign in with Google"
            textSize = 15f
            setBackgroundResource(R.drawable.bg_gradient_accent)
            setTextColor(resources.getColor(android.R.color.white, theme))
            setPadding(48, 24, 48, 24)
        }

        root.addView(tvStatus)
        root.addView(progressBar)
        root.addView(btnSignIn)
        setContentView(root)

        authManager = GoogleAuthManager(this)
        tokenManager = TokenManager(this)
        btnSignIn.setOnClickListener { startSignIn() }
    }

    private fun startSignIn() {
        setLoading(true)
        tvStatus.text = "Connecting to Google…"
        signInLauncher.launch(authManager.getSignInIntent())
    }

    private fun handleSignInResult(result: ActivityResult) {
        when (val authResult = authManager.handleSignInResult(result)) {
            is AccountAuthResult.Success -> exchangeTokens(authResult)
            is AccountAuthResult.Failure -> {
                setLoading(false)
                showError(authResult.message)
            }
        }
    }

    private fun exchangeTokens(authResult: AccountAuthResult.Success) {
        lifecycleScope.launch {
            val code = authResult.serverAuthCode
            if (code == null) {
                setLoading(false)
                showError("No auth code received.\nCheck your Client ID in secrets.xml")
                return@launch
            }

            tvStatus.text = "Exchanging tokens…"

            val tokenResult = withContext(Dispatchers.IO) {
                tokenManager.exchangeAuthCode(
                    serverAuthCode = code,
                    clientId = getString(R.string.google_web_client_id),
                    clientSecret = getString(R.string.google_web_client_secret)
                )
            }

            when (tokenResult) {
                is TokenExchangeResult.Success -> saveAccountToDb(authResult, tokenResult)
                is TokenExchangeResult.Failure -> {
                    setLoading(false)
                    showError("Token exchange failed:\n${tokenResult.message}")
                }
                TokenExchangeResult.NeedsReLogin -> {
                    setLoading(false)
                    showError("Authentication expired. Please try again.")
                }
            }
        }
    }

    private suspend fun saveAccountToDb(
        auth: AccountAuthResult.Success,
        token: TokenExchangeResult.Success
    ) {
        withContext(Dispatchers.IO) {
            // Store tokens securely
            tokenManager.saveTokens(
                accountId = auth.email,
                accessToken = token.accessToken,
                refreshToken = token.refreshToken,
                expiresInSeconds = token.expiresIn
            )

            // Save account to Room DB
            val account = Account().apply {
                email = auth.email
                displayName = auth.displayName
                profilePicUrl = auth.photoUrl
                addedAt = System.currentTimeMillis()
                isActive = true
            }
            AppDatabase.getInstance(this@AddAccountActivity).accountDao().insert(account)
        }

        tvStatus.text = "Connected: ${auth.email}"
        setLoading(false)
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("account_email", auth.email)
        })
        finish()
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnSignIn.isEnabled = !loading
        btnSignIn.alpha = if (loading) 0.5f else 1f
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        tvStatus.text = "Sign-in failed — tap to retry"
        tvStatus.setTextColor(resources.getColor(R.color.accent_red, theme))
    }
}
