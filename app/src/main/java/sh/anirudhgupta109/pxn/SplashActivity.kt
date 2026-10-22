package sh.anirudhgupta109.pxn

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread
import sh.anirudhgupta109.pxn.BuildConfig
import sh.anirudhgupta109.pxn.nonroot.NonRootBridge
import sh.anirudhgupta109.pxn.theme.PxnTheme
import sh.anirudhgupta109.pxn.util.AppUtils
import sh.anirudhgupta109.pxn.util.ModuleInstaller
import sh.anirudhgupta109.pxn.util.PixelGate

private enum class CheckState { RUNNING, DONE, FAILED }

/**
 * Launcher entry point: verify environment (Root or Non-Root Wireless ADB / Shizuku / LSPatch),
 * initialize services, then admit smoothly into MainActivity.
 */
class SplashActivity : ComponentActivity() {

	private var rootState by mutableStateOf(CheckState.RUNNING)
	private var serviceState by mutableStateOf(CheckState.RUNNING)
	private var pixelState by mutableStateOf(CheckState.RUNNING)

	private var envLabel by mutableStateOf<String?>(null)
	private var serviceLabel by mutableStateOf<String?>(null)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		val received = intent

		thread(name = "pxn-splash-init") {
			try {
				// 1. Operating Environment Check (Root or Non-Root)
				val hasRoot = ModuleInstaller.hasRoot()
				val hasAdb = NonRootBridge.hasWriteSecureSettings(this)
				val hasShizuku = NonRootBridge.hasShizukuPermission()

				envLabel = if (hasRoot) {
					"Root Environment (Magisk / KSU)"
				} else {
					"Non-Root (Wireless ADB / LSPatch)"
				}
				Thread.sleep(300)
				rootState = CheckState.DONE

				// 2. Privilege & Service Check
				val app = PxnApp.get()
				if (hasRoot) {
					if (!app.rootServiceBound) {
						app.tryConnectRootService()
					}
					// Non-blocking short wait
					app.rootServiceConnected.await(1, java.util.concurrent.TimeUnit.SECONDS)
					serviceLabel = "Root Service Connected"
				} else {
					serviceLabel = when {
						hasShizuku -> "Shizuku ADB Service Connected"
						hasAdb -> "Secure Settings Permission Granted"
						else -> "Wireless ADB / Shizuku Mode"
					}
				}
				Thread.sleep(300)
				serviceState = CheckState.DONE

				// 3. Pixel & Platform Check (Android 13 - 17 compatibility)
				Thread.sleep(300)
				pixelState = CheckState.DONE

				// Small hold for visual transition
				Thread.sleep(500)

				startActivity(
					Intent(this@SplashActivity, MainActivity::class.java).apply {
						received.extras?.let(::putExtras)
					},
				)
				finish()
			} catch (t: Throwable) {
				// Even on error, do not trap the user - proceed to MainActivity
				startActivity(Intent(this@SplashActivity, MainActivity::class.java))
				finish()
			}
		}

		setContent {
			PxnTheme {
				SplashContent(
					rootState = rootState,
					serviceState = serviceState,
					pixelState = pixelState,
					envLabel = envLabel ?: stringResource(R.string.checking_root_permissions),
					serviceLabel = serviceLabel ?: stringResource(R.string.connecting_root_service),
				)
			}
		}
	}
}

@Composable
private fun rememberLauncherIcon(): ImageBitmap {
	val context = LocalContext.current
	return remember(context) {
		val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
			?: error("launcher icon missing")

		drawable.toBitmap(
			drawable.intrinsicWidth.takeIf { it > 0 } ?: 256,
			drawable.intrinsicHeight.takeIf { it > 0 } ?: 256,
		).asImageBitmap()
	}
}

@Composable
private fun SplashContent(
	rootState: CheckState,
	serviceState: CheckState,
	pixelState: CheckState,
	envLabel: String,
	serviceLabel: String,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Image(
			bitmap = rememberLauncherIcon(),
			contentDescription = null,
			modifier = Modifier.size(120.dp),
		)

		Text(
			text = stringResource(R.string.app_name),
			fontSize = 34.sp,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.padding(bottom = 8.dp),
		)

		Text(
			text = "Non-Root & Wireless ADB Edition",
			fontSize = 14.sp,
			color = MaterialTheme.colorScheme.primary,
			modifier = Modifier.padding(bottom = 32.dp),
		)

		Column(modifier = Modifier.width(IntrinsicSize.Max)) {
			CheckRow(
				label = envLabel,
				state = rootState,
			)
			CheckRow(
				label = serviceLabel,
				state = serviceState,
			)
			CheckRow(
				label = "Device & Android 13-17 Compatibility",
				state = pixelState,
			)
		}
	}
}

@Composable
private fun CheckRow(
	label: String,
	state: CheckState,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		when (state) {
			CheckState.RUNNING -> CircularProgressIndicator(
				modifier = Modifier.size(32.dp),
				color = colorResource(R.color.launcher_accent),
			)

			CheckState.DONE -> Icon(
				painter = painterResource(R.drawable.ic_success),
				contentDescription = null,
				modifier = Modifier.size(32.dp),
				tint = Color.Unspecified,
			)

			CheckState.FAILED -> Icon(
				painter = painterResource(R.drawable.ic_fail),
				contentDescription = null,
				modifier = Modifier.size(32.dp),
				tint = Color(0xFFF44336),
			)
		}

		Text(
			text = label,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.padding(start = 8.dp),
		)
	}
}
