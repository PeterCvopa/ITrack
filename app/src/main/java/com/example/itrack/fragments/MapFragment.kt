package com.example.itrack.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.itrack.BuildConfig
import com.example.itrack.R
import com.example.itrack.common.StringHelper
import com.example.itrack.common.base.BaseFragment
import com.example.itrack.common.base.LocationHelper
import com.example.itrack.location.TrackerCommunicator
import com.example.itrack.viemodel.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MapFragment : BaseFragment<MapsViewModel>(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

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
    private lateinit var fabAction: FloatingActionButton
    private lateinit var fabLocation: FloatingActionButton
    private lateinit var markerDetailContainer: LinearLayout
    private lateinit var actionsContainer: ConstraintLayout
    private var shownBottomSheetContainer: BottomSheetContainers = BottomSheetContainers.NONE
    private lateinit var northBtn: MaterialButton
    private lateinit var eastBtn: MaterialButton
    private lateinit var southBtn: MaterialButton
    private lateinit var westBtn: MaterialButton

    private lateinit var trackerCommunicator: TrackerCommunicator

    private lateinit var mMap: GoogleMap
    private lateinit var options: PolylineOptions
    private var currentLocations: Location? = null
    private lateinit var locationChangeObserver: Observer<Location>


    override fun onMainDrawerOpened() {
        Log.d(TAG, ".onMainDrawerOpened ")
        showBottomSheetContainerByType(BottomSheetContainers.NONE)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        options = PolylineOptions().width(model.setting.lineSize.toFloat()).color(model.setting.color).geodesic(true)
        initTracking()
        startObservingLocationChange()
    }

    override fun onDestroy() {
        super.onDestroy()
        model.lastLocation.removeObserver(locationChangeObserver)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        trackerCommunicator = context as TrackerCommunicator
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
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) ->
                    trackerCommunicator.startTracking()

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
        fabAction.setOnClickListener(this::onActionFabClicked)
        fabLocation.setOnClickListener(this::onLocationFabClicked)

        northBtn.setOnClickListener(this::onMostNorthButtonCliked)
        southBtn.setOnClickListener(this::onMostSouthButtonCliked)
        westBtn.setOnClickListener(this::onWestSouthButtonCliked)
        eastBtn.setOnClickListener(this::onEastSouthButtonCliked)
    }

    private fun onMostNorthButtonCliked(view: View) {
        val mostNorthLocation = LocationHelper.mostNorth(model.locationsList)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
    }

    private fun onMostSouthButtonCliked(view: View) {
        val mostNorthLocation = LocationHelper.mostSouth(model.locationsList)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
    }

    private fun onWestSouthButtonCliked(view: View) {
        val mostNorthLocation = LocationHelper.mostWest(model.locationsList)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
    }

    private fun onEastSouthButtonCliked(view: View) {
        val mostNorthLocation = LocationHelper.mostEast(model.locationsList)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        val index = marker?.tag as Int
        this.showBottomSheetContainerByType(BottomSheetContainers.MARKER_DETAILS)
        this.setBottomSheetData(model.locationsList[index])
        return true
    }

    private fun newLocationReceived(location: Location) {
        with(location) {
            addSetMarker(this)
            addLine(this)
            currentLocations = this
        }
    }

    private fun bottomSheetContainerVisibility(container: BottomSheetContainers) {
        when (container) {
            BottomSheetContainers.ACTIONS -> {
                actionsContainer.visibility = View.VISIBLE
                markerDetailContainer.visibility = View.GONE
            }
            BottomSheetContainers.MARKER_DETAILS -> {
                actionsContainer.visibility = View.GONE
                markerDetailContainer.visibility = View.VISIBLE
            }
        }
    }

    override fun initializeModel(): MapsViewModel {
        return activity?.run {
            ViewModelProviders.of(this).get(MapsViewModel::class.java)
        } ?: throw IllegalStateException("model was not init")
    }

    override fun referenceView(view: View) {
        with(view) {
            bottomSheetLayout = findViewById(R.id.bottom_sheet)
            latView = findViewById(R.id.bottom_sheet_lat_value)
            longView = findViewById(R.id.bottom_sheet_long_value)
            accuracyView = findViewById(R.id.bottom_sheet_accuracy_value)
            timeView = findViewById(R.id.bottom_sheet_time_value)
            sourceView = findViewById(R.id.bottom_sheet_source_value)
            fabAction = findViewById(R.id.action_fab)
            fabLocation = findViewById(R.id.location_fab)
            markerDetailContainer = findViewById(R.id.marker_detail_container)
            actionsContainer = findViewById(R.id.actions_container)
            northBtn = findViewById(R.id.north_btn)
            eastBtn = findViewById(R.id.east_button)
            southBtn = findViewById(R.id.south_button)
            westBtn = findViewById(R.id.west_button)
        }
    }

    override fun initParams(savedInstanceState: Bundle?) {
        //TODO remove
    }

    private fun createLocationObserver(): Observer<Location> {
        return Observer {
            model.locationsList.add(it)
            Log.d(TAG, "${model.locationsList.size} locations stored")
            newLocationReceived(it)
        }
    }

    private fun startObservingLocationChange() {
        locationChangeObserver = createLocationObserver()
        model.lastLocation.observe(this, locationChangeObserver)
    }

    private fun setBottomSheetData(location: Location) {
        with(location) {
            latView.text = latitude.toString()
            longView.text = longitude.toString()
            accuracyView.text = accuracy.toString()
            timeView.text = StringHelper.dateToText(time)
            sourceView.text = "GPS" //TODO  store type of source
        }
    }

    private fun openBottomSheetIfNeeded() {
        if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun closeBottomSheetIfNeeded() {
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun initTracking() {
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            trackerCommunicator.startTracking()
        }
    }

    private fun drawAllStoredMarkers() {
        //TODO  draw all marker when coming from other fragment
    }

    private fun drawAllStoredLines() {

    }

    private fun addSetMarker(location: Location) {
        val current = LatLng(location.latitude, location.longitude)
        mMap.let { map ->
            map.addMarker(MarkerOptions().position(current).title("Marker").icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_black)))
                .let { marker ->
                    marker.tag = model.locationsList.size - 1
                }

        }
    }

    private fun addLine(location: Location) {
        options.apply { add(LatLng(location.latitude, location.longitude)) }
        mMap.addPolyline(options)
    }

    private fun moveCameraToBounds() {
        val builder = LatLngBounds.Builder()
        val mostCurrentPositions = model.locationsList.takeLast(10)
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

    private fun checkPermissions() = ActivityCompat.checkSelfPermission(
        activity as Activity,
        Manifest.permission.ACCESS_COARSE_LOCATION
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

    private fun showBottomSheetContainerByType(event: BottomSheetContainers) {
        if (event == BottomSheetContainers.NONE) {
            closeBottomSheetIfNeeded()
            shownBottomSheetContainer = event
            return
        }
        if (event == BottomSheetContainers.MARKER_DETAILS) {
            shownBottomSheetContainer = when (shownBottomSheetContainer) {
                BottomSheetContainers.MARKER_DETAILS -> {
                    closeBottomSheetIfNeeded()
                    BottomSheetContainers.NONE
                }
                BottomSheetContainers.ACTIONS, BottomSheetContainers.NONE -> {
                    closeBottomSheetIfNeeded()
                    bottomSheetContainerVisibility(BottomSheetContainers.MARKER_DETAILS)
                    openBottomSheetIfNeeded()
                    BottomSheetContainers.MARKER_DETAILS
                }
            }
        } else {
            shownBottomSheetContainer = when (shownBottomSheetContainer) {
                BottomSheetContainers.ACTIONS -> {
                    closeBottomSheetIfNeeded()
                    BottomSheetContainers.NONE
                }
                BottomSheetContainers.MARKER_DETAILS, BottomSheetContainers.NONE -> {
                    closeBottomSheetIfNeeded()
                    bottomSheetContainerVisibility(BottomSheetContainers.ACTIONS)
                    openBottomSheetIfNeeded()
                    BottomSheetContainers.ACTIONS
                }
            }
        }
    }

    private fun onActionFabClicked(view: View) {
        showBottomSheetContainerByType(BottomSheetContainers.ACTIONS)
    }

    private fun onLocationFabClicked(view: View) {
        moveCameraToBounds()
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            activity as Activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    enum class BottomSheetContainers {
        ACTIONS, MARKER_DETAILS, NONE
    }
}