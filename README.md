# RbxFPS

Small Android app that writes a FFlag override named `DFIntTaskSchedulerTargetFps` to
`/data/local/tmp/ClientAppSettings.json`, the external settings file the Roblox Android
client looks for at launch. Works through **root** or **Shizuku**.

## Use
1. Install the APK (from Actions artifacts or Releases).
2. Pick **Root** or **Shizuku** (Shizuku must be running and permission granted).
3. Choose an FPS, tap **Apply**. Roblox is force-closed so it re-reads the file.
4. **Check file status** shows the file and its contents. **Remove override** reverts.

The file can disappear after a Roblox update; tap Apply again.

## Notes
- Roblox only honours FFlags on its own allowlist and may change that list or the
  client at any time. This may stop working without notice.
- Modifying the Roblox client can conflict with Roblox's Terms of Use. Use at your own risk,
  preferably on an account you can afford to lose. This project is not affiliated with Roblox.
- This only writes the FPS flag. It does not inject code or touch client memory.
