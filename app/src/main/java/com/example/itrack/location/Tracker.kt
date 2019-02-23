package com.example.itrack.location

import com.google.android.gms.tasks.OnFailureListener

interface Tracker {
    fun stopLocationUpdatesIfExist()
    fun startLocationUpdates(callBack: LocationChangeCallBack, sampleInterval: Int, onFailureListener: OnFailureListener)
}