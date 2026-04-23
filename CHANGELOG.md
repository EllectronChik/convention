# Change logs

## 1.0.1 - 2026.04.23
### Fixes
- `versionName` and `versionCode` not being applied to Android application modules. The plugin now resolves `ApplicationAndroidComponentsExtension` directly instead of looking up the generic `AndroidComponentsExtension` and performing a safe cast to `ApplicationExtension`, which could silently no-op.
