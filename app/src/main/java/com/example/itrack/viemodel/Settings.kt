package com.example.itrack.viemodel

import androidx.lifecycle.MutableLiveData

data class Setting(var color: Int = -435343, var lineSize: Int = 1, var sampleInterval: MutableLiveData<Int> = MutableLiveData(10000))