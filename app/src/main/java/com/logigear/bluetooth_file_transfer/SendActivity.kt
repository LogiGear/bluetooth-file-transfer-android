package com.logigear.bluetooth_file_transfer

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.TextView

class SendActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
        val fileURI: String? = intent.getStringExtra(AlarmClock.EXTRA_MESSAGE)

        val sendingResult = BluetoothConnectionService().startClient(device, fileURI!!)
        val sendLoading: TextView = findViewById(R.id.sendLoading)
        if (sendingResult) {
            sendLoading.setText("Successfully sent the file!")
        } else {
            sendLoading.setText("Failed to send the file, please try again later.")
        }
    }
}