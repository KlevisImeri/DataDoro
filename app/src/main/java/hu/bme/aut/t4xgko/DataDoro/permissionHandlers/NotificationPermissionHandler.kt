package hu.bme.aut.t4xgko.DataDoro.permissionHandlers

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


class NotificationPermissionHandler(private val activity: AppCompatActivity) {

    private lateinit var permissionLauncher: ActivityResultLauncher<Intent>
    private var onPermissionsGranted: () -> Unit = {}

    fun setupPermissions(onGranted: () -> Unit) {
        onPermissionsGranted = onGranted
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (areNotificationsEnabled()) {
                onPermissionsGranted()
            } else {
                Toast.makeText(
                    activity,
                    "Notifications not enabled by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun checkAndRequestPermissions() {
        if (areNotificationsEnabled()) {
            onPermissionsGranted()
        } else {
            requestNotificationPermission()
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    private fun requestNotificationPermission() {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
        }
        permissionLauncher.launch(intent)
    }
}
