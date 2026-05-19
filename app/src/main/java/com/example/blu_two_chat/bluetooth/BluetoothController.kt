package com.example.blu_two_chat.bluetooth


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.example.blu_two_chat.model.BluetoothDeviceModel
import com.example.blu_two_chat.model.ChatMessage
import com.example.blu_two_chat.model.ConnectionState
import com.example.blu_two_chat.utils.MY_UUID
import com.example.blu_two_chat.utils.SERVICE_NAME
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

@SuppressLint("MissingPermission")
class BluetoothController(private val context: Context) {

    private val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = btManager.adapter

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceModel>>(emptyList())
    val scannedDevices = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceModel>>(emptyList())
    val pairedDevices = _pairedDevices.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState = _connectionState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    private var listenJob: Job? = null
    private var connectJob: Job? = null
    private var readJob: Job? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val foundReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    else
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                device?.let {
                    val model = BluetoothDeviceModel(it.name, it.address)
                    val current = _scannedDevices.value
                    if (current.none { d -> d.address == model.address }) {
                        _scannedDevices.value = current + model
                    }
                }
            }
        }
    }

    fun isBluetoothAvailable(): Boolean = adapter != null
    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true

    fun updatePairedDevices() {
        adapter?.bondedDevices
            ?.map { BluetoothDeviceModel(it.name, it.address) }
            ?.also { _pairedDevices.value = it }
    }

    fun startDiscovery() {
        _scannedDevices.value = emptyList()
        context.registerReceiver(foundReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        adapter?.startDiscovery()
    }

    fun stopDiscovery() {
        adapter?.cancelDiscovery()
        try { context.unregisterReceiver(foundReceiver) } catch (_: IllegalArgumentException) {}
    }

    fun startBluetoothServer() {
        listenJob = scope.launch {
            try {
                _connectionState.value = ConnectionState.Connecting
                serverSocket = adapter?.listenUsingRfcommWithServiceRecord(SERVICE_NAME, MY_UUID)
                val socket = serverSocket?.accept()
                serverSocket?.close()
                socket?.let {
                    clientSocket = it
                    _connectionState.value = ConnectionState.Connected
                    listenForMessages(it)
                }
            } catch (e: IOException) {
                _connectionState.value = ConnectionState.Failed(e.message ?: "Server error")
            }
        }
    }

    fun connectToDevice(device: BluetoothDeviceModel) {
        connectJob = scope.launch {
            try {
                _connectionState.value = ConnectionState.Connecting
                stopDiscovery()
                val btDevice = adapter?.getRemoteDevice(device.address)
                val socket = btDevice?.createRfcommSocketToServiceRecord(MY_UUID)
                socket?.connect()
                socket?.let {
                    clientSocket = it
                    _connectionState.value = ConnectionState.Connected
                    listenForMessages(it)
                }
            } catch (e: IOException) {
                clientSocket?.close()
                _connectionState.value = ConnectionState.Failed(e.message ?: "Could not connect")
            }
        }
    }

    private fun listenForMessages(socket: BluetoothSocket) {
        readJob = scope.launch {
            val buffer = ByteArray(1024)
            val input = socket.inputStream
            while (isActive) {
                try {
                    val bytes = input.read(buffer)
                    if (bytes > 0) {
                        val text = String(buffer, 0, bytes)
                        _messages.value += ChatMessage(text, isMine = false)
                    }
                } catch (e: IOException) {
                    _connectionState.value = ConnectionState.Disconnected
                    break
                }
            }
        }
    }

    fun sendMessage(text: String) {
        scope.launch {
            try {
                clientSocket?.outputStream?.write(text.toByteArray())
                _messages.value += ChatMessage(text, isMine = true)
            } catch (e: IOException) {
                _connectionState.value = ConnectionState.Failed("Send failed")
            }
        }
    }

    fun closeConnection() {
        try { clientSocket?.close() } catch (_: IOException) {}
        try { serverSocket?.close() } catch (_: IOException) {}
        clientSocket = null
        serverSocket = null
        readJob?.cancel()
        connectJob?.cancel()
        listenJob?.cancel()
        _connectionState.value = ConnectionState.Disconnected
    }

    fun release() {
        closeConnection()
        stopDiscovery()
        scope.cancel()
    }
}