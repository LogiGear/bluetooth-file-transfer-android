package com.logigear.bluetooth_file_transfer.helper

import android.os.Environment
import java.io.File

class BluetoothConnectionHelper {
    companion object {
        const val MEDIA_TYPE_UNKNOWN = 0
        const val MEDIA_TYPE_IMAGE = 1
        const val MEDIA_TYPE_VIDEO = 2
        const val MEDIA_TYPE_PDF = 3
        const val MEDIA_TYPE_AUDIO = 4

        fun isExternalStorageWritable(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

        fun getPublicStorageDir(fileName: String): File {
            val directoryType: String = when (getFileType(fileName)) {
                MEDIA_TYPE_IMAGE -> Environment.DIRECTORY_PICTURES
                MEDIA_TYPE_VIDEO -> Environment.DIRECTORY_MOVIES
                MEDIA_TYPE_AUDIO -> Environment.DIRECTORY_MUSIC
                MEDIA_TYPE_PDF -> Environment.DIRECTORY_DOCUMENTS
                else -> Environment.DIRECTORY_DOWNLOADS
            }
            return File(Environment.getExternalStoragePublicDirectory(directoryType), fileName)
        }

        private fun getFileType(fileName: String): Int {
            var extension: String? = null
            val i = fileName.lastIndexOf('.')
            if (i > 0) {
                extension = fileName.substring(i+1)
            }
            if (extension == null) {
                return MEDIA_TYPE_UNKNOWN
            }

            return when (extension) {
                "png", "jpg" -> MEDIA_TYPE_IMAGE
                "pdf" -> MEDIA_TYPE_PDF
                "mp3" -> MEDIA_TYPE_AUDIO
                "mp4" -> MEDIA_TYPE_VIDEO
                else -> MEDIA_TYPE_UNKNOWN
            }
        }
    }
}