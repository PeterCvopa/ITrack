package com.example.itrack.location.precondition

import android.location.Location

class LocationValidator {

    private var preconditions: MutableList<LocationPrecondition> = mutableListOf()

    fun validate(recentLocations: List<Location>, newLocation: Location): Boolean {
        if (recentLocations.size > 10) throw IllegalArgumentException("Max 10 elements in recent locations")
        var result = true
        preconditions.forEach {
            result = result || it.validate(recentLocations, newLocation)
        }
        return result
    }

    fun addPrecondition(precondition: LocationPrecondition) {
        preconditions.add(precondition)
    }
}