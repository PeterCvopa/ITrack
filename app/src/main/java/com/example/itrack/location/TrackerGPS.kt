package com.example.itrack.location

import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener


class TrackerGPS(private val activity: Activity ) {

    private val TAG = TrackerGPS::class.java.simpleName

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    private val REQUEST_CHECK_SETTINGS = 0x1

    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

    private lateinit var locationCallback: LocationCallback
    private var mLocationSettingsRequest: LocationSettingsRequest? = createLocationSettingsRequest()
    private val locationRequest = createLocationRequest()
    private val fusedLocationClient: FusedLocationProviderClient? = LocationServices.getFusedLocationProviderClient(activity)
    private val settingsClient: SettingsClient? = LocationServices.getSettingsClient(activity)
    private var isTracking: Boolean = false

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastKnowLocation(listner: OnCompleteListener<Location>) {
        fusedLocationClient?.let { it.lastLocation.addOnCompleteListener(activity, listner) }
    }


    private fun createLocationSettingsRequest(): LocationSettingsRequest? {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        return builder.build()
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(locationCallback: LocationCallback) {
        settingsClient?.checkLocationSettings(mLocationSettingsRequest)
            ?.addOnSuccessListener(activity) {
                Log.i(TAG, "All location settings are satisfied.")
                fusedLocationClient?.let {
                    it.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                }
            }?.addOnFailureListener(activity) { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " + "location settings ")
                        try {
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.i(TAG, "PendingIntent unable to execute request.")
                        }

                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage =
                            "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                        Log.e(TAG, errorMessage)
                        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
                        isTracking = false
                    }
                }
            }
    }

    fun stopLocationUpdates() {
        if (!isTracking) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.")
            return
        }
        fusedLocationClient?.removeLocationUpdates(locationCallback)
            ?.addOnCompleteListener(activity) {
                isTracking = false
            }
    }

    enum class LocationRequestSetting {
        //TODO make changes
    }

}