package com.logigear.bluetooth_file_transfer

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.logigear.bluetooth_file_transfer.databinding.ActivityMainBinding
import com.logigear.bluetooth_file_transfer.server.BluetoothServerController
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var pairedDevices: Set<BluetoothDevice>
    private lateinit var discoveredDevices: ArrayList<BluetoothDevice>

    private var isInScanningMode: Boolean = false

    private val REQUEST_ENABLE_BLUETOOTH = 1
    private var MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0
    private var MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0
    private var MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "This device does not support bluetooth", Toast.LENGTH_SHORT)
                .show()
            this.finish()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION
                )
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                )
            }
        }

        discoveredDevices = ArrayList()

        val deviceFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, deviceFilter)

        val deviceNameFilter = IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED)
        registerReceiver(nameReceiver, deviceNameFilter)

        mainEnterZone.setOnClickListener { enterScanningMode() }
        mainRefreshUserList.setOnClickListener { refreshList() }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val index = discoveredDevices.indexOf(device)
                    if (index == -1) {
                        discoveredDevices.add(device!!)
                    } else {
                        discoveredDevices[index] = device!!
                    }
                }
            }
        }
    }

    private val nameReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            when (action) {
                BluetoothDevice.ACTION_NAME_CHANGED -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val index = discoveredDevices.indexOf(device!!)
                    if (index == -1) {
                        discoveredDevices.add(device)
                    } else {
                        discoveredDevices[index] = device
                    }
                }
            }
        }
    }

    private fun refreshList() {
        if (!isInScanningMode) {
            Toast.makeText(this, "Please enter the scanning mode first", Toast.LENGTH_SHORT).show()
            return
        }

        pairedDevices = bluetoothAdapter!!.bondedDevices

        val list: ArrayList<BluetoothDevice> = ArrayList()

        val listDeviceNames: ArrayList<String> = ArrayList()

        if (pairedDevices.isNotEmpty()) {
            for (device: BluetoothDevice in pairedDevices) {
                list.add(device)
                listDeviceNames.add(device.name)
            }
        } else {
            Toast.makeText(this, "No paired bluetooth devices found", Toast.LENGTH_SHORT).show()
        }

        if (discoveredDevices.isNotEmpty()) {
            for (device: BluetoothDevice in discoveredDevices) {
                list.add(device)

                if (device.name == null) {
                    listDeviceNames.add(device.address)
                } else {
                    listDeviceNames.add(device.name)
                }
            }
        } else {
            Toast.makeText(this, "No new bluetooth devices found", Toast.LENGTH_SHORT).show()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listDeviceNames)
        mainSelectUserList.adapter = adapter

        mainSelectUserList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val device: BluetoothDevice = list[position]

                val intent = Intent(this, FilePickerActivity::class.java)
                intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
                startActivity(intent)
            }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {

            if (resultCode === Activity.RESULT_OK) {
                if (bluetoothAdapter!!.isEnabled) {
                    Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Bluetooth has been disabled", Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth has been cancelled", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode.equals(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth has been cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeTextToConnected(statusTextView: TextView) {
        statusTextView.text = "Scanning Mode"
        statusTextView.setTextColor(Color.GREEN)

        mainEnterZone.text = "Exit Scanning Mode"
    }

    private fun changeTextToDisconnected(statusTextView: TextView) {
        statusTextView.text = "Not in Scanning Mode"
        statusTextView.setTextColor(Color.RED)

        mainEnterZone.text = "Enter Scanning Mode"
    }

    private fun enterScanningMode() {
        if (isInScanningMode) {
            exitScanningMode()
        } else {
            if (!bluetoothAdapter!!.isEnabled) {
                bluetoothAdapter!!.enable()
            }

            val discoverableIntent: Intent =
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                    putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300) //5 mins
                }
            startActivity(discoverableIntent)
            BluetoothConnectionService().startServer()

            changeTextToConnected(statusTitle)
            isInScanningMode = true
            refreshList()
        }
    }

    private fun exitScanningMode() {
        BluetoothServerController().cancel()
        bluetoothAdapter!!.cancelDiscovery()

        mainSelectUserList.adapter = null
        Toast.makeText(this, "Discoverability is disabled for now", Toast.LENGTH_SHORT).show()
        changeTextToDisconnected(statusTitle)
        isInScanningMode = false
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
        unregisterReceiver(nameReceiver)
    }
}