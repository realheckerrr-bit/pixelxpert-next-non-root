package sh.anirudhgupta109.pxn.ui.nonroot

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sh.anirudhgupta109.pxn.R
import sh.anirudhgupta109.pxn.nonroot.NonRootBridge

@Composable
fun NonRootScreen() {
	val context = LocalContext.current
	val cr = context.contentResolver

	var hasSecureSettings by remember { mutableStateOf(NonRootBridge.hasWriteSecureSettings(context)) }
	var isShizukuReady by remember { mutableStateOf(NonRootBridge.hasShizukuPermission()) }
	var isShizukuInstalled by remember { mutableStateOf(NonRootBridge.isShizukuAvailable()) }

	// Status bar blacklisted icons state
	var blacklistedIcons by remember { mutableStateOf(NonRootBridge.getBlacklistedIcons(cr)) }

	// Display & animation states
	var animationScale by remember { mutableFloatStateOf(1.0f) }
	var refreshRate by remember { mutableFloatStateOf(120f) }

	fun refreshStatus() {
		hasSecureSettings = NonRootBridge.hasWriteSecureSettings(context)
		isShizukuReady = NonRootBridge.hasShizukuPermission()
		isShizukuInstalled = NonRootBridge.isShizukuAvailable()
		blacklistedIcons = NonRootBridge.getBlacklistedIcons(cr)
	}

	LaunchedEffect(Unit) {
		refreshStatus()
	}

	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
	) {
		item { Spacer(modifier = Modifier.height(8.dp)) }

		// Title Card
		item {
			Card(
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer,
				),
				shape = RoundedCornerShape(20.dp),
				modifier = Modifier.fillMaxWidth(),
			) {
				Column(modifier = Modifier.padding(18.dp)) {
					Text(
						text = "⚡ Non-Root & Wireless ADB Suite",
						style = MaterialTheme.typography.titleLarge,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onPrimaryContainer,
					)
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = "Customize status bar icons, refresh rate, animations, gestures, and SystemUI on any Pixel (Android 13 to 17) without root access.",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
					)
				}
			}
		}

		// Status & Authorization Cards
		item {
			Text(
				text = "CONNECTION & PERMISSIONS",
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.primary,
				fontWeight = FontWeight.SemiBold,
			)
			Spacer(modifier = Modifier.height(8.dp))

			Card(
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.surfaceVariant,
				),
				shape = RoundedCornerShape(16.dp),
				modifier = Modifier.fillMaxWidth(),
			) {
				Column(modifier = Modifier.padding(16.dp)) {
					// WRITE_SECURE_SETTINGS Row
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.fillMaxWidth(),
					) {
						Icon(
							imageVector = if (hasSecureSettings) Icons.Default.CheckCircle else Icons.Default.Warning,
							contentDescription = null,
							tint = if (hasSecureSettings) Color(0xFF4CAF50) else Color(0xFFFFA000),
							modifier = Modifier.size(24.dp),
						)
						Spacer(modifier = Modifier.width(12.dp))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "WRITE_SECURE_SETTINGS",
								style = MaterialTheme.typography.titleSmall,
								fontWeight = FontWeight.Bold,
							)
							Text(
								text = if (hasSecureSettings) "Granted via ADB / Shizuku" else "Not granted yet",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
						}
					}

					HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

					// Shizuku ADB Service Row
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.fillMaxWidth(),
					) {
						Icon(
							imageVector = if (isShizukuReady) Icons.Default.CheckCircle else Icons.Default.Info,
							contentDescription = null,
							tint = if (isShizukuReady) Color(0xFF4CAF50) else Color(0xFF2196F3),
							modifier = Modifier.size(24.dp),
						)
						Spacer(modifier = Modifier.width(12.dp))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "Shizuku ADB Service",
								style = MaterialTheme.typography.titleSmall,
								fontWeight = FontWeight.Bold,
							)
							Text(
								text = when {
									isShizukuReady -> "Authorized and active (Shell UID 2000)"
									isShizukuInstalled -> "Installed: Tap to authorize"
									else -> "Optional: Run Wireless ADB or install Shizuku"
								},
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
						}
						if (!isShizukuReady && isShizukuInstalled) {
							Button(
								onClick = {
									NonRootBridge.requestShizukuPermission()
									refreshStatus()
								},
								contentPadding = ButtonDefaults.TextButtonContentPadding,
							) {
								Text("Authorize")
							}
						}
					}
				}
			}
		}

		// Wireless ADB Setup Guide (if not granted)
		if (!hasSecureSettings && !isShizukuReady) {
			item {
				Card(
					colors = CardDefaults.cardColors(
						containerColor = MaterialTheme.colorScheme.secondaryContainer,
					),
					shape = RoundedCornerShape(16.dp),
					modifier = Modifier.fillMaxWidth(),
				) {
					Column(modifier = Modifier.padding(16.dp)) {
						Text(
							text = "Quick Wireless ADB Setup",
							style = MaterialTheme.typography.titleSmall,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onSecondaryContainer,
						)
						Spacer(modifier = Modifier.height(4.dp))
						Text(
							text = "Run this command once in Wireless ADB or computer terminal:",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSecondaryContainer,
						)
						Spacer(modifier = Modifier.height(8.dp))

						val cmd = "adb shell pm grant sh.anirudhgupta109.pxn android.permission.WRITE_SECURE_SETTINGS"
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.clip(RoundedCornerShape(8.dp))
								.background(MaterialTheme.colorScheme.surface)
								.padding(10.dp),
						) {
							Text(
								text = cmd,
								fontFamily = FontFamily.Monospace,
								fontSize = 12.sp,
								color = MaterialTheme.colorScheme.onSurface,
							)
						}

						Spacer(modifier = Modifier.height(8.dp))
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End,
						) {
							OutlinedButton(
								onClick = {
									val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
									clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", cmd))
									Toast.makeText(context, "Command copied to clipboard!", Toast.LENGTH_SHORT).show()
								},
							) {
								Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
								Spacer(modifier = Modifier.width(6.dp))
								Text("Copy Command")
							}
							Spacer(modifier = Modifier.width(8.dp))
							Button(onClick = { refreshStatus() }) {
								Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
								Spacer(modifier = Modifier.width(6.dp))
								Text("Verify")
							}
						}
					}
				}
			}
		}

		// Status Bar Icon Manager (Fully Functional Non-Root!)
		item {
			Text(
				text = "STATUS BAR ICONS (SYSTEMUI)",
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.primary,
				fontWeight = FontWeight.SemiBold,
			)
			Spacer(modifier = Modifier.height(8.dp))

			Card(
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.surface,
				),
				shape = RoundedCornerShape(16.dp),
				modifier = Modifier.fillMaxWidth(),
			) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text(
						text = "Toggle visibility for individual status bar icons:",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
					Spacer(modifier = Modifier.height(12.dp))

					NonRootBridge.KNOWN_ICON_SLOTS.forEach { (slot, title) ->
						val isHidden = blacklistedIcons.contains(slot)
						Row(
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.SpaceBetween,
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 6.dp),
						) {
							Text(
								text = title,
								style = MaterialTheme.typography.bodyMedium,
								fontWeight = FontWeight.Medium,
							)
							Switch(
								checked = !isHidden,
								onCheckedChange = { show ->
									NonRootBridge.setIconHidden(cr, slot, !show)
									blacklistedIcons = NonRootBridge.getBlacklistedIcons(cr)
								},
							)
						}
					}
				}
			}
		}

		// Refresh Rate & Animations
		item {
			Text(
				text = "DISPLAY & PERFORMANCE",
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.primary,
				fontWeight = FontWeight.SemiBold,
			)
			Spacer(modifier = Modifier.height(8.dp))

			Card(
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.surface,
				),
				shape = RoundedCornerShape(16.dp),
				modifier = Modifier.fillMaxWidth(),
			) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text(
						text = "Force Peak Refresh Rate",
						style = MaterialTheme.typography.titleSmall,
						fontWeight = FontWeight.Bold,
					)
					Spacer(modifier = Modifier.height(8.dp))
					Row(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						modifier = Modifier.fillMaxWidth(),
					) {
						listOf(60f to "60 Hz", 90f to "90 Hz", 120f to "120 Hz").forEach { (hz, label) ->
							FilterChip(
								selected = refreshRate == hz,
								onClick = {
									refreshRate = hz
									NonRootBridge.setRefreshRate(cr, hz)
									Toast.makeText(context, "Refresh rate set to $label", Toast.LENGTH_SHORT).show()
								},
								label = { Text(label) },
							)
						}
					}

					HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

					Text(
						text = "System Animation Speed",
						style = MaterialTheme.typography.titleSmall,
						fontWeight = FontWeight.Bold,
					)
					Spacer(modifier = Modifier.height(8.dp))
					Row(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						modifier = Modifier.fillMaxWidth(),
					) {
						listOf(
							0.0f to "Instant (0x)",
							0.5f to "Fast (0.5x)",
							1.0f to "Normal (1x)",
						).forEach { (scale, label) ->
							FilterChip(
								selected = animationScale == scale,
								onClick = {
									animationScale = scale
									NonRootBridge.setAnimationScales(cr, scale)
									Toast.makeText(context, "Animation scale set to $label", Toast.LENGTH_SHORT).show()
								},
								label = { Text(label) },
							)
						}
					}
				}
			}
		}

		// Bootleggers Style Block Header (Non-Root / ADB & LSPatch)
		item {
			Text(
				text = "BOOTLEGGERS STYLE QS BLOCK HEADER",
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.primary,
				fontWeight = FontWeight.SemiBold,
			)
			Spacer(modifier = Modifier.height(8.dp))

			Card(
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.surface,
				),
				shape = RoundedCornerShape(16.dp),
				modifier = Modifier.fillMaxWidth(),
			) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text(
						text = "Block Header Preview & Style",
						style = MaterialTheme.typography.titleSmall,
						fontWeight = FontWeight.Bold,
					)
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = "Styled QS header card inspired by Bootleggers ROM with custom titles, pill rounding, and accent borders.",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
					Spacer(modifier = Modifier.height(12.dp))

					// Live Preview of Bootleggers Block Header
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.background(
								color = MaterialTheme.colorScheme.surfaceVariant,
								shape = RoundedCornerShape(16.dp)
							)
							.padding(16.dp),
					) {
						Column(
							horizontalAlignment = Alignment.CenterHorizontally,
							modifier = Modifier.fillMaxWidth()
						) {
							Box(
								modifier = Modifier
									.background(
										color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
										shape = RoundedCornerShape(12.dp)
									)
									.padding(horizontal = 20.dp, vertical = 8.dp)
							) {
								Text(
									text = "★ BOOTLEGGERS ★",
									color = MaterialTheme.colorScheme.onPrimary,
									fontWeight = FontWeight.Black,
									letterSpacing = 1.5.sp,
									style = MaterialTheme.typography.titleMedium,
								)
							}
							Spacer(modifier = Modifier.height(8.dp))
							Text(
								text = "Non-Root & LSPatch Ready (Pixels A13 - A17)",
								style = MaterialTheme.typography.labelSmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
						}
					}
				}
			}
		}

		// Actions & SystemUI Control
		item {
			Text(
				text = "SYSTEMUI & ADB UTILITIES",
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.primary,
				fontWeight = FontWeight.SemiBold,
			)
			Spacer(modifier = Modifier.height(8.dp))

			Card(
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.surface,
				),
				shape = RoundedCornerShape(16.dp),
				modifier = Modifier.fillMaxWidth(),
			) {
				Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
					Button(
						onClick = {
							val ok = NonRootBridge.restartSystemUI(context)
							Toast.makeText(
								context,
								if (ok) "SystemUI restarting via ADB..." else "SystemUI restart sent",
								Toast.LENGTH_SHORT,
							).show()
						},
						modifier = Modifier.fillMaxWidth(),
					) {
						Icon(Icons.Default.Refresh, contentDescription = null)
						Spacer(modifier = Modifier.width(8.dp))
						Text("Restart SystemUI via ADB")
					}

					OutlinedButton(
						onClick = {
							val (ok, out) = NonRootBridge.pinWirelessAdbPort(5555)
							Toast.makeText(
								context,
								if (ok) "Wireless ADB pinned to port 5555" else "Result: $out",
								Toast.LENGTH_SHORT,
							).show()
						},
						modifier = Modifier.fillMaxWidth(),
					) {
						Text("Pin Wireless ADB to Port 5555")
					}
				}
			}
		}

		item { Spacer(modifier = Modifier.height(32.dp)) }
	}
}
