package com.forecasty.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.forecasty.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

fun Fragment.requestLocationPermission(
    block: (isGranted: Boolean) -> Unit
): ActivityResultLauncher<String> {
    return registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        block(isGranted)
    }
}

fun Fragment.showPermissionDialog() {
    val alertDialogBuilder = AlertDialog.Builder(requireContext())
        .setCancelable(true)
        .setTitle(getString(R.string.permission_location_rationale_title))
        .setMessage(getString(R.string.permission_location_rationale))

    if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
        alertDialogBuilder
            .setPositiveButton(getString(R.string.go_to_app_settings)) { dialog, _ ->
                dialog.dismiss()
                openAppSystemSettings()
            }

        alertDialogBuilder.create().show()
    }
}

@SuppressLint("MissingPermission")
fun Fragment.queryUserLocation(
    onSuccess: (location: Location?) -> Unit,
    onError: (e: Throwable?) -> Unit
) {
    val locationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(requireContext())

    locationClient.lastLocation
        .addOnSuccessListener {
            onSuccess(it)
        }
        .addOnCanceledListener {
            onError(
                IllegalStateException("getUserLocation: Action cancelled")
            )
        }
        .addOnFailureListener {
            onError(it)
        }
}

fun Fragment.openAppSystemSettings() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        data = Uri.fromParts(
            "package",
            this@openAppSystemSettings.requireContext().packageName,
            this@openAppSystemSettings.tag
        )
    })
}