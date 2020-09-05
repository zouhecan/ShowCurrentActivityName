package com.example.permission

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.DialogInterface
import android.os.Binder
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import com.example.iosconfirm.IOSConfirm

class PermissionUtil {
    companion object {
        const val REQUEST_CODE_REQUEST_OVERLAY = 101
        const val REQUEST_CODE_USAGE_STATS = 102

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
                    requestByFragment(activity, REQUEST_CODE_REQUEST_OVERLAY, callBack)
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
         * 申请应用数据
         */
        fun requestUsageStatsPermission(activity: Activity?, callBack: IPermissionCallBack?) {
            if (activity == null || activity.isFinishing) {
                return
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1 || checkUsageStatsPermissionInner(activity)) {
                callBack?.onPermissionsGranted(true, mutableListOf())
                return
            }
            val iosConfirm: IOSConfirm = IOSConfirm.Builder(activity)
                .setMessage(activity.resources.getString(R.string.open_usage_stats_permission_tips))
                .setPositiveButton(activity.resources.getString(R.string.go_to_grant_permission)) { dialog: DialogInterface, _: Int ->
                    requestByFragment(activity, REQUEST_CODE_USAGE_STATS, callBack)
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
                    obj =
                        cls.getMethod("getSystemService", String::class.java).invoke(context, str2)
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

        /**
         * 检查访问应用数据权限
         */
        fun checkUsageStatsPermissionInner(context: Context): Boolean {
            context.getSystemService(Context.APP_OPS_SERVICE)?.let {
                val appOps = it as AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    "android:get_usage_stats",
                    Process.myUid(),
                    context.packageName
                )
                return mode == AppOpsManager.MODE_ALLOWED
            }
            return false
        }

        /**
         * 通过fragment发起请求
         */
        private fun requestByFragment(
            activity: Activity,
            requestCode: Int,
            callBack: IPermissionCallBack?
        ) {
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
            fragment.request(requestCode, callBack)

        }
    }
}

