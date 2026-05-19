package com.example.blu_two_chat.viewmodel

import androidx.lifecycle.ViewModel
import com.example.blu_two_chat.model.BluetoothDeviceModel
import com.example.blu_two_chat.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel : ViewModel() {

    val pairedDevices: StateFlow<List<BluetoothDeviceModel>> = MutableStateFlow(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDeviceModel>> = MutableStateFlow(emptyList())
    val connectionState: StateFlow<ConnectionState> = MutableStateFlow(ConnectionState.Disconnected)

    fun startScan() {}
    fun stopScan() {}
    fun startServer() {}
    fun connectToDevice(device: BluetoothDeviceModel) {}
}