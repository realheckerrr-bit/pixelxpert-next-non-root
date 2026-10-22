# PixelXpert-Next Non-Root & Wireless ADB Edition

<p align="center">
  <img src="PixelXpert.svg" alt="PixelXpert Logo" width="160" height="160" />
</p>

<p align="center">
  <b>Pixel Customization Suite ported for Non-Root devices via Wireless ADB, Shizuku, and LSPatch</b><br>
  <i>Compatible with Google Pixel devices running Android 13, 14, 15, 16, and 17</i>
</p>

---

## ⚡ What is PixelXpert-Next Non-Root?

This edition ports **PixelXpert-Next** from a root-only Magisk/Zygisk module into a **standalone signed Android application** (`.apk`) that operates **without root access**, leveraging:

1. **Wireless ADB (`WRITE_SECURE_SETTINGS`)**: Native permission to control system preferences, status bar icons, refresh rates, animations, and gesture insets directly.
2. **Shizuku Integration**: Direct connection to Android's privileged shell (UID 2000) for instant SystemUI restarts, display scaling (`wm size` / `wm density`), and privileged command execution.
3. **LSPatch Compatibility**: Seamless integration with [LSPatch](https://github.com/lsposed/lspatch) (non-root Xposed framework) for apps running in unrooted patched environments.
4. **Universal Pixel Compatibility**: `minSdk` configured to **API 33 (Android 13)** through **API 37 (Android 17)**.

---

## 🌟 Available Non-Root Features

| Category | Feature | Non-Root Mechanism |
|---|---|---|
| **Status Bar** | Hide / Show individual icons (Wi-Fi, Mobile Data, Battery, Clock, Bluetooth, Alarm, Volume, Zen, Hotspot, VPN, GPS, NFC) | `icon_blacklist` in `Settings.Secure` |
| **Display** | Lock Display Refresh Rate (60Hz, 90Hz, 120Hz) | `peak_refresh_rate` & `min_refresh_rate` in `Settings.System` |
| **Animations** | System Animation Scales (Instant 0x, Fast 0.5x, Normal 1x) | `window_animation_scale`, `transition_animation_scale`, `animator_duration_scale` |
| **Gestures** | Back gesture navigation sensitivity & inset widths | `back_gesture_inset_scale_left`, `back_gesture_inset_scale_right` |
| **Ambient / AOD** | Always On Display, Double-Tap-to-Wake, Pick-Up-to-Wake | `doze_always_on`, `doze_pulse_on_double_tap`, `doze_pulse_on_pick_up` |
| **Notifications** | Toggle Heads-up Notification banners | `heads_up_notifications_enabled` in `Settings.Global` |
| **Display Scaling** | Custom resolution & DPI density overrides | `wm size` & `wm density` via Shizuku / ADB Shell |
| **SystemUI** | Instant restart without device reboot | `pkill -f com.android.systemui` via ADB |
| **Wireless ADB** | Pin ADB daemon port to 5555 | `setprop service.adb.tcp.port 5555` |

---

## 🚀 Installation & Quick Start

### 1. Install the APK
Download the latest signed release APK:
* [Download `PixelXpert-Next-NonRoot.apk`](https://github.com/realheckerrr-bit/pixelxpert-next-non-root/releases/latest)

Install it directly on your Google Pixel device.

### 2. Grant Privileges (One-Time Setup)

Choose either **Method A** or **Method B**:

#### Method A: Wireless ADB / USB ADB (Recommended)
Connect your device to a computer via USB or enable **Wireless Debugging** in Developer Options, then execute:
```sh
adb shell pm grant sh.anirudhgupta109.pxn android.permission.WRITE_SECURE_SETTINGS
```

#### Method B: Shizuku
1. Install and launch [Shizuku](https://shizuku.rikka.app/).
2. Start Shizuku via Wireless Debugging on your device.
3. Open **PixelXpert-Next Non-Root** and tap **Authorize** on the Shizuku banner.

---

## 🛠️ Building From Source

```sh
# Clone repository
git clone https://github.com/realheckerrr-bit/pixelxpert-next-non-root.git
cd pixelxpert-next-non-root

# Build signed release APK
./gradlew assembleRelease
```
The output APK is emitted at:
`app/build/outputs/apk/release/PixelXpert-Next.apk`

---

## ⚖️ License & Credits

* Forked from [PixelXpert-Next](https://github.com/anirudhgupta109/PixelXpert-Next) by Anirudh Gupta and [PixelXpert](https://github.com/siavash79/PixelXpert) by Siavash.
* Non-Root port by realhecker.
* Shizuku by Rikka.
* Licensed under the GNU General Public License v3.0 (GPL-3.0).
