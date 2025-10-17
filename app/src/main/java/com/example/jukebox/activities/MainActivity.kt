package com.example.jukebox.activities

import LightsManager
import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.example.jukebox.MusicManager
import com.example.jukebox.R
import com.example.jukebox.services.MainService

class MainActivity : AppCompatActivity() {
    private var myService: MainService? = null
    private var isBound = false


    private val lightsManager = LightsManager(this)
    private val musicManager = MusicManager(this) {
        genreLiveData.postValue(null)
    }
    private val genreLiveData = MutableLiveData<Genre?>()

    private companion object {
        const val PERMISSION_REQUEST_CODE = 1
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        const val TAG = "MainActivity"
    }

    private enum class Genre(@StringRes val genreName: Int, @ColorRes val color: Int, @DrawableRes val image: Int) {
        ROCK(R.string.rock, R.color.blue_rock, R.drawable.rock),
        POP(R.string.pop, R.color.purple_pop, R.drawable.pop),
        HIP_HOP(R.string.hip_hop, R.color.yellow_hip_hop, R.drawable.hip_hop),
        TECHNO(R.string.techno, R.color.green_techno, R.drawable.techno),
        DISCO(R.string.disco, R.color.orange_disco, R.drawable.disco),
        SLOW(R.string.slow, R.color.red_slow, R.drawable.slow);

        fun getGenreName(context: Context): String {
            return context.getString(genreName)
        }
        fun getGenreColor(context: Context): Int {
            return context.getColor(color)
        }
        fun getGenreDrawable(context: Context): Int {
            return image
        }
    }

    private val callback = object : MainService.MyCallback {
        override fun onDataReceived(data: Int) {
            genreLiveData.postValue(Genre.entries[data])
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MainService.MyBinder
            myService = binder.getService()
            isBound = true
            myService?.registerCallback(callback)
            Log.d(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            myService = null
            Log.d(TAG, "Service disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissionsIfNeeded()
        setAppState(null)
        hideSystemBars()
    }



    override fun onStart() {
        super.onStart()
        Log.d(TAG,"binding service")
        val intent = Intent(this, MainService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        startService(intent)
        genreLiveData.distinctUntilChanged().observe(this) { newGenre ->
            setAppState(newGenre)
        }
        lightsManager.connect()
    }

    private fun setAppState(genre: Genre?) {
        if(genre != null) {
            setUi(genre.getGenreDrawable(this))
            musicManager.playRandomSong(genre.ordinal)
            lightsManager.changeLightsColor(genre.ordinal)
        } else {
            setUi(null)
            lightsManager.changeLightsColor(null)
        }
    }

    private fun setUi(imageId: Int?) {
        val textView: TextView = findViewById(R.id.genreText)
        val layout: ConstraintLayout = findViewById(R.id.genreLayout)
        val imageBackground: ImageView = findViewById(R.id.genreImage)

        if (imageId != null) {
            textView.visibility = View.GONE
            imageBackground.visibility = View.VISIBLE
            imageBackground.setImageResource(imageId)
        } else {
            imageBackground.visibility = View.GONE
            textView.visibility = View.VISIBLE
            layout.setBackgroundColor(getColor(R.color.grey_choose))
        }
    }

    private fun requestPermissionsIfNeeded() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.i(TAG, "All permissions granted!")
            } else {
                Log.i(TAG, "Permissions denied! BLE features might not work.")
            }
        }
    }

    private fun hideSystemBars() {
        window.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
