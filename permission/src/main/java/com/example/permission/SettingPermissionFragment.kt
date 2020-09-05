package com.example.permission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

/**
 *  跳转到设置页中操作权限
 */
class SettingPermissionFragment : Fragment() {
    private var callBackMap = HashMap<Int, IPermissionCallBack?>()

    fun request(requestCode: Int, callBack: IPermissionCallBack?) {
        callBackMap[requestCode] = callBack
        when (requestCode) {
            PermissionUtil.REQUEST_CODE_REQUEST_OVERLAY -> {
                requestAlertWindow(requestCode)
            }
            PermissionUtil.REQUEST_CODE_USAGE_STATS -> {
                callBackMap[PermissionUtil.REQUEST_CODE_USAGE_STATS] = callBack
                requestUsageStats(requestCode)
            }
        }
    }

    /**
     * 申请悬浮窗
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestAlertWindow(requestCode: Int) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + context!!.packageName)
        )
        startActivityForResult(intent, requestCode)
    }

    /**
     * 申请应用数据
     */
    private fun requestUsageStats(requestCode: Int) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivityForResult(intent, requestCode)
    }

    /**
     * 设置页返回
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PermissionUtil.REQUEST_CODE_REQUEST_OVERLAY -> {
                callBackMap[requestCode]?.onPermissionsGranted(
                    PermissionUtil.checkFloatPermission(context!!),
                    mutableListOf()
                )
            }
            PermissionUtil.REQUEST_CODE_USAGE_STATS -> {
                callBackMap[requestCode]?.onPermissionsGranted(
                    PermissionUtil.checkUsageStatsPermissionInner(context!!),
                    mutableListOf()
                )
            }
        }
    }
}