package com.example.blu_two_chat.viewmodel


import androidx.lifecycle.ViewModel
import com.example.blu_two_chat.bluetooth.BluetoothController
import com.example.blu_two_chat.model.BluetoothDeviceModel

class ChatViewModel(private val controller: BluetoothController) : ViewModel() {

    val scannedDevices = controller.scannedDevices
    val pairedDevices = controller.pairedDevices
    val connectionState = controller.connectionState
    val messages = controller.messages

    fun startScan() {
        controller.updatePairedDevices()
        controller.startDiscovery()
    }

    fun stopScan() = controller.stopDiscovery()
    fun startServer() = controller.startBluetoothServer()
    fun connectToDevice(d: BluetoothDeviceModel) = controller.connectToDevice(d)
    fun sendMessage(text: String) = controller.sendMessage(text)
    fun disconnect() = controller.closeConnection()

    override fun onCleared() {
        super.onCleared()
        controller.release()
    }
}