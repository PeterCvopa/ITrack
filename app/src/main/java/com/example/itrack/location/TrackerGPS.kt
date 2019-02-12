package com.example.itrack.location

import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.itrack.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.lang.Exception


class TrackerGPS(private val activity: Activity) : Tracker {

    companion object {
        private val TAG = TrackerGPS::class.simpleName
        private const val REQUEST_CHECK_SETTINGS = 0x1
    }

    private lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationChangeCallBack: LocationChangeCallBack
    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
    private val settingsClient: SettingsClient = LocationServices.getSettingsClient(activity)
    private val locationCallback: LocationCallback = createLocationCallBack()
    private var isTracking: Boolean = false

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates(callBack: LocationChangeCallBack, sampleInterval: Int) {
        stopLocationUpdatesIfExist()
        this.locationChangeCallBack = callBack
        this.locationRequest = this.createLocationRequest(sampleInterval.toLong())
        this.mLocationSettingsRequest = this.createLocationSettingsRequest()!!
        this.settingsClient
            .checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(activity, this::onLocationSettingSuccess)
            .addOnFailureListener(activity, this::onLocationSettingFails)
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

    private fun onRemoveLocationUpdateCompleted(task: Task<Void>) {
        isTracking = false
        Log.d(TAG, "onRemoveLocationUpdateCompleted with result: ${task.isSuccessful}.")
    }

    @SuppressLint("MissingPermission")
    private fun onLocationSettingSuccess(locationSettingsResponse: LocationSettingsResponse) {
        Log.d(TAG, ".onLocationSettingSuccess.")
        fusedClient.let {
            isTracking = true
            Log.d(TAG, "added locReq i= ${locationRequest.interval} ifast= ${locationRequest.fastestInterval}")
            it.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }

    private fun onLocationSettingFails(e: Exception) {
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

    private fun onSettingNeedResolution(e: Exception) {
        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " + "location settings ")
        try {
            val rae = e as ResolvableApiException
            rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
        } catch (sie: IntentSender.SendIntentException) {
            Log.e(TAG, sie.message)
        }
    }

    private fun onSettingFail() {
        val errorMessage = activity.resources.getString(R.string.insufficient_setting_message)
        Log.e(TAG, errorMessage)
        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
        isTracking = false
    }
}