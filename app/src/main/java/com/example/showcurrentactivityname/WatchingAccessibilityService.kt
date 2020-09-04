package com.example.showcurrentactivityname

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.lang.StringBuilder

internal class WatchingAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Log.d("event", event.toString())
            ShowTopActivityWindowManager.window?.show(getInfo(event))
        }
    }

    private var currentActivityName: String? = null
    private var currentViewName: String? = null

    private fun getInfo(event: AccessibilityEvent): String {
        if (event.className.isNullOrEmpty()) {
            return event.packageName.toString()
        }
        if (isActivity(event)) {
            currentActivityName = event.className.toString()
            currentViewName = null
        } else {
            currentViewName = event.className.toString()
        }
        return if (currentViewName.isNullOrEmpty()) {
            "${event.packageName}\n$currentActivityName"
        } else {
            "${event.packageName}\n$currentActivityName\n$currentViewName"
        }
    }


    private fun isActivity(event: AccessibilityEvent): Boolean {
        val component = ComponentName(event.packageName.toString(), event.className.toString())
        return try {
            packageManager.getActivityInfo(component, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onInterrupt() {
    }

}