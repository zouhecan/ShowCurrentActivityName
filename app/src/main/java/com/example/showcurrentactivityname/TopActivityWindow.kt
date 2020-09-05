package com.example.showcurrentactivityname

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView

/**
 * desc: 显示栈顶activity名称的window
 */
class TopActivityWindow(var mContext: Context) {
    private var sWindowParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_TOAST else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        0x18,
        PixelFormat.TRANSLUCENT
    )
    private var sWindowManager: WindowManager =
        mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var sView: View
    private var textView: TextView
    private var isShowing = false

    init {
        sWindowParams.gravity = Gravity.START + Gravity.TOP
        sView = LayoutInflater.from(mContext)
            .inflate(R.layout.debug_top_activity_window, null)
        textView = sView.findViewById(R.id.text)
    }

    fun show(text: String?) {
        if (text.isNullOrEmpty()) {
            return
        }
        textView.text = text
        if (isShowing) {
            return
        }
//        if (ShowTopActivityWindowManager.checkFloatPermission(mContext)) {
//            isShowing = true
//            sWindowManager.addView(sView, sWindowParams)
//        } else {
//            dismiss()
//            ShowTopActivityWindowManager.updateTopActivityWindowStatus(false)
//        }
    }

    fun dismiss() {
        if (!isShowing) {
            return
        }
        sWindowManager.removeView(sView)
        isShowing = false
    }

}