package app.rork.quran.ui.screen.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/** Vendor settings vary by OS version. Check actual activities, never just the brand. */
class BackgroundLaunchSettings internal constructor(
    private val context: Context,
    private val candidates: List<ComponentName> = defaultCandidates,
) {
    companion object {
        private val defaultCandidates = listOf(
            "com.hihonor.systemmanager" to "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
            "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
            "com.huawei.systemmanager" to "com.huawei.systemmanager.optimize.process.ProtectActivity",
            "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
            "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
            "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity",
            "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
            "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
            "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager",
            "com.asus.mobilemanager" to "com.asus.mobilemanager.autostart.AutoStartActivity",
            "com.oneplus.security" to "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity",
            "com.samsung.android.lool" to "com.samsung.android.sm.battery.ui.usage.CheckableAppListActivity",
            "com.samsung.android.lool" to "com.samsung.android.sm.ui.battery.BatteryActivity",
            "com.samsung.android.lool" to "com.samsung.android.sm.battery.ui.BatteryActivity",
            "com.evenwell.powersaving.g3" to "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity",
            "com.letv.android.letvsafe" to "com.letv.android.letvsafe.AutobootManageActivity",
        ).map { (pkg, activity) -> ComponentName(pkg, activity) }
    }

    private fun availableIntents(): List<Intent> = candidates.mapNotNull { component ->
        val info = try {
            @Suppress("DEPRECATION")
            context.packageManager.getActivityInfo(component, 0)
        } catch (_: PackageManager.NameNotFoundException) {
            return@mapNotNull null
        } catch (_: SecurityException) {
            return@mapNotNull null
        }
        if (!info.exported || !info.enabled || !info.applicationInfo.enabled ||
            (info.permission != null && context.checkSelfPermission(info.permission) != PackageManager.PERMISSION_GRANTED)) {
            return@mapNotNull null
        }
        Intent().setComponent(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra("package_name", context.packageName)
            .putExtra("packageName", context.packageName)
    }

    fun isAvailable(): Boolean = availableIntents().isNotEmpty()

    fun open(): Boolean {
        for (intent in availableIntents()) {
            try {
                context.startActivity(intent)
                return true
            } catch (_: android.content.ActivityNotFoundException) {
            } catch (_: SecurityException) {
            }
        }
        return false
    }
}
