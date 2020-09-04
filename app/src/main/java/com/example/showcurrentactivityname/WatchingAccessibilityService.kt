package com.example.showcurrentactivityname

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

internal class WatchingAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            ShowTopActivityWindowManager.window?.show("${event.packageName}\n${event.className}")
        }
    }

    override fun onInterrupt() {}
}