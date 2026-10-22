package sh.anirudhgupta109.pxn.xposed.annotations

import sh.anirudhgupta109.pxn.Constants
import sh.anirudhgupta109.pxn.annotations.BaseModPack

/**
 * Applies to the Settings app.
 *
 * This carries [@BaseModPack][BaseModPack], which is how :modPackProcessor
 * finds every class annotated with this one and registers it for that
 * package.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@BaseModPack(targetPackage = Constants.SETTINGS_PACKAGE)
annotation class SettingsModPack
