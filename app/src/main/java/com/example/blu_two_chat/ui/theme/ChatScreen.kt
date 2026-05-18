package com.example.blu_two_chat.ui.theme


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.blu_two_chat.model.ChatMessage
import com.example.blu_two_chat.viewmodel.ChatViewModel


@Composable
fun ChatScreen(viewModel: ChatViewModel) {

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()



    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {

        Text("Connected", modifier = Modifier.padding(8.dp))

        LazyColumn(state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth()) {
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message…") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (input.isNotBlank()) {
                    input = ""
                }
            }) { Text("Send") }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val align     = if (message.isMine) Alignment.End      else Alignment.Start
    val bgColor   = if (message.isMine) Color(0xFF2196F3)  else Color(0xFFE0E0E0)
    val textColor = if (message.isMine) Color.White        else Color.Black

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = align) {
        Surface(color = bgColor, shape = RoundedCornerShape(12.dp)) {
            Text(message.text, color = textColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
        }
    }
}