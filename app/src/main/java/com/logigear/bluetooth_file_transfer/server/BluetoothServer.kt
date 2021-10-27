package com.logigear.bluetooth_file_transfer.server

import android.bluetooth.BluetoothSocket
import com.logigear.bluetooth_file_transfer.helper.BluetoothConnectionHelper
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class BluetoothServer(private val socket: BluetoothSocket): Thread() {

    private val inputStream = this.socket.inputStream

    private val outputStream = this.socket.outputStream

    override fun run() {
        if (BluetoothConnectionHelper.isExternalStorageWritable()) {
            val totalFileNameSizeInBytes: Int
            val totalFileSizeInBytes: Int

            // File name string size
            val fileNameSizebuffer = ByteArray(4) // Only 4 bytes needed for this operation, int => 4 bytes
            inputStream!!.read(fileNameSizebuffer, 0, 4)
            var fileSizeBuffer = ByteBuffer.wrap(fileNameSizebuffer)
            totalFileNameSizeInBytes = fileSizeBuffer.int

            // String of file name
            val fileNamebuffer = ByteArray(1024)
            inputStream.read(fileNamebuffer, 0, totalFileNameSizeInBytes)
            val fileName = String(fileNamebuffer, 0, totalFileNameSizeInBytes)

            // File size integer bytes
            val fileSizebuffer = ByteArray(4) // int => 4 bytes
            inputStream.read(fileSizebuffer, 0, 4)
            fileSizeBuffer = ByteBuffer.wrap(fileSizebuffer)
            totalFileSizeInBytes = fileSizeBuffer.int

            // The actual file bytes
            val baos = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var read: Int
            var totalBytesRead = 0
            read = inputStream.read(buffer, 0, buffer.size)
            while (read != -1) {
                baos.write(buffer, 0, read)
                totalBytesRead += read
                if (totalBytesRead == totalFileSizeInBytes) {
                    break
                }
                read = inputStream.read(buffer, 0, buffer.size)
            }
            baos.flush()

            val saveFile = BluetoothConnectionHelper.getPublicStorageDir(fileName)
            if (saveFile.exists()) {
                saveFile.delete()
            }
            val fos = FileOutputStream(saveFile.path)
            fos.write(baos.toByteArray())
            fos.close()
        }
        sleep(5000)
        inputStream.close()
        outputStream.close()
        socket.close()
    }
}