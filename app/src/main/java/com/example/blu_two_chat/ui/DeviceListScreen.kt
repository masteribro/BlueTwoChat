package com.example.blu_two_chat.ui


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blu_two_chat.model.BluetoothDeviceModel
import com.example.blu_two_chat.model.ConnectionState
import com.example.blu_two_chat.viewmodel.ChatViewModel

@Composable
fun DeviceListScreen(viewModel: ChatViewModel) {

    val paired  by viewModel.pairedDevices.collectAsState()
    val scanned by viewModel.scannedDevices.collectAsState()
    val state   by viewModel.connectionState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { viewModel.startScan() })   { Text("Scan") }
            Button(onClick = { viewModel.stopScan() })    { Text("Stop") }
            Button(onClick = { viewModel.startServer() }) { Text("Host") }
        }

        when (state) {
            is ConnectionState.Connecting ->
                Text("Connecting…", modifier = Modifier.padding(top = 12.dp))
            is ConnectionState.Failed ->
                Text("Error: ${(state as ConnectionState.Failed).message}",
                    modifier = Modifier.padding(top = 12.dp))
            else -> {}
        }

        Text("Paired devices",
            fontWeight = FontWeight.Bold, fontSize = 18.sp,
            modifier = Modifier.padding(top = 16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(paired) { device ->
                DeviceItem(device) { viewModel.connectToDevice(device) }
            }
        }

        Text("Available devices",
            fontWeight = FontWeight.Bold, fontSize = 18.sp,
            modifier = Modifier.padding(top = 8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(scanned) { device ->
                DeviceItem(device) { viewModel.connectToDevice(device) }
            }
        }
    }
}

@Composable
private fun DeviceItem(device: BluetoothDeviceModel, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(vertical = 8.dp)) {
        Text(device.name ?: "Unknown", fontWeight = FontWeight.SemiBold)
        Text(device.address, fontSize = 12.sp)
    }
}