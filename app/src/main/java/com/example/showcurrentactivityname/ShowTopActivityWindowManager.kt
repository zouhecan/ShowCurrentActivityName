package com.example.showcurrentactivityname

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Binder
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import com.example.iosconfirm.IOSConfirm

/**
 * desc: 显示栈顶activity名称的window manager
 */
object ShowTopActivityWindowManager {

    var window: TopActivityWindow? = null

    var openTopActivityWindow = false

    var sharePreference: SharedPreferences? = null

    //存储是否开启状态的sp
    private const val IS_TOP_ACTIVITY_WINDOW_ENABLE = "show_top_activity_window_enable"

    fun checkTopActivityWindowStatus() {
        openTopActivityWindow = sharePreference!!.getBoolean(
            IS_TOP_ACTIVITY_WINDOW_ENABLE,
            false
        )
    }

    @JvmStatic
    fun updateTopActivityWindowStatus(enable: Boolean) {
        openTopActivityWindow = enable
        val editor = sharePreference!!.edit()
        editor.putBoolean(IS_TOP_ACTIVITY_WINDOW_ENABLE, enable)
        editor.apply()
    }

    /**
     * 申请悬浮窗权限
     */
    @JvmStatic
    fun requestOverlayWindowPermission(
        activity: Activity?,
        callBack: IPermissionCallBack?,
        tip: String? = null
    ) {
        if (activity == null || activity.isFinishing) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(activity)) {
            callBack?.onPermissionsGranted(true, mutableListOf())
            return
        }
        val iosConfirm: IOSConfirm = IOSConfirm.Builder(activity)
            .setMessage(if (tip.isNullOrEmpty()) activity.resources.getString(R.string.popup_window_permission_tips) else tip)
            .setPositiveButton(activity.resources.getString(R.string.go_to_grant_permission)) { dialog: DialogInterface, _: Int ->
                val fragmentTag = "OverlayPermissionFragment"
                val fragment: SettingPermissionFragment
                val fragmentManager = (activity as FragmentActivity).supportFragmentManager
                if (fragmentManager.findFragmentByTag(fragmentTag) != null) {
                    fragment =
                        fragmentManager.findFragmentByTag(fragmentTag) as SettingPermissionFragment
                } else {
                    fragment = SettingPermissionFragment()
                    fragmentManager.beginTransaction().add(fragment, fragmentTag)
                        .commitAllowingStateLoss()
                    fragmentManager.executePendingTransactions()
                }
                fragment.requestAlertWindow(callBack)
                dialog.dismiss()
            }
            .setNegativeButton(activity.resources.getString(R.string.cancel)) { dialog: DialogInterface, _: Int ->
                callBack?.onPermissionsGranted(false, mutableListOf())
                dialog.dismiss()
            }
            .createConfirm()
        iosConfirm.setCancelable(false)
        iosConfirm.show()
    }

    /**
     * 检查悬浮窗权限
     */
    @JvmStatic
    fun checkFloatPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                var cls = Class.forName("android.content.Context")
                val declaredField = cls.getDeclaredField("APP_OPS_SERVICE")
                declaredField.isAccessible = true
                var obj: Any? = declaredField[cls] as? String ?: return false
                val str2 = obj as String
                obj = cls.getMethod("getSystemService", String::class.java).invoke(context, str2)
                cls = Class.forName("android.app.AppOpsManager")
                val declaredField2 = cls.getDeclaredField("MODE_ALLOWED")
                declaredField2.isAccessible = true
                val checkOp =
                    cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String::class.java)
                val result =
                    checkOp.invoke(obj, 24, Binder.getCallingUid(), context.packageName) as Int
                result == declaredField2.getInt(cls)
            } catch (e: Exception) {
                false
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                Settings.canDrawOverlays(context)
            } else {
                val appOpsMgr = context.getSystemService(Context.APP_OPS_SERVICE)
                    ?: return false
                val mode = (appOpsMgr as AppOpsManager).checkOpNoThrow(
                    "android:system_alert_window", Process.myUid(), context
                        .packageName
                )
                mode == AppOpsManager.MODE_ALLOWED
            }
        }
    }

}