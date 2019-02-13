package com.example.itrack.location

import android.location.Location

class LocationValidator {

    private var preconditions: MutableList<LocationPrecondition> = mutableListOf()

    fun validate(oldLocation: Location, newLocation: Location): Boolean {
        var result = true
        preconditions.forEach {
            result = result && it.validate(oldLocation, newLocation)
        }
        return result
    }

    fun addPrecondition(precondition: LocationPrecondition) {
        preconditions.add(precondition)
    }
}