package com.example.itrack.fragments

import android.location.Location
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.itrack.R
import com.example.itrack.common.LocationHelper
import com.example.itrack.common.StringHelper
import com.example.itrack.common.base.BaseFragment
import com.example.itrack.common.componets.BarChart
import com.example.itrack.location.LocationValidator
import com.example.itrack.location.MinimalDistancePrecondition
import com.example.itrack.viemodel.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapFragment : BaseFragment<MapsViewModel>(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    companion object {
        private val TAG = MapFragment::class.simpleName

    }

    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var statisticsbottomSheetLayout: LinearLayout
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var statisticsSheetBehavior: BottomSheetBehavior<LinearLayout>
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
    private lateinit var barChart: BarChart

    private lateinit var mMap: GoogleMap
    private lateinit var options: PolylineOptions
    private var currentLocation: Location? = null
    private lateinit var locationChangeObserver: Observer<Location>
    private val locationValidator: LocationValidator = LocationValidator()

    init {
        locationValidator.addPrecondition(MinimalDistancePrecondition())
    }
    override fun onStatisticsItemMenuClicked() {
        toggleStatisticsBottomSheet()
    }

    override fun onMainDrawerOpened() {
        Log.d(TAG, ".onMainDrawerOpened ")
        showBottomSheetContainerByType(BottomSheetContainers.NONE)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        options = PolylineOptions().width(model.setting.lineSize.toFloat()).color(model.setting.color).geodesic(true)
        drawAllLocations()
        startObservingLocationChange()
    }

    override fun onDestroy() {
        super.onDestroy()
        model.currentLocation.removeObserver(locationChangeObserver)
    }

    override fun getXmlResource(): Int {
        return R.layout.map_fragment_layout
    }

    override fun initializeViews() {
        supportMapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        supportMapFragment.getMapAsync(this)
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)
        statisticsSheetBehavior = BottomSheetBehavior.from(statisticsbottomSheetLayout)
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
        barChart.setData(listOf(10, 20, 30, 40, 50, 4, 32, 43, 21, 4))
    }

    private fun onMostNorthButtonCliked(view: View) {
        if (!model.locationsList.isEmpty()) {
            val mostNorthLocation = LocationHelper.mostNorth(model.locationsList)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
        }
    }

    private fun onMostSouthButtonCliked(view: View) {
        if (!model.locationsList.isEmpty()) {
            val mostNorthLocation = LocationHelper.mostSouth(model.locationsList)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
        }
    }

    private fun onWestSouthButtonCliked(view: View) {
        if (!model.locationsList.isEmpty()) {
            val mostNorthLocation = LocationHelper.mostWest(model.locationsList)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
        }
    }

    private fun onEastSouthButtonCliked(view: View) {
        if (!model.locationsList.isEmpty()) {
            val mostNorthLocation = LocationHelper.mostEast(model.locationsList)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        val index = marker?.tag as Int
        this.showBottomSheetContainerByType(BottomSheetContainers.MARKER_DETAILS)
        this.setBottomSheetData(model.locationsList[index])
        return true
    }

    override fun initializeModel(): MapsViewModel {
        return activity?.run {
            ViewModelProviders.of(this).get(MapsViewModel::class.java)
        } ?: throw IllegalStateException("model was not init")
    }

    override fun referenceView(view: View) {
        with(view) {
            bottomSheetLayout = findViewById(R.id.bottom_sheet)
            statisticsbottomSheetLayout = findViewById(R.id.statistics_bottom_sheet)
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
            barChart = findViewById(R.id.bar_chart)
        }
    }

    private fun drawLocation(location: Location) {
        with(location) {
            addSetMarker(this)
            addLine(this)
            currentLocation = this
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
            BottomSheetContainers.NONE -> {
            }
        }
    }

    private fun createLocationObserver(): Observer<Location> {
        return Observer {
            var oldLocation: Location? = currentLocation
            if (oldLocation == null || locationValidator.validate(oldLocation, it)) {
                model.locationsList.add(it)
                drawLocation(it)
                Log.d(TAG, "${model.locationsList.size} locations stored")
            } else {
                Log.d(TAG, "new location location is no valid ")
            }
        }
    }

    private fun startObservingLocationChange() {
        locationChangeObserver = createLocationObserver()
        model.currentLocation.observe(this, locationChangeObserver)
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

    private fun toggleStatisticsBottomSheet() {
        if (statisticsSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            statisticsSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            statisticsSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun drawAllLocations() {
        model.locationsList.forEach {
            drawLocation(it)
        }
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
        if (!mostCurrentPositions.isEmpty()) {
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

    enum class BottomSheetContainers {
        ACTIONS, MARKER_DETAILS, NONE
    }
}