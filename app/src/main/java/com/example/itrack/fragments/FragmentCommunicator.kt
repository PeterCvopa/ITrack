package com.example.itrack.fragments

import android.location.Location

interface FragmentCommunicator {
    fun onNewLocationReceived(location: Location)
}