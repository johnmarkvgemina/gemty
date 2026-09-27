package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorderHelper(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    var currentOutputFile: File? = null
        private set

    var isRecording: Boolean = false
        private set

    fun startRecording(): File? {
        if (isRecording) return currentOutputFile

        val audioDir = File(context.filesDir, "research_recordings")
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }

        val file = File(audioDir, "research_oral_log_${System.currentTimeMillis()}.m4a")
        currentOutputFile = file

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
                isRecording = true
                Log.d("AudioRecorder", "Recording started: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Failed to start audio recording", e)
                release()
                currentOutputFile = null
                isRecording = false
                return null
            }
        }

        return currentOutputFile
    }

    fun stopRecording(): File? {
        if (!isRecording) return currentOutputFile
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping recording", e)
        } finally {
            mediaRecorder = null
            isRecording = false
        }
        return currentOutputFile
    }

    fun cancelRecording() {
        if (isRecording) {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            } catch (e: Exception) {
                // ignore
            } finally {
                mediaRecorder = null
                isRecording = false
            }
        }
        currentOutputFile?.let {
            if (it.exists()) it.delete()
        }
        currentOutputFile = null
    }

    fun getMaxAmplitude(): Int {
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
