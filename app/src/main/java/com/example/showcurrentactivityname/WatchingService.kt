package com.example.showcurrentactivityname

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import java.util.*

class WatchingService : Service() {
    private val mHandler = Handler()
    private var mActivityManager: ActivityManager? = null
    private var timer: Timer? = null
    private val logTag = "zouhecan"

    override fun onCreate() {
        super.onCreate()
        mActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(logTag, "Watching Service start")
        if (timer == null) {
            timer = Timer()
            timer!!.scheduleAtFixedRate(RefreshTask(), 0, 500)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onTaskRemoved(rootIntent: Intent) {
        Log.d(logTag, ServiceInfo.FLAG_STOP_WITH_TASK.toString() + "")
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext, 1, restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService[AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500] = restartServicePendingIntent
        super.onTaskRemoved(rootIntent)
    }

    internal inner class RefreshTask : TimerTask() {
        override fun run() {
            val name = getCurrentActivityName()
            if (name.isNullOrEmpty()) {
                return
            }
            Log.i(logTag, "top running app is : $name")
            mHandler.post {
               MainActivity.topActivityWindow?.show(name)
            }
        }
    }

    private fun getCurrentActivityName(): String? {
        var topActivity = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val mUsageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val events = mUsageStatsManager.queryEvents(now - 1000, now)
            while (events.hasNextEvent()) {
                val event = UsageEvents.Event()
                events.getNextEvent(event)
                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        topActivity = "${event.packageName}\n${event.className}"
                    }
                }
            }
        } else {
            val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val forGroundActivity = activityManager.getRunningTasks(1)
            topActivity = forGroundActivity[0].topActivity!!.packageName + "\n" + forGroundActivity[0].topActivity!!.className
        }
        return topActivity
    }
}