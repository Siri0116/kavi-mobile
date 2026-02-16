# Kavi-Mobile Installation & Setup Guide

## 🚀 Kavi is Ready!
Kavi has been built with a robust **Core Architecture** featuring:
- **Always-On Voice**: Listens for "Kavi" even when screen is off.
- **Smart Overlay**: Floating interface that appears over other apps.
- **Conversational Memory**: Remembers context across the conversation.
- **Advanced Automation**: Can control your screen and apps.

---

## 📲 Installation Steps

### Step 4: Build & Install
**Option A: Direct Install (Recommended)**
1. Click the green **Run** button (▶) in Android Studio
2. Select your connected device
3. Wait for the app to install and launch

**Option B: Build APK for Sideloading**
1. Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for build completion
3. APK location: `app/build/outputs/apk/debug/app-debug.apk`
4. Transfer APK to phone (USB/cloud/email)
5. On phone: Settings → Security → Install unknown apps → Enable
6. Open APK file and install

**Option C: Cloud Build (No PC Required)**
If you cannot run Android Studio on your PC:
1. Create a GitHub repository named `kavi-mobile`
2. **IMPORTANT**: Upload ALL files from INSIDE your project folder to the repository root.
   - Do NOT upload the `kavi-mobile` folder itself.
   - You should see `gradlew`, `app`, and `.github` in the main list.
3. Go to **Actions** tab on GitHub
4. Select **Build Kavi APK** workflow
5. Wait for build to finish (~3 mins) and download `kavi-mobile-debug` artifact
6. Install on your phone

### 2. Grant Core Permissions
When Kavi first launches, it will ask for standard permissions. **Allow All**:
- ✅ **Microphone** (Required for hearing you)
- ✅ **Phone/Contacts** (Required for calling/texting)
- ✅ **Location** (Required for weather/navigation)
- ✅ **Notifications** (Required to keep the service running)

### 3. Grant Advanced Permissions (CRITICAL)
For Kavi to work as a true assistant, you must manually grant these 3 special permissions:

#### A. Display Over Other Apps (For the Overlay)
1. Kavi will prompt you or take you to settings.
2. Find **Kavi Mobile** in the list.
3. Toggle **Allow display over other apps** -> **ON**.
   * *Why? This allows Kavi to show its floating face and UI on top of your lock screen or other apps.*

#### B. Battery Optimization (For Always-On Listening)
1. Kavi will request to ignore battery optimizations.
2. Select **Allow** or **Unrestricted**.
   * *Why? Android kills background apps to save battery. This keeps Kavi's "ears" open.*

#### C. Accessibility Service (For Brain & Automation)
1. Go to **Settings > Accessibility > Downloaded Apps**.
2. Tap **Kavi Accessibility Service**.
3. Toggle **Use Kavi Accessibility Service** -> **ON**.
   * *Why? This allows Kavi to "see" your screen (read notifications, find buttons) and "touch" it (scroll, click, close apps).*

---

## 🗣️ How to Use Kavi

### Activation
*   **Wake Word**: Just say **"Kavi"** anytime (even if screen is off!).
*   **Manual**: Tap the **Mic Button** in the app or the **Floating Orb**.

### Try These Commands
*   **Conversational**:
    *   "Hello, who are you?"
    *   "Tell me a joke."
    *   "What's the weather like?"
*   **Action**:
    *   "Open Instagram."
    *   "Call Mom."
    *   "Take a selfie."
    *   "Turn on the flashlight."
*   **Memory**:
    *   "My name is John." ... "What is my name?"
    *   "Remind me to buy milk."
*   **System**:
    *   "Turn do not disturb on."
    *   "Set volume to 50%."
*   **Security**:
    *   "Run a security check."

---

## 🔧 Troubleshooting

### "Kavi isn't listening when screen is off"
*   Check **Battery Optimization** settings. It must be **Unrestricted**.
*   Ensure the notification "Kavi is listening" is visible in your status bar.

### "Kavi can't click buttons"
*   Ensure **Accessibility Service** is enabled.
*   Some secure apps (like banking) block accessibility clicks.

### "I don't see the floating bubble"
*   Ensure **Display Over Other Apps** permission is granted.

---

## 📂 Project Status
**Version**: 1.0.0 (Core Architecture Verified)
**Build**: Debug
**Backend**: Local Logic + Web Search (Privacy Focused)
