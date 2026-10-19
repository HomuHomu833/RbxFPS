# RbxFPS

Small Android app that writes a FFlag override named `DFIntTaskSchedulerTargetFps` to
`/data/local/tmp/ClientAppSettings.json`, the external settings file the Roblox Android
client looks for at launch. Works through **root** or **Shizuku**.

Package: `com.homuhomu833.rbxfps`

## Use
1. Install the APK (download the artifact from the Actions tab).
2. Pick **Root** or **Shizuku** (Shizuku must be running and permission granted).
3. Choose an FPS, tap **Apply**. Roblox is force-closed so it re-reads the file.
4. **Status** shows the file and its contents. **Remove** reverts.

## Build
GitHub Actions (manual run) builds a debug-signed APK with AGP 9.4, Gradle 9.6.1, JDK 21,
compileSdk/targetSdk 37 (Android 17), Material 3 Expressive with dynamic colors.
Locally: open in Android Studio, or run `gradle assembleDebug` with JDK 17+ and the Android SDK.

## Notes
- Roblox only honours FFlags on its own allowlist and may change that list or the
  client at any time. This may stop working without notice.
- Modifying the Roblox client can conflict with Roblox's Terms of Use. Use at your own risk.
  This project is not affiliated with Roblox.
