package com.example.showcurrentactivityname

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.ActivityManager.RunningTaskInfo
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
import androidx.annotation.RequiresApi
import java.util.*

class WatchingService : Service() {
    private val mHandler = Handler()
    private var mActivityManager: ActivityManager? = null
    private val text: String? = null
    private var timer: Timer? = null
    private val TAG = "zouhecan"

    override fun onCreate() {
        super.onCreate()
        mActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "Watching Service start")
        if (timer == null) {
            timer = Timer()
            timer!!.scheduleAtFixedRate(RefreshTask(), 0, 500)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onTaskRemoved(rootIntent: Intent) {
        Log.d(TAG, ServiceInfo.FLAG_STOP_WITH_TASK.toString() + "")
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
            Log.d(TAG, "getCurrentActivityName = $name")
            //            List<RunningTaskInfo> rtis = mActivityManager.getRunningTasks(5);
//            String act = rtis.get(0).topActivity.getPackageName() + "\n"
//                    + rtis.get(0).topActivity.getClassName();
//            if (!act.equals(text)) {
//                text = act;
//                Log.d("zouhecan", "Watching " + act);
////                if (SPHelper.isShowWindow(WatchingService.this)) {
////
////                    mHandler.post(new Runnable() {
////                        @Override
////                        public void run() {
////                            TasksWindow.show(WatchingService.this, text);
////                        }
////                    });
////            }
//            }
        }
    }

    private fun getCurrentActivityName(): String {
        var topActivity = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val mUsageStatsManager: UsageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            var str1: String? = ""
            var str2 = ""
            val now = System.currentTimeMillis()
            val events = mUsageStatsManager.queryEvents(now - 1000, now)
            while (events.hasNextEvent()) {
                val event = UsageEvents.Event()
                events.getNextEvent(event)
                when (event.eventType) {
                    1 -> {
                        str1 = event.packageName
                        str2 = event.className
                        topActivity = """
                                $str1
                                $str2
                                """.trimIndent()
                    }
                    2 -> if (event.packageName == str1) str1 = null
                }
            }
        } else {
            val activityManager =
                applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val forGroundActivity =
                activityManager.getRunningTasks(1)
            val currentActivity: RunningTaskInfo
            currentActivity = forGroundActivity[0]
            topActivity = currentActivity.topActivity!!.packageName
        }
        Log.i(TAG, "top running app is : $topActivity")
        return topActivity
    }
}