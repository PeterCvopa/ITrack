package com.example.itrack.location.precondition

import android.location.Location

interface LocationPrecondition {
    fun validate(oldLocation : List<Location>, newLocation: Location): Boolean
}