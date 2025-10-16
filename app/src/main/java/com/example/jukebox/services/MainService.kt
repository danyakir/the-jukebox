package com.example.jukebox.services

import BLECallback
import BLEManager
import android.util.Log
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class MainService : Service() {
    private lateinit var bleManager: BLEManager
    private var callback: MyCallback? = null

    companion object {
        private const val TAG = "MainService"
    }

    interface MyCallback {
        fun onDataReceived(data: Int)
    }

    override fun onCreate() {
        Log.d(TAG, "service is created")
        super.onCreate()
        bleManager = BLEManager(this, object : BLECallback {
            override fun onDataReceived(buttonNumber: Int) {
                Log.d(TAG,"Button $buttonNumber was pressed")
                callback?.onDataReceived(buttonNumber)
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG,"service is started")

        bleManager.startScanning()

        return START_STICKY
    }

    inner class MyBinder : Binder() {
        fun getService(): MainService = this@MainService
    }

    override fun onBind(intent: Intent?): IBinder {
        return MyBinder()
    }

    fun registerCallback(callback: MyCallback) {
        this.callback = callback
    }

    override fun onDestroy() {
        super.onDestroy()
        callback = null
    }
}