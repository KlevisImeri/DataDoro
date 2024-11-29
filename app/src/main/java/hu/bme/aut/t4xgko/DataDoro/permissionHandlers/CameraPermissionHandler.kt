package hu.bme.aut.t4xgko.DataDoro.permissionHandlers

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class CameraPermissionHandler(private val activity: AppCompatActivity) {
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var onPermissionsGranted: () -> Unit = {}

    fun setupPermissions(onGranted: () -> Unit, onNotGranted: () -> Unit) { 
        onPermissionsGranted = onGranted
        permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            onPermissionsGranted()
        } else {
            onNotGranted()
            Toast.makeText(
                activity,
                "Camera Permissions not granted by the user. Please enable in the settings!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

    fun checkAndRequestPermissions() {
        if (hasRequiredPermissions()) {
            onPermissionsGranted()
        } else {
            requestRequiredPermissions()
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
            }
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
            permissionLauncher.launch(requiredPermissions)
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }
}