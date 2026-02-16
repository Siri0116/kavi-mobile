# Kavi Mobile

A Siri/Alexa-style voice assistant for Android with full system control capabilities.

## Features

- 🎤 **Voice Recognition** - Speech-to-text using Android's built-in recognizer
- 🧠 **Intent Engine** - Rule-based command classification
- 📱 **App Control** - Launch any installed app by voice
- 📞 **Phone Control** - Make calls to contacts
- 📸 **Camera Control** - Take photos and videos
- 🗺️ **Navigation** - Open Google Maps with destinations
- ♿ **Accessibility Service** - Advanced UI automation
- 🔄 **Background Service** - Always-on listening mode
- 🌐 **AI Backend Ready** - Prepared for integration with Kavi server

## Project Structure

```
kavi-mobile/
├── app/
│   ├── src/main/
│   │   ├── java/com/kavi/mobile/
│   │   │   ├── MainActivity.kt              # Main UI and voice input
│   │   │   ├── services/
│   │   │   │   ├── KaviForegroundService.kt # Background service
│   │   │   │   └── KaviAccessibilityService.kt # UI automation
│   │   │   ├── engine/
│   │   │   │   ├── IntentEngine.kt          # Command classification
│   │   │   │   └── ActionExecutor.kt        # Action routing
│   │   │   └── automation/
│   │   │       ├── AppLauncher.kt           # App launching
│   │   │       └── PhoneController.kt       # Phone/SMS control
│   │   ├── res/                             # UI resources
│   │   └── AndroidManifest.xml              # Permissions & services
│   └── build.gradle.kts                     # Dependencies
├── build.gradle.kts                         # Project config
└── settings.gradle.kts                      # Gradle settings
```

## Setup Instructions

### Prerequisites

1. **Android Studio** - Download from [developer.android.com](https://developer.android.com/studio)
2. **Android SDK** - Install via Android Studio
3. **Physical Android Device** - For testing (emulator has limited voice support)

### Installation

1. **Open Project in Android Studio**
   ```
   File → Open → Select kavi-mobile folder
   ```

2. **Sync Gradle**
   - Android Studio will automatically sync dependencies
   - Wait for "Gradle sync finished" message

3. **Enable Developer Options on Phone**
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Developer Options will appear in Settings

4. **Enable USB Debugging**
   - Settings → Developer Options → USB Debugging (ON)

5. **Connect Phone via USB**
   - Connect phone to laptop
   - Allow USB debugging when prompted

6. **Build and Install**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   - Or click the green "Run" button in Android Studio
   - Select your connected device

### First Run Setup

1. **Grant Permissions**
   - Microphone (required for voice input)
   - Phone (for making calls)
   - Contacts (for contact lookup)
   - Camera (for photo/video)
   - Location (for navigation)

2. **Enable Accessibility Service** (for advanced automation)
   - Settings → Accessibility
   - Find "Kavi Mobile"
   - Toggle ON
   - Confirm warning dialog

3. **Start Background Service** (optional)
   - Tap "Start Background Service" button in app
   - Kavi will run continuously in background

## Usage

### Basic Commands

**App Launching:**
- "Open Instagram"
- "Launch Chrome"
- "Start WhatsApp"

**Phone Calls:**
- "Call John"
- "Phone Mom"
- "Dial Sarah"

**Camera:**
- "Take a photo"
- "Take a picture"
- "Record a video"

**Navigation:**
- "Navigate to home"
- "Directions to office"
- "Map to New York"

**Messages:**
- "Send message to John"
- "WhatsApp Sarah"

**Other:**
- "Set alarm for 7 AM"
- "Play music"
- "Search for pizza near me"

### Voice Input

1. Tap the microphone button
2. Speak your command clearly
3. Wait for processing
4. Action will execute automatically

## Development Phases

### ✅ Phase 1: Base Setup (COMPLETE)
- Android project structure
- Gradle configuration
- MainActivity with UI
- Permission handling

### ✅ Phase 2: Voice Input (COMPLETE)
- Speech recognition integration
- Microphone permissions
- Voice UI feedback

### ✅ Phase 3: Intent Engine (COMPLETE)
- Rule-based command classification
- Parameter extraction
- Intent routing

### ✅ Phase 4: Android Control (COMPLETE)
- App launching
- Phone calls
- Camera control
- Navigation

### ✅ Phase 5: Accessibility Service (COMPLETE)
- UI automation framework
- Screen reading
- Click/type/scroll actions

### ✅ Phase 6: Background Service (COMPLETE)
- Foreground service
- Persistent notification
- Always-on capability

### 🔄 Phase 7: APK Build & Testing (NEXT)
- Generate signed APK
- Sideload to device
- Real-world testing

### 🔮 Phase 8: AI Backend Integration (FUTURE)
- REST API client
- WebSocket support
- Server communication
- Offline fallback

## Building APK for Sideloading

### Debug APK (for testing)

```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK (for distribution)

1. **Generate Keystore** (first time only)
   ```
   Build → Generate Signed Bundle / APK
   → APK → Create new keystore
   ```

2. **Build Signed APK**
   ```
   Build → Generate Signed Bundle / APK
   → APK → Select keystore → Release
   ```

3. **Transfer to Phone**
   - Via USB cable
   - Via cloud storage
   - Via email

4. **Install on Phone**
   - Settings → Security → Install unknown apps
   - Enable for your file manager
   - Open APK file and install

## Permissions Explained

| Permission | Purpose |
|------------|---------|
| RECORD_AUDIO | Voice input via microphone |
| CALL_PHONE | Make phone calls |
| READ_CONTACTS | Lookup contacts by name |
| CAMERA | Take photos/videos |
| ACCESS_FINE_LOCATION | Navigation features |
| FOREGROUND_SERVICE | Background listening |
| BIND_ACCESSIBILITY_SERVICE | UI automation |
| INTERNET | Future AI backend |

## Limitations

### WhatsApp Messaging
- Can open chat and pre-fill message
- **Cannot auto-send** (OS restriction)
- User must tap send button

### Play Store vs Sideload
- **Sideload (recommended)**: Full features, no restrictions
- **Play Store**: Limited accessibility, background restrictions

### Battery Usage
- Background service uses ~3-5% per hour
- Optimize in Settings → Battery → Kavi Mobile

## Troubleshooting

### Voice recognition not working
- Check microphone permission
- Ensure internet connection (for cloud recognition)
- Try speaking more clearly

### App won't launch
- Check if app is installed
- Try full app name (e.g., "Instagram" not "Insta")

### Accessibility features not working
- Enable accessibility service in Settings
- Restart app after enabling

### Background service stops
- Disable battery optimization for Kavi
- Settings → Battery → Battery optimization → Kavi → Don't optimize

## Future Enhancements

- [ ] Custom wake word detection ("Hey Kavi")
- [ ] Offline speech recognition (Vosk/Whisper)
- [ ] AI backend integration
- [ ] Conversation context memory
- [ ] Smart home control
- [ ] Calendar integration
- [ ] Email automation
- [ ] Custom command scripting

## Technical Stack

- **Language**: Kotlin
- **Min SDK**: API 26 (Android 8.0)
- **Target SDK**: API 34 (Android 14)
- **Architecture**: MVVM-inspired
- **UI**: Material Design 3
- **Networking**: Retrofit + OkHttp (for future AI backend)

## License

Private project - Not for distribution

## Contact

For questions or issues, contact the development team.

---

**Built with ❤️ for voice-first mobile interaction**
