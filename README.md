# Gboard Cursor Keys 1.0.0

LSPosed module for two cursor arrows at the bottom corners of Gboard. Tap once to move one character; hold for 350 ms to repeat every 75 ms. The buttons use light/dark colours from Android's current theme.

This source ZIP uses the Android SDK preinstalled on GitHub's `ubuntu-latest` runner. It does not run `android-actions/setup-android` or `sdkmanager`, which previously failed with `Failed to find package 'tools'`.

## Build and install

Upload the contents of this folder to a GitHub repository, open **Actions → Build APK → Run workflow**, and download the `GboardCursorKeys-debug-apk` artifact after the run. Install `app-debug.apk`, enable **Gboard Cursor Keys** in LSPosed and scope **only Gboard** (`com.google.android.inputmethod.latin`). Force-stop Gboard or restart your phone, then test in a text field.

The module adds its buttons to the IME window and sends DPAD_LEFT/DPAD_RIGHT through the current input connection. It does not need root commands or Accessibility. The overlays occupy 44 dp in each lower corner, so they may cover Gboard keys on some layouts; this first build is intended for hands-on placement testing. Gboard may use a different window implementation in a future version. If your keyboard does not show the arrows, capture the LSPosed log and a screenshot of Gboard open.
