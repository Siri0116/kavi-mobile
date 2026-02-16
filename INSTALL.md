# 📲 How to Install Kavi-Mobile

You are correct—simply copying the folder won't work! Android apps need to be compiled into an **APK file** (Android Package) to run on your phone.

Here are the 3 ways to install Kavi:

---

## Option 1: The "Pro" Way (Recommended)
**Best for:** Developers who want to edit code later.

1. **Download & Install Android Studio** (if you haven't already).
2. Open Android Studio.
3. Select **Open** and choose the `c:\xampp\htdocs\kavi-mobile` folder.
4. Wait for it to "sync" (download dependencies).
5. **Connect your Android phone** to your PC via USB.
   - Make sure **USB Debugging** is on (Settings > Developer Options).
6. Click the green **Run ▶️** button in the top toolbar.
   - Select your phone from the list.
   - Android Studio will build the app and install it automatically.

---

## Option 2: The Command Line Way (Fast)
**Best for:** If you just want the APK file right now.

1. I am currently running the build command for you in the background.
2. Once finished, the APK file will be located here:
   `c:\xampp\htdocs\kavi-mobile\app\build\outputs\apk\debug\app-debug.apk`
3. **Copy this specific file** to your phone (via USB, Google Drive, or WhatsApp).
4. On your phone, tap the file to install it.
   - You may need to allow "Install from Unknown Sources".

---

## Option 3: Using ADB (Wireless/Wired)
**Best for:** Installing without Android Studio GUI.

1. Open your terminal/command prompt.
2. Navigate to the project folder:
   ```cmd
   cd c:\xampp\htdocs\kavi-mobile
   ```
3. Connect your phone via USB.
4. Run this command:
   ```cmd
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

---

## ⚠️ Important: Granting Permissions
Once installed, Kavi needs permissions to work. She won't ask nicely for all of them automatically (due to security policies), so you must grant them manually:

1. Go to **Settings > Apps > Kavi**.
2. **Permissions**: Allow Microphone, Location, Phone, Contacts.
3. **Advanced Settings** (Crucial for Security/Automation):
   - **Display over other apps**: ALLOW (for security alerts).
   - **Modify System Settings**: ALLOW (for brightness/screen timeout).
   - **Accessibility**: Go to Settings > Accessibility > Installed Apps > **Kavi Service** > Turn **ON**.
   - **Notification Access**: Search "Notification Access" in settings > Enable for Kavi.
   - **Usage Access**: Search "Usage Access" in settings > Enable for Kavi.

---

## ❓ Troubleshooting

**"App not installed"**
- Uninstall any previous version of the app first.
- Ensure you have enough storage space.

**"Blocking installation" (Play Protect)**
- Click "More Details" > "Install Anyway" (Since this is a custom app, Google doesn't recognize it).

**"Build Failed"**
- Check your internet connection (Gradle needs to download tools).
- Ensure JDK 17+ is installed (Android Studio handles this usually).
