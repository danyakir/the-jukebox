package com.example.jukebox

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

class MusicManager(private val context: Context, private val onSongEnded: () -> Unit) {
    private var player: ExoPlayer? = null

    fun playRandomSong(genreOrdinal: Int) {
        stop()
        val musicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "JukeBox/$genreOrdinal")
        val songs = musicDir.listFiles { file -> file.extension == "mp3" }

        requestStoragePermission(context)

        if (!songs.isNullOrEmpty()) {
            val randomSong = songs.random()
            val mediaItem = MediaItem.fromUri(randomSong.toUri())

            player = ExoPlayer.Builder(context).build()
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.play()

            player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        onSongEnded()
                    }
                }
            })
        }
    }

    fun getMusicFiles(context: Context): List<String> {
        val musicFiles = mutableListOf<String>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA)

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val filePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                musicFiles.add(filePath)
            }
        }
        return musicFiles
    }

    private fun stop() {
        player?.apply {
            stop()
            release()
        }
        player = null
    }

    private fun requestStoragePermission(context: Context) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + context.packageName)
                context.startActivity(intent)
        }
    }
}

