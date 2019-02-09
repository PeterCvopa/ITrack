package com.example.itrack.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.tasks.Task

class NetworkGPSGStrategy(
    val locationListener: LocationListener,
    private val ctx: Context
) : LocationStrategy {
    override fun getLocation(): Location {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }




}