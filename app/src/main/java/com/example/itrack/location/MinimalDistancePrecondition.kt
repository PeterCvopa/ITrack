package com.example.itrack.location

import android.location.Location
import com.example.itrack.common.LocationHelper

class MinimalDistancePrecondition (var minimalDistance: Int = 10) : LocationPrecondition {
    override fun validate(oldLocation : Location, newLocation: Location): Boolean {
        return LocationHelper.distanceBetweenTwoLocations(oldLocation,newLocation) >= minimalDistance
    }
}