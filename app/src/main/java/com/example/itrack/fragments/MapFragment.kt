package com.example.itrack.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar

class MapFragment : BaseFragment(), LocationChangeCallBack, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    companion object {
        private val TAG = MapFragment::class.simpleName
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    }

    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var latView: TextView
    private lateinit var longView: TextView
    private lateinit var accuracyView: TextView
    private lateinit var timeView: TextView
    private lateinit var sourceView: TextView

    private lateinit var model: MapsViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var tracker: TrackerGPS

    private var options = PolylineOptions().width(5.0F).color(Color.BLUE).geodesic(true)
    private var currentLocations: Location? = null
    private var locationList: MutableList<Location> = mutableListOf()

    override fun onMarkerClick(marker: Marker?): Boolean {
        this.toggleBottomSheet()
        val index = marker?.tag as Int
        //TODO change icon  marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_black_s))
        this.setBottomSheetData(locationList[index])
        return true
    }

    private fun setBottomSheetData(location: Location) {
        with(location){
            latView.text = latitude.toString()
            longView.text = longitude.toString()
            accuracyView.text = accuracy.toString()
            timeView.text = time.toString()
            sourceView.text = "GPS" //TODO  store type of source
        }
    }

    private fun toggleBottomSheet() {
        if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tracker = TrackerGPS(activity as Activity, this)
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            tracker.getLastKnowLocation(OnCompleteListener { task ->
                currentLocations = task.result?.apply {
                    addLocationAndSetMarker(this)
                }!!
                Log.d(TAG, task.result.toString())
            })
            tracker.startLocationUpdates()
        }
    }

    override fun onLocationReceived(locationResult: LocationResult) {
        Log.d(TAG, ".onLocationReceived :{$locationResult}")
        val location = locationResult.lastLocation
        with(location) {
            addLocationAndSetMarker(this)
            addLine(this)
            currentLocations = this
        }
        moveCameraToBounds()
    }

    private fun addLocationAndSetMarker(location: Location) {
        locationList.add(location)
        val current = LatLng(location.latitude, location.longitude)
        mMap.let { map ->
            map.addMarker(MarkerOptions().position(current).title("Marker").icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_black)))
                .let { marker ->
                    marker.tag = locationList.size - 1
                }
            map.moveCamera(CameraUpdateFactory.newLatLng(current))
        }
    }

    private fun addLine(location: Location) {
        options.apply { add(LatLng(location.latitude, location.longitude)) }
        mMap.addPolyline(options);
    }

    private fun moveCameraToBounds() {
        val builder = LatLngBounds.Builder()
        val mostCurrentPositions = locationList.takeLast(10)
        mostCurrentPositions.forEach {
            builder.include(LatLng(it.latitude, it.longitude))
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100),
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    Log.d(TAG, ".CancelableCallback.onFinish ")
                }

                override fun onCancel() {
                    Log.d(TAG, ".CancelableCallback.onCancel ")
                }
            })

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
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(p0: View, p1: Int) {
                Log.d(TAG, "sheetBehavior.onStateChanged")
            }

            override fun onSlide(p0: View, p1: Float) {
                Log.d(TAG, "sheetBehavior.onSlide")
            }
        })
    }

    override fun initializeModel() {
        model = ViewModelProviders.of(this.activity!!).get(MapsViewModel::class.java)
    }

    override fun referenceView(view: View) {
        bottomSheetLayout = view.findViewById(R.id.bottom_sheet)
        latView = view.findViewById(R.id.bottom_sheet_lat_value)
        longView = view.findViewById(R.id.bottom_sheet_long_value)
        accuracyView = view.findViewById(R.id.bottom_sheet_accuracy_value)
        timeView = view.findViewById(R.id.bottom_sheet_time_value)
        sourceView = view.findViewById(R.id.bottom_sheet_source_value)
    }

    override fun initParams(savedInstanceState: Bundle?) {
        //TODO
    }

    override fun onDestroy() {
        super.onDestroy()
        tracker.stopLocationUpdates()
    }

}