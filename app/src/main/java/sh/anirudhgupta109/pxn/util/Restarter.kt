package sh.anirudhgupta109.pxn.util

import com.topjohnwu.superuser.Shell
import sh.anirudhgupta109.pxn.nonroot.NonRootBridge

/**
 * Restart utilities supporting both Root and Non-Root Wireless ADB / Shizuku.
 */
object Restarter {

	/**
	 * Restart SystemUI. Uses NonRootBridge (via Shizuku/ADB) or falls back to root shell.
	 */
	fun restartSystemUI(): Boolean {
		return NonRootBridge.restartSystemUI()
	}

	/**
	 * Soft reboot: restart zygote runtime.
	 */
	fun softRestart() {
		val (ok, _) = NonRootBridge.runShell("setprop ctl.restart zygote || kill $(pidof zygote)")
		if (!ok) {
			Shell.cmd("kill $(pidof zygote)").submit()
			Shell.cmd("kill $(pidof zygote64)").submit()
		}
	}

	/**
	 * Full restart, via intent or adb reboot.
	 */
	fun restartSystem() {
		NonRootBridge.runShell("svc power reboot || am start -a android.intent.action.REBOOT")
		Shell.cmd("am start -a android.intent.action.REBOOT").exec()
	}
}
