package com.shopkeeper.mobileshop.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

object BluetoothPrinterManager {
    private val PRINTER_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    @SuppressLint("MissingPermission")
    fun getPairedPrinters(context: Context): List<BluetoothDevice> {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()
        return adapter.bondedDevices.filter { 
            val name = it.name?.uppercase() ?: ""
            name.contains("PRINTER") || name.contains("POS") || name.contains("MTP") || name.contains("THERMAL") || name.contains("BT") 
        }.ifEmpty { adapter.bondedDevices.toList() }
    }

    @SuppressLint("MissingPermission")
    suspend fun connectAndPrint(context: Context, deviceAddress: String, textToPrint: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter ?: return@withContext false
            val device = adapter.getRemoteDevice(deviceAddress)

            outputStream?.close()
            bluetoothSocket?.close()

            bluetoothSocket = device.createRfcommSocketToServiceRecord(PRINTER_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream

            if (outputStream == null) return@withContext false

            outputStream?.write(byteArrayOf(0x1B, 0x40)) 
            outputStream?.write(textToPrint.toByteArray(Charsets.UTF_8))
            outputStream?.write("\n\n\n".toByteArray(Charsets.UTF_8))
            outputStream?.write(byteArrayOf(0x1D, 0x56, 0x42, 0x00)) 
            outputStream?.flush()
            Thread.sleep(1000)
            
            outputStream?.close()
            bluetoothSocket?.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                outputStream?.close()
                bluetoothSocket?.close()
            } catch (ex: Exception) {}
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun showPrinterSelectionDialog(context: Context, onPrinterSelected: (String) -> Unit) {
        val printers = getPairedPrinters(context)
        if (printers.isEmpty()) {
            android.widget.Toast.makeText(context, "No paired Bluetooth printers found.", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        val printerNames = printers.map { "${it.name}\n${it.address}" }.toTypedArray()
        com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
            .setTitle("Select Bluetooth Printer")
            .setItems(printerNames) { _, which ->
                val address = printers[which].address
                AppPreferences.setBluetoothPrinterAddress(context, address)
                onPrinterSelected(address)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
