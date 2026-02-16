# Building Kavi-Mobile APK

## Prerequisites

- Android Studio installed
- Android SDK installed (via Android Studio)
- Physical Android device (recommended) or emulator

---

## Method 1: Build and Install via Android Studio (Recommended)

### Step 1: Open Project
1. Launch Android Studio
2. Click **File → Open**
3. Navigate to `c:\xampp\htdocs\kavi-mobile`
4. Click **OK**

### Step 2: Wait for Gradle Sync
- Android Studio will automatically download dependencies
- Wait for "Gradle sync finished" notification
- This may take 2-5 minutes on first open

### Step 3: Connect Device
1. Enable Developer Options on your Android phone:
   - Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Settings → Developer Options → USB Debugging (ON)
3. Connect phone via USB
4. Allow USB debugging when prompted

### Step 4: Run the App
1. Click the green **Run** button (▶) in toolbar
2. Select your connected device from the list
3. Wait for build and installation
4. App will launch automatically

---

## Method 2: Build APK for Manual Installation

### Debug APK (for testing)

1. In Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for build completion
3. Click "locate" in the notification to find the APK
4. APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK (for distribution)

#### First Time: Generate Keystore

1. **Build → Generate Signed Bundle / APK**
2. Select **APK**
3. Click **Create new...**
4. Fill in keystore details:
   - Key store path: Choose location (e.g., `kavi-mobile-keystore.jks`)
   - Password: Create strong password
   - Alias: `kavi-mobile`
   - Validity: 25 years
   - Certificate info: Fill your details
5. Click **OK**
6. **IMPORTANT**: Save keystore file and passwords securely!

#### Build Signed APK

1. **Build → Generate Signed Bundle / APK**
2. Select **APK**
3. Choose your keystore file
4. Enter passwords
5. Select **release** build variant
6. Click **Finish**
7. APK location: `app/build/outputs/apk/release/app-release.apk`

---

## Method 3: Build via Command Line (Advanced)

### Debug APK
```bash
cd c:\xampp\htdocs\kavi-mobile
gradlew assembleDebug
```
APK: `app\build\outputs\apk\debug\app-debug.apk`

### Release APK (requires keystore)
```bash
gradlew assembleRelease
```
APK: `app\build\outputs\apk\release\app-release.apk`

---

## Installing APK on Android Device

### Via USB (ADB)
```bash
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Via File Transfer

1. **Transfer APK to phone:**
   - USB cable: Copy to Downloads folder
   - Cloud: Upload to Google Drive/Dropbox, download on phone
   - Email: Send to yourself, download on phone

2. **Enable installation from unknown sources:**
   - Android 8.0+: Settings → Apps → Special access → Install unknown apps
   - Select your file manager → Allow from this source
   - Older Android: Settings → Security → Unknown sources (ON)

3. **Install:**
   - Open file manager on phone
   - Navigate to APK location
   - Tap the APK file
   - Tap **Install**
   - Tap **Open** when done

---

## First Run Setup

### 1. Grant Permissions

When you first open Kavi Mobile, grant these permissions:

- ✅ **Microphone** (REQUIRED) - For voice input
- ✅ **Phone** - For making calls
- ✅ **Contacts** - For contact lookup
- ✅ **Camera** - For photos/videos
- ✅ **Location** - For navigation

**Note**: You can grant permissions later in Settings → Apps → Kavi Mobile → Permissions

### 2. Enable Accessibility Service (Optional)

For advanced automation features:

1. Go to **Settings → Accessibility**
2. Find **Kavi Mobile** in the list
3. Toggle it **ON**
4. Read and confirm the warning dialog
5. Tap **Allow**

### 3. Disable Battery Optimization (Recommended)

To keep background service running:

1. **Settings → Battery → Battery optimization**
2. Select **All apps**
3. Find **Kavi Mobile**
4. Select **Don't optimize**

---

## Testing the App

### Basic Voice Test

1. Open Kavi Mobile
2. Tap the microphone button
3. Say: **"Open Chrome"**
4. Chrome should launch

### Test Commands

Try these commands:
- "Open Instagram"
- "Take a photo"
- "Open camera"
- "Open settings"

### Background Service Test

1. Tap **"Start Background Service"** button
2. Check notification appears
3. Minimize app
4. Service should keep running

---

## Troubleshooting Build Issues

### Gradle Sync Failed

**Error**: "Failed to sync Gradle"

**Solution**:
1. Check internet connection
2. **File → Invalidate Caches / Restart**
3. Delete `.gradle` folder in project
4. Restart Android Studio

### SDK Not Found

**Error**: "SDK location not found"

**Solution**:
1. Create `local.properties` file in project root
2. Add: `sdk.dir=C\:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk`
3. Sync Gradle again

### Build Failed - Missing Dependencies

**Error**: "Could not resolve dependencies"

**Solution**:
1. Check internet connection
2. **File → Settings → Build → Gradle**
3. Ensure "Offline work" is unchecked
4. Sync Gradle again

### APK Installation Failed

**Error**: "App not installed"

**Solutions**:
- Uninstall any existing version first
- Enable "Install unknown apps" for your file manager
- Check if APK file is corrupted (re-download)
- Ensure Android version is 8.0+ (API 26+)

---

## Build Variants

### Debug
- **Purpose**: Development and testing
- **Size**: Larger (includes debug symbols)
- **Performance**: Slower
- **Logging**: Enabled
- **Signature**: Debug keystore (auto-generated)

### Release
- **Purpose**: Distribution
- **Size**: Smaller (optimized)
- **Performance**: Faster
- **Logging**: Disabled
- **Signature**: Your keystore (required)
- **ProGuard**: Enabled (code obfuscation)

---

## APK Size

- **Debug APK**: ~5-8 MB
- **Release APK**: ~3-5 MB (after ProGuard)

---

## Minimum Requirements

- **Android Version**: 8.0 (Oreo) or higher
- **API Level**: 26+
- **RAM**: 2GB minimum
- **Storage**: 50MB free space
- **Microphone**: Required for voice input
- **Internet**: Required for speech recognition

---

## Next Steps After Installation

1. ✅ Install APK on device
2. ✅ Grant all permissions
3. ✅ Enable accessibility service
4. ✅ Test voice commands
5. ✅ Start background service
6. ✅ Test automation features

---

## Support

For build issues, check:
- [README.md](file:///c:/xampp/htdocs/kavi-mobile/README.md) - Full documentation
- [QUICKSTART.md](file:///c:/xampp/htdocs/kavi-mobile/QUICKSTART.md) - Quick start guide
- Android Studio build output for error details

---

**Ready to build! 🚀**
