package com.example.itrack.location

interface Tracker {
    fun startLocationUpdates(callBack: LocationChangeCallBack, sampleInterval :Int)
    fun stopLocationUpdatesIfExist()
}