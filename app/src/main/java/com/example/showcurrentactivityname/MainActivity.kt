package com.example.showcurrentactivityname

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.permission.IPermissionCallBack
import com.example.permission.PermissionUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private val logTag = MainActivity::class.java.canonicalName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ShowTopActivityWindowManager.window = TopActivityWindow(this)
        switchBtn.isChecked = false
        content.setOnClickListener {
            if (switchBtn.isChecked) {
                dismiss()
            } else {
                startAccessibilityService()
            }
        }
    }

    private var isRequesting = false

    override fun onResume() {
        super.onResume()
        if (isRequesting) {
            return
        }
        startAccessibilityService()
    }

    private fun startAccessibilityService() {
        isRequesting = true
        PermissionUtil.requestOverlayWindowPermission(
            this,
            IPermissionCallBack { grantedAll, _ ->
                if (grantedAll) {
                    requestUsageStats()
                } else {
                    dismiss()
                    Toast.makeText(
                        this,
                        getString(R.string.popup_window_permission_tips) + getString(R.string.app_name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun requestUsageStats() {
        PermissionUtil.requestUsageStatsPermission(this, IPermissionCallBack { grantedAll, _ ->
            if (grantedAll) {
                show()
            } else {
                dismiss()
                Toast.makeText(
                    this,
                    getString(R.string.open_usage_stats_permission_tips) + getString(R.string.app_name),
                    Toast.LENGTH_SHORT
                ).show()
            }
            isRequesting = false
        })
    }

    private fun show() {
        switchBtn.isChecked = true
        startService(Intent(this, WatchingService::class.java))
        ShowTopActivityWindowManager.updateTopActivityWindowStatus(true)
    }

    private fun dismiss() {
        switchBtn.isChecked = false
        ShowTopActivityWindowManager.window?.dismiss()
        ShowTopActivityWindowManager.updateTopActivityWindowStatus(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        exitProcess(-1)
    }
}