package com.nestorian87.eter.domain.repository

import java.io.File

interface AudioRecorderRepository {
    fun startRecording(outputFile: File)

    fun stopRecording(): File

    fun cancelRecording()
}
