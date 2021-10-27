package com.logigear.bluetooth_file_transfer.server

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import com.logigear.bluetooth_file_transfer.BluetoothConnectionService
import java.io.IOException

class BluetoothServerController : Thread() {

    private var isCancelled: Boolean

    private val serverSocket: BluetoothServerSocket?

    init {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter != null) {
            this.serverSocket = bluetoothAdapter
                .listenUsingRfcommWithServiceRecord(
                    "BluetoothFileTransfer",
                    BluetoothConnectionService.uuid
                )
            this.isCancelled = false
        } else {
            this.serverSocket = null
            this.isCancelled = true
        }
    }

    override fun run() {
        var socket: BluetoothSocket
        while (true) {
            if (this.isCancelled) {
                break
            }

            try {
                socket = serverSocket!!.accept()
            } catch (e: IOException) {
                break
            }

            if (!this.isCancelled && socket != null) {
                BluetoothServer(socket).start()
            }
        }
    }

    fun cancel() {
        this.isCancelled = true
        this.serverSocket!!.close()
    }
}