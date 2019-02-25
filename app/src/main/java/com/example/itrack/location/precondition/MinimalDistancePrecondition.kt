package com.example.itrack.location.precondition

import android.location.Location
import com.example.itrack.common.LocationHelper

class MinimalDistancePrecondition(var minimalDistance: Int = 10) :
    LocationPrecondition {
    override fun validate(recentLocations: List<Location>, newLocation: Location): Boolean {
        return if (recentLocations.isEmpty()) true else LocationHelper.distanceBetweenTwoLocations(recentLocations.last(), newLocation) >= minimalDistance
    }
}