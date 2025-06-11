package com.a5starcompany.sprintchecksdk.util

import android.util.Log
import com.a5starcompany.sprintchecksdk.BuildConfig

class Logger {
    val DEBUG: Boolean = BuildConfig.DEBUG

    fun d(tag: String?, message: String?) {
        if (DEBUG) {
            Log.d(tag, message.toString())
        }
    }

    fun e(tag: String?, message: String?, tr: Throwable? ) {
        if (DEBUG) {
            Log.e(tag, message.toString(), tr)
        }
    }

    fun i(tag: String?, message: String?) {
        if (DEBUG) {
            Log.i(tag, message.toString())
        }
    }

    fun v(tag: String?, message: String?) {
        if (DEBUG) {
            Log.v(tag, message.toString())
        }
    }

    fun w(tag: String?, message: String?) {
        if (DEBUG) {
            Log.w(tag, message.toString())
        }
    }

    fun println(priority: Int, tag: String?, message: String?) {
        if (DEBUG) {
            Log.println(priority,tag, message.toString())
        }
    }
}