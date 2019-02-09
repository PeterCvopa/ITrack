package com.example.itrack.location

import android.location.Location

interface LocationStrategy {

    fun getLocation(
    ): Location

}