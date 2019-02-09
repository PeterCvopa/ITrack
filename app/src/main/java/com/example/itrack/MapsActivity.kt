package com.example.itrack

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.example.itrack.BuildConfig.APPLICATION_ID
import com.example.itrack.location.TrackerGPS
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationCallback: LocationCallback
    private lateinit var tracker: TrackerGPS

    private lateinit var currentLocations: Location


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    Log.d(TAG, "Coordinates lat:{${location.latitude}}")
                    Log.d(TAG, "Coordinates long:{${location.longitude}}")
                    Log.d(TAG, "Coordinates long:{${location.altitude}}")
                    Log.d(TAG, "Coordinates long:{${location.time}")
                    Log.d(TAG, "Coordinates long:{${location.accuracy}")
                    addNewMarkerAndFocus(location.latitude, location.longitude)
                }
            }
        }
        tracker = TrackerGPS(this)
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            tracker.getLastKnowLocation(OnCompleteListener<Location> { task ->
                currentLocations = task.result?.apply {
                    addNewMarkerAndFocus(this.latitude, this.longitude)
                }!!
                Log.d(TAG, task.result.toString())
            })
            tracker.startLocationUpdates(locationCallback)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> tracker.getLastKnowLocation(OnCompleteListener { task ->
                    currentLocations = task.result!!
                    Log.d(TAG, task.result.toString())
                })
                else -> {
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        View.OnClickListener {
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        })
                }
            }
        }
    }

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED


    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
        } else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    private fun showSnackbar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), getString(snackStrId), LENGTH_INDEFINITE)
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this, arrayOf(ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun addNewMarkerAndFocus(lat: Double, lon: Double) {
        val current = LatLng(lat, lon)
        mMap.let {
            it.addMarker(MarkerOptions().position(current).title("Marker"))
            it.moveCamera(CameraUpdateFactory.newLatLng(current))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    companion object {
        private val TAG = MapsActivity::class.simpleName
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    }
}

