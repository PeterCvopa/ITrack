package com.example.itrack.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.ViewModelProviders
import com.example.itrack.BuildConfig
import com.example.itrack.R
import com.example.itrack.common.base.BaseFragment
import com.example.itrack.location.LocationChangeCallBack
import com.example.itrack.location.TrackerGPS
import com.example.itrack.viemodel.MapsViewModel
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar

class MapFragment : BaseFragment(), LocationChangeCallBack, OnMapReadyCallback {

    companion object {
        private val TAG = MapFragment::class.simpleName
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    }

    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var model: MapsViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var tracker: TrackerGPS

    private var currentLocations: Location? = null
    private var locationList: MutableList<Location> = mutableListOf()

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tracker = TrackerGPS(activity as Activity, this)
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            tracker.getLastKnowLocation(OnCompleteListener { task ->
                currentLocations = task.result?.apply {
                    addNewMarkerAndFocus(this.latitude, this.longitude)
                }!!
                Log.d(TAG, task.result.toString())
            })
            tracker.startLocationUpdates()
        }
    }

    override fun onLocationReceived(locationResult: LocationResult) {
        var lastKnow = locationResult.lastLocation
        Log.d(TAG, "last Know :{${locationResult.lastLocation}}")
        locationList.add(locationResult.lastLocation)
        addNewMarkerAndFocus(lastKnow.latitude, lastKnow.longitude)
        Log.d(TAG, "number of locations:{${locationList.size}}")
        for (location in locationResult.locations) {
            Log.d(TAG, "Coordinates lat:{${location.latitude}}")
            Log.d(TAG, "Coordinates long:{${location.longitude}}")
            Log.d(TAG, "Coordinates alt:{${location.altitude}}")
            Log.d(TAG, "Coordinates time:{${location.time}")
            Log.d(TAG, "Coordinates accur:{${location.accuracy}")
            addNewMarkerAndFocus(location.latitude, location.longitude)
        }
    }

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(
            activity as Activity, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED


    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity as Activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
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
        val snackbar = Snackbar.make(
            activity?.findViewById(android.R.id.content)!!, getString(snackStrId),
            Snackbar.LENGTH_INDEFINITE
        )
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            activity as Activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
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
                    showSnackbar(
                        R.string.permission_denied_explanation, R.string.settings,
                        View.OnClickListener {
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        })
                }
            }
        }
    }

    override fun getXmlResource(): Int {
        return R.layout.map_fragment_layout
    }

    override fun initializeViews() {
        supportMapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        supportMapFragment.getMapAsync(this)
    }

    override fun initializeModel() {
        model = ViewModelProviders.of(this.activity!!).get(MapsViewModel::class.java)
    }

    override fun referenceView(view: View) {
        //TODO
    }

    override fun initParams(savedInstanceState: Bundle?) {
        //TODO
    }


}