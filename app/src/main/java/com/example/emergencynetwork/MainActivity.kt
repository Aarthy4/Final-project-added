package com.example.emergencynetwork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.lifecycle.lifecycleScope
import com.example.emergencynetwork.bt.BluetoothService
import com.example.emergencynetwork.data.AppDatabase
import com.example.emergencynetwork.data.Message
import com.example.emergencynetwork.util.AESHelper
import kotlinx.coroutines.launch
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable

class MainActivity : ComponentActivity() {

    private lateinit var btService: BluetoothService
    private val db by lazy { AppDatabase.getDatabase(this) }
    private var userName = "User-" + (100..999).random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btService = BluetoothService(this) { sender, msg ->
            lifecycleScope.launch {
                val encrypted = AESHelper.encrypt(msg)
                db.messageDao().insert(
                    Message(
                        senderName = sender,
                        contentEncrypted = encrypted,
                        timestamp = System.currentTimeMillis(),
                        received = true
                    )
                )
            }
        }

        btService.startServer()

        setContent {
            val scope = rememberCoroutineScope()
            val messages = remember { mutableStateListOf<Message>() }

            LaunchedEffect(Unit) {
                db.messageDao().getAllMessages().collect { list ->
                    messages.clear()
                    messages.addAll(list)
                }
            }

            var text by remember { mutableStateOf("") }
            var devices by remember { mutableStateOf(listOf<BluetoothDevice>()) }

            Scaffold(
                topBar = { TopAppBar(title = { Text("Emergency Network - Phase 1") }) }
            ) { padding ->

                Column(modifier = Modifier.padding(16.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Your name") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            btService.startDiscovery()
                            scope.launch {
                                kotlinx.coroutines.delay(1500)
                                devices = btService.getDiscoveredDevices()
                            }
                        }) { Text("Scan") }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Discovered Devices:")
                    LazyColumn(modifier = Modifier.height(120.dp)) {
                        items(devices.size) { idx ->
                            val d = devices[idx]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .clickable {
                                        val payload = "${userName}|Hello from $userName"
                                        btService.sendMessage(d, payload)
                                    }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(d.name ?: "Unknown")
                                    Text(d.address ?: "")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row {
                        Button(onClick = {
                            lifecycleScope.launch {
                                val enc = AESHelper.encrypt(text)
                                db.messageDao().insert(
                                    Message(
                                        senderName = userName,
                                        contentEncrypted = enc,
                                        timestamp = System.currentTimeMillis(),
                                        received = false
                                    )
                                )
                            }
                        }) { Text("Save") }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = {
                            devices.forEach { d ->
                                val payload = "${userName}|$text"
                                btService.sendMessage(d, payload)
                            }
                        }) { Text("Send") }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Inbox (newest first):")

                    LazyColumn {
                        items(messages.size) { i ->
                            val m = messages[i]
                            val decrypted = try {
                                AESHelper.decrypt(m.contentEncrypted)
                            } catch (_: Exception) {
                                "[ERROR]"
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("${m.senderName} - ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date(m.timestamp))}")
                                    Text(decrypted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        btService.cleanup()
    }
}
