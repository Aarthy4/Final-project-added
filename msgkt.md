app/src/main/java/com/example/emergencynetwork/data/Message.kt
package com.example.emergencynetwork.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val contentEncrypted: String,
    val timestamp: Long,
    val received: Boolean = false
)
