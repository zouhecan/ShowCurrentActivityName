package com.example.showcurrentactivityname

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.iosconfirm.IOSConfirm
import kotlinx.android.synthetic.main.activity_main.*

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

    override fun onResume() {
        super.onResume()
        startAccessibilityService()
    }

    private fun startAccessibilityService() {
        ShowTopActivityWindowManager.requestOverlayWindowPermission(
            this,
            IPermissionCallBack { grantedAll, _ ->
                if (grantedAll) {
                    if (isAccessibilitySettingsOn()) {
                        show()
                    } else {
                        val iosConfirm: IOSConfirm = IOSConfirm.Builder(this)
                            .setMessage(resources.getString(R.string.open_accessibility_service))
                            .setPositiveButton(resources.getString(R.string.go_to_grant_permission)) { dialog: DialogInterface, _: Int ->
                                val intent = Intent()
                                intent.action = "android.settings.ACCESSIBILITY_SETTINGS"
                                startActivity(intent)
                                dialog.dismiss()
                            }
                            .setNegativeButton(resources.getString(R.string.cancel)) { dialog: DialogInterface, _: Int ->
                                Toast.makeText(
                                    this,
                                    "" + getString(R.string.open_accessibility_service),
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                            }
                            .createConfirm()
                        iosConfirm.setCancelable(false)
                        iosConfirm.show()
                        dismiss()
                    }
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

    private fun show() {
        switchBtn.isChecked = true
//        ShowTopActivityWindowManager.window?.show(packageName + "\n" + "${this::class.java.canonicalName}")
        ShowTopActivityWindowManager.updateTopActivityWindowStatus(true)
    }

    private fun dismiss() {
        switchBtn.isChecked = false
        ShowTopActivityWindowManager.window?.dismiss()
        ShowTopActivityWindowManager.updateTopActivityWindowStatus(false)
    }

    private fun isAccessibilitySettingsOn(): Boolean {
        var accessibilityEnabled = 0
        val service: String =
            this.packageName + "/" + WatchingAccessibilityService::class.java.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
            Log.v(logTag, "accessibilityEnabled = $accessibilityEnabled")
        } catch (e: Settings.SettingNotFoundException) {
            Log.e(
                logTag, "Error finding setting, default accessibility to not found: "
                        + e.message
            )
        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    Log.v(
                        logTag,
                        "-------------- > accessibilityService :: $accessibilityService $service"
                    )
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        Log.v(
                            logTag,
                            "We've found the correct setting - accessibility is switched on!"
                        )
                        return true
                    }
                }
            }
        } else {
            Log.v(logTag, "***ACCESSIBILITY IS DISABLED***")
        }
        return false
    }

    override fun onDestroy() {
//        ShowTopActivityWindowManager.window?.dismiss()
        super.onDestroy()
        Log.d("zouhecan", "MainActivity onDestroy")
    }
}