package com.example.showcurrentactivityname

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
    private var mCallBack: IPermissionCallBack? = null

    /**
     * 申请悬浮窗
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun requestAlertWindow(callBack: IPermissionCallBack?) {
        this.mCallBack = callBack
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + context!!.packageName)
        )
        startActivityForResult(intent, 101)
    }

    /**
     * 设置页返回
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            101 -> {
                mCallBack?.onPermissionsGranted(
                    ShowTopActivityWindowManager.checkFloatPermission(context!!),
                    mutableListOf()
                )
            }
        }
    }
}