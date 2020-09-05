package com.example.showcurrentactivityname

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

/**
 * desc: 显示栈顶activity名称的window manager
 */
object ShowTopActivityWindowManager {

    var window: TopActivityWindow? = null

    var openTopActivityWindow = false

    @JvmStatic
    fun updateTopActivityWindowStatus(enable: Boolean) {
        openTopActivityWindow = enable
    }

}