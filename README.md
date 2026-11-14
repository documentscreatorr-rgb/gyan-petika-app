# Gyan Petika — Android App

**GYAN PETIKA** (*"The Digital Box of Knowledge"*) is a native Android app (Kotlin + Jetpack Compose + Material 3) connected live to [gyanpetika.blogspot.com](https://gyanpetika.blogspot.com).

## Download the APK via GitHub Actions (No PC Required)
1. Push this project to a GitHub repository (from AI Studio's **Push to GitHub** button or `git push`).
2. Open the **Actions** tab in your GitHub repository.
3. Click the **Build Gyan Petika APK** workflow run (or trigger it manually with **Run workflow**).
4. Once it finishes (green checkmark), scroll to **Artifacts** and download **`Gyan-Petika-Debug-APK`**.
5. Unzip the downloaded artifact and tap `app-debug.apk` on your Android phone to install.

## Build Locally with Android Studio
1. Generate `debug.keystore` in the repository root from `debug.keystore.base64`:
   - **macOS / Linux:** `base64 -d debug.keystore.base64 > debug.keystore`
   - **Windows (PowerShell):** `[IO.File]::WriteAllBytes("debug.keystore", [Convert]::FromBase64String((Get-Content debug.keystore.base64 -Raw)))`
2. Open the project in Android Studio and select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
