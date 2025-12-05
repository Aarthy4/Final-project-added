package com.example.emergencynetwork.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    private val permissions = arrayOf(
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun requestBluetoothPermissions(activity: Activity) {
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                needed.toTypedArray(),
                100
            )
        }
    }
}
