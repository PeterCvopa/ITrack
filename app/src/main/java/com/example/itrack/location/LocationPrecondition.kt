package com.example.itrack.location

import android.location.Location

interface LocationPrecondition {
    fun validate(oldLocation : Location, newLocation: Location): Boolean
}