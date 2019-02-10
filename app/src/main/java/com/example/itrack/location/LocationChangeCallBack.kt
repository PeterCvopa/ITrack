package com.example.itrack.location

import com.google.android.gms.location.LocationResult

interface LocationChangeCallBack {
    fun onLocationReceived(locationResult: LocationResult)
}