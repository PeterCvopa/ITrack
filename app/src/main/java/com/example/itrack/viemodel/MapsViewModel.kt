package com.example.itrack.viemodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapsViewModel : ViewModel() {
    var lastLocation  = MutableLiveData<Location>()
    var locationsList: MutableList<Location> = mutableListOf()
    var setting: Setting = Setting()
}