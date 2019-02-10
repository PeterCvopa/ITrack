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
import java.lang.Exception


class TrackerGPS(private val activity: Activity, val locationChangeCallBack: LocationChangeCallBack) {

    companion object {
        private val TAG = TrackerGPS::class.simpleName
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 0x1
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
        private const val UPDATE_INTERVAL_IN_MILLISECONDS_FAST: Long = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }

    private var mLocationSettingsRequest: LocationSettingsRequest? = createLocationSettingsRequest()
    private val locationRequest = createLocationRequest()
    private val fusedLocationClient: FusedLocationProviderClient? = LocationServices.getFusedLocationProviderClient(activity)
    private val settingsClient: SettingsClient? = LocationServices.getSettingsClient(activity)
    private val locationCallback: LocationCallback = createLocationCallBack()
    private var isTracking: Boolean = false

    @SuppressLint("MissingPermission")
    fun getLastKnowLocation(listener: OnCompleteListener<Location>) {
        fusedLocationClient?.let { it.lastLocation.addOnCompleteListener(activity, listener) }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
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
                        onSettingNeedResolution(e)
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        onSettingFail()
                    }
                }
            }
    }

    private fun createLocationCallBack(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                locationChangeCallBack.onLocationReceived(locationResult)
            }
        }
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = UPDATE_INTERVAL_IN_MILLISECONDS_FAST
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun createLocationSettingsRequest(): LocationSettingsRequest? {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        return builder.build()
    }

    private fun onSettingNeedResolution(e: Exception) {
        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " + "location settings ")
        try {
            val rae = e as ResolvableApiException
            rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
        } catch (sie: IntentSender.SendIntentException) {
            Log.i(TAG, "PendingIntent unable to execute request.")
        }
    }

    private fun onSettingFail() {
        val errorMessage = "Insufficient setting permission.Fix in Settings."
        Log.e(TAG, errorMessage)
        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
        isTracking = false
    }

    fun stopLocationUpdates() {
        if (!isTracking) {
            Log.d(TAG, "stopLocationUpdates: updates never requested.")
            return
        }
        fusedLocationClient?.removeLocationUpdates(locationCallback)
            ?.addOnCompleteListener(activity) {
                isTracking = false
                Log.d(TAG, "location updates stopped .")
            }
    }
}