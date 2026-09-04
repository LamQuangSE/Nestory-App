# Nestory-App

## Project Docs

- [Coding Convention](CODING_CONVENTION.md)
- [Git Workflow](GIT_WORKFLOW.md)
- [Project Structure](PROJECT_STRUCTURE.md)

# How to Run the Application

This guide details how to launch and run the Nestory application: either using an Android Virtual Device (Emulator) via Android Studio, or using a physical Android device.

---

## Method 1: Running with Android Studio & Android Virtual Device (Emulator) *(Recommended)*

Nestory integrates on-device Google ML Kit (which requires Google Play Services) and biometric authentication. Configuring the virtual device with the correct system image is required.

### Step 1: Create a Virtual Device
1. In Android Studio, open **Device Manager** (`Tools` > **Device Manager** or click the Device Manager icon on the top-right toolbar).
2. Click **Create Device** (or the **+** button).
3. Select hardware specifications:
   - **Category:** Phone
   - **Device:** Select **Pixel 8** (or Pixel 7)
   - Click **Next**.

### Step 2: Select a System Image with Google Play
1. Under the **Recommended** tab, locate the target system image:
   - **Release Name:** **UpsideDownCake** (API Level 34 - Android 14.0)
   - **Target:** Must select the variant labeled **Google Play** (e.g., `Google Play | x86_64` or `Google Play | arm64-v8a`)
   > **Important:** Do not select a system image labeled only with *Google APIs*. The Google ML Kit Document Scanner component requires Google Play Services from the official Play Store image to function properly.
2. Click the **Download** button next to the system image if it is not yet installed.
3. Once downloaded, select the image and click **Next**.

### Step 3: Complete Configuration
1. **AVD Name:** Retain the default name or rename it (e.g., `Pixel_8_API_34`).
2. Under **Emulated Performance**:
   - **Graphics:** Set to `Automatic` or `Hardware - GLES 2.0` to enable host GPU acceleration.
3. Click **Finish**.

### Step 4: Launch the Emulator and Run the App
1. In the **Device Manager**, click the **Play** icon next to your virtual device to start the emulator.
2. Wait for the virtual device to finish booting to the Android home screen.
3. On the top toolbar of Android Studio:
   - Ensure your running emulator is selected in the target device dropdown.
   - Ensure `app` is selected in the run configuration dropdown.
4. Click **Run** (green play button or press `Shift + F10`).
5. Gradle will build, install, and automatically launch Nestory on the emulator.

### Simulating Hardware Features on the Emulator
- **Biometric Authentication (Fingerprint):** When the app displays the vault unlock prompt, open the emulator side toolbar, click the three dots (`...`) for **Extended Controls**, navigate to the **Fingerprint** tab, and click **Touch Sensor** to simulate a successful fingerprint match.
- **Camera Document Scanning:** In **Extended Controls** > **Camera**, verify that the back camera is set to `VirtualScene` (enabling movement in an emulated 3D room to capture test documents) or connected to your computer's webcam.

---

## Method 2: Running on a Physical Android Device

Used for testing real camera capture, physical fingerprint hardware, and background alarm notifications.

### Step 1: Enable Developer Options
1. Open **Settings** on your Android device (Android 7.0+ required, Android 12+ recommended).
2. Go to **About phone** > **Software information**.
3. Tap **Build number** 7 consecutive times until developer mode is unlocked.

### Step 2: Enable USB Debugging
1. Return to the main **Settings** menu and open **Developer options**.
2. Toggle **USB debugging** to **On**.

### Step 3: Connect and Run
 1. Connect your Android device to your computer via a USB cable.
 2. When the prompt *"Allow USB debugging?"* appears on your phone, check *Always allow from this computer* and tap **Allow**.
 3. In Android Studio, select your physical device from the target device dropdown.
 4. Click **Run** (`Shift + F10`) to build and deploy the app directly to your phone.

---

## Method 3: Install via APK using ADB (Command Line)

Use this if you prefer building the APK locally and installing via terminal without opening Android Studio.

### Step 1–2: Enable Developer Options & USB Debugging
Same as **Method 2, Step 1–2**:
1. Enable **Developer Options** (tap Build number 7×)
2. Enable **USB Debugging** in Developer Options

### Step 3: Connect Device & Authorize
1. Connect your Android device via USB.
2. Allow USB debugging on the device prompt (check *Always allow from this computer*).

### Step 4: Build the Debug APK

**Terminal to use:**
- **Windows:** PowerShell (default in VS Code terminal) or Command Prompt (`cmd`)
- **macOS / Linux:** Terminal (bash/zsh)

**Commands:**

```bash
# Windows (PowerShell)
cd Nestory-App\src
.\gradlew assembleDebug

# Windows (Command Prompt / cmd)
cd Nestory-App\src
gradlew.bat assembleDebug

# macOS / Linux
cd Nestory-App/src
chmod +x gradlew
./gradlew assembleDebug
```

The APK will be generated at:
```
Nestory-App/src/app/build/outputs/apk/debug/app-debug.apk
```

### Step 5: Install via ADB

```bash
# Windows (PowerShell / cmd) — same syntax
cd Nestory-App\src\app\build\outputs\apk\debug
adb install -r app-debug.apk

# macOS / Linux
cd Nestory-App/src/app/build/outputs/apk/debug
adb install -r app-debug.apk
```
- `-r` reinstalls/overwrites if the app already exists.

> **Tip:** Verify device connection first with `adb devices`. Your device should appear as `device` (not `unauthorized`).