package com.example.guess

class Log {
    private val enableLog = BuildConfig.DEBUG
    fun i(tag: String, msg: String) {
        if (enableLog) {
            android.util.Log.i(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (enableLog) {
            android.util.Log.e(tag, msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (enableLog) {
            android.util.Log.d(tag, msg)
        }
    }

    fun v(tag: String, msg: String) {
        if (enableLog) {
            android.util.Log.v(tag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        if (enableLog) {
            android.util.Log.w(tag, msg)
        }
    }
}