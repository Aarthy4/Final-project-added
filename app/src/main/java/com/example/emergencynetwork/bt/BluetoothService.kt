package com.example.emergencynetwork.bt

import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.*
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

class BluetoothService(
    private val context: Context,
    private val onMessageReceived: (String, String) -> Unit
) {

    private val adapter: BluetoothAdapter? = getDefaultAdapter()
    private val discoveredDevices = mutableListOf<BluetoothDevice>()

    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var serverSocket: BluetoothServerSocket? = null

    init {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(
            BluetoothReceiver { device ->
                if (!discoveredDevices.contains(device)) {
                    discoveredDevices.add(device)
                }
            },
            filter
        )
    }

    fun startDiscovery() {
        discoveredDevices.clear()
        adapter?.startDiscovery()
    }

    fun getDiscoveredDevices(): List<BluetoothDevice> = discoveredDevices

    fun startServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = adapter?.listenUsingRfcommWithServiceRecord(
                    "EmergencyNetworkService",
                    uuid
                )

                while (true) {
                    val socket = serverSocket?.accept() ?: continue
                    handleConnection(socket)
                }

            } catch (e: Exception) {
                Log.e("BT-SERVER", "Error: ${e.message}")
            }
        }
    }

    fun sendMessage(device: BluetoothDevice, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                socket.connect()

                val output = socket.outputStream
                output.write(message.toByteArray())

                output.close()
                socket.close()

            } catch (e: Exception) {
                Log.e("BT-SEND", "Error sending: ${e.message}")
            }
        }
    }

    private fun handleConnection(socket: BluetoothSocket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val input = socket.inputStream
                val buffer = ByteArray(1024)
                val bytes = input.read(buffer)
                val msg = String(buffer, 0, bytes)

                val parts = msg.split("|")
                if (parts.size == 2) {
                    val sender = parts[0]
                    val content = parts[1]
                    onMessageReceived(sender, content)
                }

                input.close()
                socket.close()

            } catch (e: IOException) {
                Log.e("BT-RECV", "Error: ${e.message}")
            }
        }
    }

    fun cleanup() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
    }
}
