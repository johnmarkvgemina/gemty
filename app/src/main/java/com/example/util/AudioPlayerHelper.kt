package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.File

class AudioPlayerHelper(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    var currentlyPlayingPath: String? = null
        private set
    var isAudioPlayingState: Boolean = false
        private set

    fun playAudio(filePath: String, onCompletion: () -> Unit) {
        stopAudio()

        val file = File(filePath)
        if (!file.exists()) {
            Log.w("AudioPlayer", "Audio file does not exist: $filePath")
            return
        }

        try {
            val player = MediaPlayer()
            player.setDataSource(filePath)
            player.prepare()
            player.setOnCompletionListener {
                this@AudioPlayerHelper.isAudioPlayingState = false
                this@AudioPlayerHelper.currentlyPlayingPath = null
                onCompletion()
            }
            player.start()
            mediaPlayer = player
            isAudioPlayingState = true
            currentlyPlayingPath = filePath
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error playing audio file", e)
            stopAudio()
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
            isAudioPlayingState = false
            currentlyPlayingPath = null
        }
    }
}
