package com.logigear.bluetooth_file_transfer

import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.logigear.bluetooth_file_transfer.helper.FilePickerHelper
import kotlinx.android.synthetic.main.activity_file_picker.*
import java.io.File

class FilePickerActivity : AppCompatActivity() {

    private var device: BluetoothDevice? = null
    private var fileURI: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_picker)

        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        if (device!!.name == null) {
            deviceInfoNameValue.text = device!!.address
        } else {
            deviceInfoNameValue.text = device!!.name
        }

        fileSelectButton.setOnClickListener { filePicker() }
        fileSelectorSend.setOnClickListener { send() }
    }

    private fun filePicker() {
        val mimeTypes: Array<String> = arrayOf("image/*", "video/*", "application/pdf", "audio/*")

        val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(Intent.createChooser(intent, "Choose a file"), 111)
    }

    private fun send() {
        if (fileURI == "") {
            Toast.makeText(this, "Please choose a file first", Toast.LENGTH_SHORT).show()
        } else {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Confirmation")
            alertDialogBuilder.setMessage("Are you sure want to send this file?")
            alertDialogBuilder.setPositiveButton("Send") { _, _ -> checkLessThan5MB(fileURI) }
            alertDialogBuilder.setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(this, "Cancelled the file sending process", Toast.LENGTH_SHORT)
                    .show()
            }
            alertDialogBuilder.show()
        }

    }

    private fun checkLessThan5MB(fileURI: String) {
        val fiveMB = 1024 * 1024 * 5;
        val file = File(fileURI)

        if (file.readBytes().size > fiveMB) {
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("File too large")
            alertDialogBuilder.setMessage("This file is larger than the 5MB Limit")
            alertDialogBuilder.setPositiveButton("OK") { _, _ ->
                Toast.makeText(this, "File sending failed", Toast.LENGTH_SHORT).show()
            }
            alertDialogBuilder.show()
        } else {
            val intent = Intent(this, SendActivity::class.java)
            intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device)
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, fileURI)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == 111) {
            if (resultCode == RESULT_OK) {
                val selectedFile = data?.data
                val selectedFilePath = FilePickerHelper.getPath(this, selectedFile!!)
                fileInfoNameValue.text = selectedFilePath
                fileURI = selectedFilePath!!
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "File choosing cancelled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error while choosing this file", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
}