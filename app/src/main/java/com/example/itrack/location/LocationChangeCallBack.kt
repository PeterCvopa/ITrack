package com.example.itrack.location

import android.location.Location
import com.google.android.gms.location.LocationResult


interface LocationChangeCallBack {
  fun  onLocationReceived(locationResult :LocationResult)
}