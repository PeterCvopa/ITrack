package com.example.itrack.location

import com.google.android.gms.tasks.OnFailureListener

interface Tracker {
    fun stopLocationUpdatesIfExist()
    fun startLocationUpdates(
        sampleInterval: Int,
        callBack: LocationChangeCallBack,
        onFailureListener: OnFailureListener
    )
}