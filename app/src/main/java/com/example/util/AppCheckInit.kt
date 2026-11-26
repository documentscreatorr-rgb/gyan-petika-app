package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"

// App Check contract keys defined by the Firebase SDK and runtime harness.
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"
private const val APPCHECK_META_PREFS = "gyan_petika_appcheck_meta"
private const val KEY_HAS_INJECTED_DEBUG_TOKEN = "has_injected_debug_token"

fun configureAppCheck(context: Context, intent: Intent?) {
  try {
    // 1. Ensure FirebaseApp is initialized
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    if (BuildConfig.DEBUG) {
      // 2. Debug: Read registered debug token from launch Intent extra when running in test/emulator harness
      val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
      val metaPrefs = context.getSharedPreferences(APPCHECK_META_PREFS, Context.MODE_PRIVATE)

      if (!debugToken.isNullOrEmpty()) {
        val persistenceKey = FirebaseApp.getInstance().persistenceKey
        val prefs =
          context.getSharedPreferences(
            "$DEBUG_STORE_PREFIX$persistenceKey",
            Context.MODE_PRIVATE,
          )
        // CRITICAL: MUST use synchronous commit() instead of apply() so the secret is written
        // before DebugAppCheckProviderFactory reads it.
        prefs.edit().putString(DEBUG_SECRET_KEY, debugToken).commit()
        metaPrefs.edit().putBoolean(KEY_HAS_INJECTED_DEBUG_TOKEN, true).commit()
        Log.d(TAG, "App Check debug secret configured successfully.")
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
          DebugAppCheckProviderFactory.getInstance()
        )
        Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        return
      }

      val hasInjectedDebugToken = metaPrefs.getBoolean(KEY_HAS_INJECTED_DEBUG_TOKEN, false)
      if (hasInjectedDebugToken) {
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
          DebugAppCheckProviderFactory.getInstance()
        )
        Log.d(TAG, "DebugAppCheckProviderFactory installed using persisted debug token.")
        return
      }

      // On sideloaded debug APKs on physical devices (no harness debug token injected),
      // use the provisioned reCAPTCHA Enterprise site key so App Check attestation succeeds.
      if (installRecaptchaProvider(context)) {
        return
      }

      Log.w(TAG, "No debug token or reCAPTCHA site key found; falling back to DebugAppCheckProviderFactory.")
      FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
        DebugAppCheckProviderFactory.getInstance()
      )
    } else {
      // 3. Release: Look up recaptcha_site_key safely without throwing Resources.NotFoundException
      installRecaptchaProvider(context)
    }
  } catch (e: Exception) {
    Log.e(TAG, "Failed to initialize Firebase App Check", e)
  }
}

private fun installRecaptchaProvider(context: Context): Boolean {
  val resId =
    context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
  if (resId == 0) {
    Log.e(TAG, "recaptcha_site_key string resource not found in package.")
    return false
  }
  val siteKey = context.getString(resId)
  if (siteKey.isBlank()) {
    Log.e(TAG, "recaptcha_site_key resource string is blank.")
    return false
  }
  FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
    RecaptchaAppCheckProviderFactory.getInstance(siteKey)
  )
  Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
  return true
}
