package com.example.showcurrentactivityname

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