package com.logigear.bluetooth_file_transfer.helper

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.lang.Long

class FilePickerHelper {
    companion object {
        const val PRIMARY_DOCUMENT = "primary"
        const val BLANK_DOCUMENT = ""

        const val EXTERNAL_STORAGE_DOCUMENT = "com.android.externalstorage.documents"
        const val DOWNLOADS_DOCUMENT = "com.android.providers.downloads.documents"
        const val MEDIA_DOCUMENT = "com.android.providers.media.documents"

        const val IMAGE = "image"
        const val VIDEO = "video"
        const val AUDIO = "audio"
        const val CONTENT = "content"
        const val FILE = "file"

        const val PUBLIC_DOWNLOADS_LOCATION = "content://downloads/public_downloads"
        fun getPath(context: Context, uri: Uri): String? {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    val documentId = DocumentsContract.getDocumentId(uri)
                    val split = documentId.split(":".toRegex()).dropLastWhile {
                        it.isEmpty()
                    }.toTypedArray()
                    val type = split[0]
                    if (PRIMARY_DOCUMENT.equals(type, ignoreCase = true)) {
                        return (BLANK_DOCUMENT + Environment.getDownloadCacheDirectory() + "/" + split[1])
                    }
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse(PUBLIC_DOWNLOADS_LOCATION), Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val documentId = DocumentsContract.getDocumentId(uri)
                    val split = documentId.split(":".toRegex()).dropLastWhile {
                        it.isEmpty()
                    }.toTypedArray()
                    val type = split[0]

                    val contentUri: Uri? = when {
                        (IMAGE == type) -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        (VIDEO == type) -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        (AUDIO == type) -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if (CONTENT.equals(uri.scheme!!, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            } else if (FILE.equals(uri.scheme!!, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        private fun getDataColumn(
            context: Context, uri: Uri?,
            selection: String?, selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)

            try {
                cursor = context.contentResolver.query(
                    uri!!, projection,
                    selection, selectionArgs, null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(columnIndex)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        private fun isMediaDocument(uri: Uri): Boolean {
            return MEDIA_DOCUMENT == uri.authority
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return DOWNLOADS_DOCUMENT == uri.authority
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return EXTERNAL_STORAGE_DOCUMENT == uri.authority
        }
    }
}