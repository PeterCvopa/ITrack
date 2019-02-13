package com.example.itrack.viemodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapsViewModel : ViewModel() {
    var currentLocation  = MutableLiveData<Location>()
    var locationsList: MutableList<Location> = mutableListOf()
    var setting: Setting = Setting()
}