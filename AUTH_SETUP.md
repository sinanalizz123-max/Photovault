# PhotoVault — Google OAuth Setup Guide

## One-Time Setup (Required Before Building)

### Step 1 — Google Cloud Console

1. Go to **https://console.cloud.google.com/**
2. Click **"New Project"** → Name it `PhotoVault`
3. Go to **APIs & Services → Library**
4. Search **"Photos Library API"** → Click **Enable**

### Step 2 — OAuth Consent Screen

1. **APIs & Services → OAuth Consent Screen**
2. User Type: **External**
3. Fill in App Name: `PhotoVault`, your email
4. **Add Scopes:**
   - `https://www.googleapis.com/auth/photoslibrary`
   - `https://www.googleapis.com/auth/photoslibrary.appendonly`
   - `https://www.googleapis.com/auth/photoslibrary.readonly.appcreateddata`
5. **Add Test Users** → add your Gmail accounts (while in Testing mode)
6. Save

### Step 3 — Android Credential (for sign-in)

1. **APIs & Services → Credentials → Create Credentials → OAuth 2.0 Client ID**
2. Application type: **Android**
3. Package name: `com.photovault`
4. SHA-1 fingerprint (debug):
   ```
   keytool -keystore ~/.android/debug.keystore -list -v -alias androiddebugkey -storepass android
   ```
   Copy the `SHA1:` line
5. Click **Create** → Copy the **Client ID**

### Step 4 — Web Credential (for token exchange)

1. **Create Credentials → OAuth 2.0 Client ID**
2. Application type: **Web Application**
3. Name: `PhotoVault Backend`
4. Click **Create** → Copy both **Client ID** and **Client Secret**

### Step 5 — Create secrets.xml

Create this file: `app/src/main/res/values/secrets.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_oauth_client_id">YOUR_ANDROID_CLIENT_ID.apps.googleusercontent.com</string>
    <string name="google_web_client_id">YOUR_WEB_CLIENT_ID.apps.googleusercontent.com</string>
    <string name="google_web_client_secret">YOUR_WEB_CLIENT_SECRET</string>
</resources>
```

> ⚠️ This file is in `.gitignore` — never commit it!

### Step 6 — Verify Build Config

In `app/build.gradle`, ensure:
```groovy
compileSdk 34
minSdk 26
```

---

## How the OAuth Flow Works (No Backend Server Needed)

```
User taps "Add Account"
        ↓
GoogleSignIn launches → user selects Gmail account
        ↓
Google returns serverAuthCode (one-time code)
        ↓
App exchanges serverAuthCode → access_token + refresh_token
(using Web Client ID + Secret, directly from the device)
        ↓
Tokens stored in EncryptedSharedPreferences (AES-256)
        ↓
Before each API call:
  - Check if access_token expired (1 hour)
  - If yes → use refresh_token to silently get new access_token
  - If refresh fails → prompt re-login
        ↓
API calls use Bearer access_token in Authorization header
```

## Multiple Accounts

Each account has its own:
- Access token (keyed by email)
- Refresh token (keyed by email)
- Quota counter (in Room DB)
- Upload queue entries

Tokens never cross between accounts.

## Quota Limits

| Limit | Value |
|-------|-------|
| Requests per day | 10,000 per project |
| Requests per user per day | ~9,000 (safe limit) |
| Batch create items | 50 per call |

If quota hit (HTTP 429):
- That account's uploads pause
- Other accounts continue
- Auto-resume after midnight Pacific Time
