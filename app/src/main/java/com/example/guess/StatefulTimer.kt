package com.example.guess

import android.os.CountDownTimer

private const val TAG = "StatefulTimer"

abstract class StatefulTimer(duration: Int) {
    enum class States { STOPPED, RUNNING }

    private var state = States.STOPPED
    abstract fun onTick(secondsUntilFinished: Int)
    abstract fun onFinished()
    private val timer = object : CountDownTimer((duration * 1000).toLong(), 1000) {
        override fun onTick(p0: Long) {
            onTick(((p0 / 1000) + 1).toInt())
        }

        override fun onFinish() {
            Log().i(TAG, "StatefulTimer finished")
            onFinished()
            state = States.STOPPED
        }
    }

    fun getState(): States {
        return state
    }

    fun start() {
        timer.cancel()
        timer.start()
        state = States.RUNNING
        Log().i(TAG, "StatefulTimer started")
    }

    fun cancel() {
        timer.cancel()
        state = States.STOPPED
        Log().i(TAG, "StatefulTimer canceled")
    }
}