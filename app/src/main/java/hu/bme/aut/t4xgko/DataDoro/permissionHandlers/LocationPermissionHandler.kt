package hu.bme.aut.t4xgko.DataDoro.permissionHandlers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

class LocationPermissionHandler(private val activity: AppCompatActivity) {

    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var onPermissionsGranted: () -> Unit = {}

    fun setupPermissions(onGranted: () -> Unit) {
        onPermissionsGranted = onGranted
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                onPermissionsGranted()
            } else {
                Toast.makeText(
                    activity,
                    "Location permissions not granted by the user.",
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
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestRequiredPermissions() {
        permissionLauncher.launch(requiredPermissions)
    }

    fun getLastKnownLocation(): Location? {
        if (!hasRequiredPermissions()) {
            return null
        }

        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null

        for (provider in providers) {
            val location = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                bestLocation = location
            }
        }

        return bestLocation;
    }
}
