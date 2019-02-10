package com.example.itrack.viemodel

import android.location.Location
import androidx.lifecycle.ViewModel

class MapsViewModel : ViewModel (){
  var  locationsList : MutableList<Location> = mutableListOf()
}