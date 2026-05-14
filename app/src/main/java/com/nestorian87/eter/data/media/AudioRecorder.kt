package com.nestorian87.eter.data.media

import com.nestorian87.eter.domain.repository.AudioRecorderRepository
import android.media.MediaRecorder
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorder @Inject constructor() : AudioRecorderRepository {
    private var mediaRecorder: MediaRecorder? = null
    private var activeOutputFile: File? = null

    override fun startRecording(outputFile: File) {
        cancelRecording()

        outputFile.parentFile?.mkdirs()

        @Suppress("DEPRECATION")
        val recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(AUDIO_ENCODING_BITRATE)
            setAudioSamplingRate(AUDIO_SAMPLING_RATE)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }

        mediaRecorder = recorder
        activeOutputFile = outputFile
    }

    override fun stopRecording(): File {
        val recorder = mediaRecorder ?: throw IllegalStateException("Recording has not started")
        val outputFile = activeOutputFile ?: throw IllegalStateException("No active output file")

        return try {
            recorder.stop()
            outputFile
        } catch (error: RuntimeException) {
            outputFile.delete()
            throw IllegalStateException("Could not finish audio recording", error)
        } finally {
            releaseRecorder()
        }
    }

    override fun cancelRecording() {
        val outputFile = activeOutputFile

        try {
            runCatching { mediaRecorder?.stop() }
        } finally {
            releaseRecorder()
        }

        if (outputFile != null && outputFile.exists()) {
            outputFile.delete()
        }
    }

    private fun releaseRecorder() {
        mediaRecorder?.reset()
        mediaRecorder?.release()
        mediaRecorder = null
        activeOutputFile = null
    }

    private companion object {
        const val AUDIO_ENCODING_BITRATE = 128_000
        const val AUDIO_SAMPLING_RATE = 44_100
    }
}
