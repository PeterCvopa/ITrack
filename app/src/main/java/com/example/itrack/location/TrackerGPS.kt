package com.example.itrack.location

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task


class TrackerGPS(private val activity: Activity) : Tracker {
    companion object {
        private val TAG = TrackerGPS::class.simpleName
    }

    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationChangeCallBack: LocationChangeCallBack
    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(activity)
    private val locationCallback: LocationCallback = createLocationCallBack()
    private var isTracking: Boolean = false

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates(
        sampleInterval: Int,
        callBack: LocationChangeCallBack,
        onFailureListener: OnFailureListener
    ) {
        stopLocationUpdatesIfExist()
        this.locationChangeCallBack = callBack
        this.locationRequest = this.createLocationRequest(sampleInterval.toLong())
        this.mLocationSettingsRequest = this.createLocationSettingsRequest()!!
        this.settingsClient
            .checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(activity, this::onLocationSettingSuccess)
            .addOnFailureListener(activity, onFailureListener)
    }

    override fun stopLocationUpdatesIfExist() {
        if (!isTracking) {
            Log.d(TAG, "stopLocationUpdatesIfExist: no tracking.")
            return
        } else {
            fusedClient
                .removeLocationUpdates(locationCallback)
                .addOnCompleteListener(activity, this::onRemoveLocationUpdateCompleted)
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationSettingSuccess(locationSettingsResponse: LocationSettingsResponse) {
        Log.d(TAG, ".onLocationSettingSuccess. $locationSettingsResponse")
        fusedClient.let {
            isTracking = true
            Log.d(TAG, "added locReq i= ${locationRequest.interval} ifast= ${locationRequest.fastestInterval}")
            it.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }

    private fun onRemoveLocationUpdateCompleted(task: Task<Void>) {
        isTracking = false
        Log.d(TAG, "onRemoveLocationUpdateCompleted with result: ${task.isSuccessful}.")
    }

    private fun createLocationCallBack(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                locationChangeCallBack.onLocationReceived(locationResult)
            }
        }
    }

    private fun createLocationRequest(sampleInterval: Long): LocationRequest {
        return LocationRequest.create().apply {
            interval = sampleInterval
            fastestInterval = sampleInterval / 2
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun createLocationSettingsRequest(): LocationSettingsRequest? {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        return builder.build()
    }
}