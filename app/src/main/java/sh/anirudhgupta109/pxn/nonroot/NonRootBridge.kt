package sh.anirudhgupta109.pxn.nonroot

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.topjohnwu.superuser.Shell
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Non-Root & Wireless ADB bridge for PixelXpert.
 *
 * Allows applying system tweaks, status bar customization, refresh rate control,
 * animation scales, display scaling, and SystemUI control on Pixel devices from
 * Android 13 to Android 17 without root, using Wireless ADB, Shizuku, or
 * android.permission.WRITE_SECURE_SETTINGS.
 */
object NonRootBridge {

	private const val TAG = "NonRootBridge"

	const val PERMISSION_WRITE_SECURE_SETTINGS = Manifest.permission.WRITE_SECURE_SETTINGS

	// ========================================================================
	// Status & Capability Checks
	// ========================================================================

	/** Check if device is running with Root access */
	fun hasRoot(): Boolean = runCatching { Shell.getShell().isRoot }.getOrDefault(false)

	/** Check if WRITE_SECURE_SETTINGS has been granted to PixelXpert via ADB */
	fun hasWriteSecureSettings(context: Context): Boolean =
		context.checkSelfPermission(PERMISSION_WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

	/** Check if Shizuku manager service is installed and alive */
	fun isShizukuAvailable(): Boolean = runCatching {
		Shizuku.pingBinder()
	}.getOrDefault(false)

	/** Check if PixelXpert is authorized in Shizuku */
	fun hasShizukuPermission(): Boolean = runCatching {
		isShizukuAvailable() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
	}.getOrDefault(false)

	/** Request Shizuku authorization dialog */
	fun requestShizukuPermission(requestCode: Int = 1001) {
		runCatching {
			if (isShizukuAvailable() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
				Shizuku.requestPermission(requestCode)
			}
		}
	}

	/** Check whether either Root, Shizuku or WRITE_SECURE_SETTINGS is available */
	fun isAnyPrivilegeAvailable(context: Context): Boolean =
		hasWriteSecureSettings(context) || hasShizukuPermission() || hasRoot()

	// ========================================================================
	// Privileged Shell Execution
	// ========================================================================

	/**
	 * Run a shell command with the highest available privilege:
	 * 1. Shizuku ADB Process (UID 2000)
	 * 2. Root Shell (UID 0)
	 * 3. Local Process
	 */
	fun runShell(command: String): Pair<Boolean, String> {
		// Try Shizuku first if authorized
		if (hasShizukuPermission()) {
			try {
				val newProcessMethod = Shizuku::class.java.getMethod(
					"newProcess",
					Array<String>::class.java,
					Array<String>::class.java,
					String::class.java,
				)
				val process = newProcessMethod.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
				val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
				val exitCode = process.waitFor()
				if (exitCode == 0) return true to output.trim()
			} catch (t: Throwable) {
				Log.w(TAG, "Shizuku exec failed: $command", t)
			}
		}

		// Try Root if available
		if (hasRoot()) {
			try {
				val result = Shell.cmd(command).exec()
				return result.isSuccess to result.out.joinToString("\n").trim()
			} catch (t: Throwable) {
				Log.w(TAG, "Root exec failed: $command", t)
			}
		}

		// Fallback local execution
		return try {
			val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
			val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
			val exitCode = process.waitFor()
			(exitCode == 0) to output.trim()
		} catch (t: Throwable) {
			false to (t.message ?: "Execution failed")
		}
	}

	// ========================================================================
	// SystemUI Control
	// ========================================================================

	/**
	 * Restart SystemUI cleanly without root via ADB shell process or am crash
	 */
	fun restartSystemUI(context: Context? = null): Boolean {
		val (successShizuku, _) = runShell("pkill -f com.android.systemui || am crash com.android.systemui")
		if (successShizuku) return true

		if (hasRoot()) {
			return Shell.cmd("killall com.android.systemui").exec().isSuccess
		}
		return false
	}

	// ========================================================================
	// Direct Settings Modification (requires WRITE_SECURE_SETTINGS)
	// ========================================================================

	fun putSecureString(cr: ContentResolver, key: String, value: String?): Boolean = try {
		Settings.Secure.putString(cr, key, value)
		true
	} catch (t: Throwable) {
		// Fallback to adb shell if Shizuku is ready
		runShell("settings put secure $key ${value ?: "\"\""}").first
	}

	fun putSecureInt(cr: ContentResolver, key: String, value: Int): Boolean = try {
		Settings.Secure.putInt(cr, key, value)
		true
	} catch (t: Throwable) {
		runShell("settings put secure $key $value").first
	}

	fun putGlobalString(cr: ContentResolver, key: String, value: String?): Boolean = try {
		Settings.Global.putString(cr, key, value)
		true
	} catch (t: Throwable) {
		runShell("settings put global $key ${value ?: "\"\""}").first
	}

	fun putGlobalInt(cr: ContentResolver, key: String, value: Int): Boolean = try {
		Settings.Global.putInt(cr, key, value)
		true
	} catch (t: Throwable) {
		runShell("settings put global $key $value").first
	}

	fun putGlobalFloat(cr: ContentResolver, key: String, value: Float): Boolean = try {
		Settings.Global.putFloat(cr, key, value)
		true
	} catch (t: Throwable) {
		runShell("settings put global $key $value").first
	}

	fun putSystemFloat(cr: ContentResolver, key: String, value: Float): Boolean = try {
		Settings.System.putFloat(cr, key, value)
		true
	} catch (t: Throwable) {
		runShell("settings put system $key $value").first
	}

	fun putSystemInt(cr: ContentResolver, key: String, value: Int): Boolean = try {
		Settings.System.putInt(cr, key, value)
		true
	} catch (t: Throwable) {
		runShell("settings put system $key $value").first
	}

	// ========================================================================
	// Status Bar Icon Blacklist (Non-Root SystemUI Customization)
	// ========================================================================

	const val SETTING_ICON_BLACKLIST = "icon_blacklist"

	/** Read the set of currently blacklisted/hidden status bar icon slots */
	fun getBlacklistedIcons(cr: ContentResolver): Set<String> {
		val raw = Settings.Secure.getString(cr, SETTING_ICON_BLACKLIST) ?: return emptySet()
		return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
	}

	/** Set the exact status bar icons to hide */
	fun setBlacklistedIcons(cr: ContentResolver, icons: Set<String>): Boolean {
		val joined = icons.joinToString(",")
		return putSecureString(cr, SETTING_ICON_BLACKLIST, joined)
	}

	/** Toggle a single status bar icon hidden or shown */
	fun setIconHidden(cr: ContentResolver, iconSlot: String, hidden: Boolean): Boolean {
		val current = getBlacklistedIcons(cr).toMutableSet()
		if (hidden) {
			current.add(iconSlot)
		} else {
			current.remove(iconSlot)
		}
		return setBlacklistedIcons(cr, current)
	}

	// Standard AOSP & Pixel status bar slot names
	val KNOWN_ICON_SLOTS = listOf(
		"wifi" to "Wi-Fi",
		"mobile" to "Mobile Data / Cellular",
		"battery" to "Battery Indicator",
		"clock" to "Status Bar Clock",
		"bluetooth" to "Bluetooth",
		"alarm_clock" to "Alarm Clock",
		"volume" to "Volume / Vibrate / Silent",
		"zen" to "Do Not Disturb",
		"hotspot" to "Mobile Hotspot",
		"ethernet" to "Ethernet",
		"cast" to "Screen Casting",
		"location" to "Location / GPS",
		"vpn" to "VPN Connection",
		"nfc" to "NFC",
		"camera" to "Camera Privacy Indicator",
		"microphone" to "Microphone Indicator"
	)

	// ========================================================================
	// Display & Performance Tweaks (Non-Root)
	// ========================================================================

	/** Force display peak and minimum refresh rates (e.g. 60Hz, 90Hz, 120Hz) */
	fun setRefreshRate(cr: ContentResolver, rate: Float): Boolean {
		val ok1 = putSystemFloat(cr, "peak_refresh_rate", rate)
		val ok2 = putSystemFloat(cr, "min_refresh_rate", rate)
		return ok1 && ok2
	}

	/** Set animation speed multipliers (1.0 = normal, 0.5 = 2x faster, 0.0 = instant) */
	fun setAnimationScales(cr: ContentResolver, scale: Float): Boolean {
		val ok1 = putGlobalFloat(cr, "window_animation_scale", scale)
		val ok2 = putGlobalFloat(cr, "transition_animation_scale", scale)
		val ok3 = putGlobalFloat(cr, "animator_duration_scale", scale)
		return ok1 && ok2 && ok3
	}

	/** Adjust back gesture navigation sensitivity (left/right inset scales) */
	fun setBackGestureSensitivity(cr: ContentResolver, leftInset: Int, rightInset: Int): Boolean {
		val ok1 = putSecureInt(cr, "back_gesture_inset_scale_left", leftInset)
		val ok2 = putSecureInt(cr, "back_gesture_inset_scale_right", rightInset)
		return ok1 && ok2
	}

	/** Toggle Ambient Display / AOD */
	fun setAOD(cr: ContentResolver, enable: Boolean): Boolean =
		putSecureInt(cr, "doze_always_on", if (enable) 1 else 0)

	/** Toggle Double Tap to Wake */
	fun setDoubleTapToWake(cr: ContentResolver, enable: Boolean): Boolean =
		putSecureInt(cr, "doze_pulse_on_double_tap", if (enable) 1 else 0)

	/** Toggle Pick Up to Wake */
	fun setPickUpToWake(cr: ContentResolver, enable: Boolean): Boolean =
		putSecureInt(cr, "doze_pulse_on_pick_up", if (enable) 1 else 0)

	/** Toggle Heads-up notifications */
	fun setHeadsUpEnabled(cr: ContentResolver, enable: Boolean): Boolean =
		putGlobalInt(cr, "heads_up_notifications_enabled", if (enable) 1 else 0)

	/** Pin Wireless ADB port to 5555 */
	fun pinWirelessAdbPort(port: Int = 5555): Pair<Boolean, String> =
		runShell("setprop service.adb.tcp.port $port && stop adbd && start adbd")

	/** Modify display resolution / density */
	fun setDisplayResolution(width: Int, height: Int): Pair<Boolean, String> =
		runShell("wm size ${width}x${height}")

	fun setDisplayDensity(density: Int): Pair<Boolean, String> =
		runShell("wm density $density")

	fun resetDisplay(): Pair<Boolean, String> {
		val r1 = runShell("wm size reset")
		val r2 = runShell("wm density reset")
		return (r1.first && r2.first) to "${r1.second} ${r2.second}".trim()
	}

	// ========================================================================
	// Automatic Preference Change Listener
	// ========================================================================

	/**
	 * Hooked from PrefStore to automatically translate PixelXpert preferences
	 * into active Non-Root system settings.
	 */
	fun onPrefChanged(context: Context, key: String, value: Any?) {
		val cr = context.contentResolver
		try {
			when (key) {
				"pin_wireless_adb" -> {
					if (value == true || value == "true") pinWirelessAdbPort(5555)
				}
				"pinned_adb_port" -> {
					val port = (value as? String)?.toIntOrNull() ?: 5555
					pinWirelessAdbPort(port)
				}
				"doubleTapToWake" -> {
					setDoubleTapToWake(cr, value == true || value == "true")
				}
				"TapToShowAmbient" -> {
					setDoubleTapToWake(cr, value == true || value == "true")
				}
				"PickToShowAmbient" -> {
					setPickUpToWake(cr, value == true || value == "true")
				}
				"ForceAODwCharging" -> {
					setAOD(cr, value == true || value == "true")
				}
				"allScreenRotations" -> {
					val enabled = value == true || value == "true"
					putSystemInt(cr, "accelerometer_rotation", if (enabled) 1 else 0)
				}
				else -> {}
			}
		} catch (t: Throwable) {
			Log.w(TAG, "Failed to apply non-root tweak for key: $key", t)
		}
	}
}
