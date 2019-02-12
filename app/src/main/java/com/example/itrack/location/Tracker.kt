package com.example.itrack.location

interface Tracker {
    fun startLocationUpdates(callBack: LocationChangeCallBack)
    fun stopLocationUpdates()
}