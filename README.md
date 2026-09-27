# Gboard Cursor Keys 1.0.8 navigation row prototype

LSPosed module for two cursor arrows in the navigation row while Gboard is visible. Tap once to move one character; hold for 350 ms to repeat every 75 ms.

This source ZIP uses the Android SDK preinstalled on GitHub's `ubuntu-latest` runner. It does not run `android-actions/setup-android` or `sdkmanager`, which previously failed with `Failed to find package 'tools'`.
The Xposed API dependency resolves through `https://api.xposed.info/`, listed in `settings.gradle`.

## Diagnostic test

Enable the module in LSPosed for **Gboard** (`com.google.android.inputmethod.latin`) and **Launcher3** (`com.android.launcher3`), then reboot. This prototype puts arrows in the Launcher3 navigation row and forwards their taps to Gboard. Gboard now reports keyboard visibility directly to Launcher3, because Launcher3's navigation view did not expose reliable IME insets. It does not modify NavDotStyle. In a normal text field, type `abcd`, put the cursor between `b` and `c`, then tap each arrow once and hold one briefly. Share LSPosed log lines containing `GboardCursorKeys:` if the controls are absent or unresponsive. The logs do **not** include text from the editor.

## Build and install

Upload the contents of this folder to a GitHub repository, open **Actions → Build APK → Run workflow**, and download the `GboardCursorKeys-debug-apk` artifact after the run. Install `app-debug.apk`, enable **Gboard Cursor Keys** in LSPosed, scope Gboard and Launcher3, then reboot and test in a text field.

This version uses Launcher3's navigation button container, which may vary between ROM releases. If the arrows do not appear, inspect the `Launcher controller` and `Launcher nav` log lines. If taps are logged but the cursor does not move, inspect the `Gboard cursor receiver` and `move` lines.
