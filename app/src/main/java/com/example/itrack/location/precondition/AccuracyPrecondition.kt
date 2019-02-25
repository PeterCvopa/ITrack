package com.example.itrack.location.precondition

import android.location.Location

class AccuracyPrecondition : LocationPrecondition {
    override fun validate(recentLocations: List<Location>, newLocation: Location): Boolean {
        if (recentLocations.isEmpty()) return true
        var accSum = 0.0f
        recentLocations.forEach {
            accSum += it.accuracy
        }
       return (accSum / recentLocations.size ) > newLocation.accuracy
    }
}
