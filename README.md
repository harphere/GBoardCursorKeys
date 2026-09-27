# Gboard Cursor Keys 1.0.4 diagnostic build

LSPosed module for two cursor arrows at the bottom corners of Gboard. Tap once to move one character; hold for 350 ms to repeat every 75 ms. The buttons use light/dark colours from Android's current theme.

This source ZIP uses the Android SDK preinstalled on GitHub's `ubuntu-latest` runner. It does not run `android-actions/setup-android` or `sdkmanager`, which previously failed with `Failed to find package 'tools'`.
The Xposed API dependency resolves through `https://api.xposed.info/`, listed in `settings.gradle`.

## Diagnostic test

After installing 1.0.4, enable the module for Gboard in LSPosed and force-stop Gboard (or restart the phone). In a normal text field, type `abcd`, put the cursor between `b` and `c`, then tap each arrow once and hold one briefly. Open LSPosed's module log and share lines containing `GboardCursorKeys:`. The lines report touch events, connection availability, selection positions and return values; they do **not** include text from the editor. Avoid testing in a password field.

## Build and install

Upload the contents of this folder to a GitHub repository, open **Actions → Build APK → Run workflow**, and download the `GboardCursorKeys-debug-apk` artifact after the run. Install `app-debug.apk`, enable **Gboard Cursor Keys** in LSPosed and scope **only Gboard** (`com.google.android.inputmethod.latin`). Force-stop Gboard or restart your phone, then test in a text field.

The module adds its buttons to the IME window and changes the cursor selection through the current input connection, with DPAD key events as a fallback. It does not need root commands or Accessibility. The overlays occupy 44 dp in each lower corner, so they may cover Gboard keys on some layouts. Gboard may use a different window implementation in a future version. If taps still do not move the cursor, send the LSPosed log and the name of the app and text field used for the test.
