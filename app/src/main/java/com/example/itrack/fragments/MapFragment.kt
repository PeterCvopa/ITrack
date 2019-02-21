package com.example.itrack.fragments

import android.location.Location
import android.os.Bundle
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
import java.io.Serializable


class MapFragment : BaseFragment<MapsViewModel>(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    companion object {
        private val TAG = MapFragment::class.simpleName

    }

    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var graphBottomSheetLayout: LinearLayout
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var graphSheetBehavior: BottomSheetBehavior<LinearLayout>
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
    private var bottomSheetState: BottomSheetState = BottomSheetState.NONE
    private var initBottomSheetState: BottomSheetState = BottomSheetState.NONE
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
    private val BOTTOM_SHEET_STATE_KEY = "BOTTOM_SHEET_STATE_KEY"

    init {
        locationValidator.addPrecondition(MinimalDistancePrecondition())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(BOTTOM_SHEET_STATE_KEY, bottomSheetState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            initBottomSheetState = it.getSerializable(BOTTOM_SHEET_STATE_KEY) as BottomSheetState
        }
    }

    override fun onAccGraphItemMenuClicked() {
        prepareAndSetGraphData()
        showBottomSheetContainerByType(BottomSheetState.GRAPH)
    }

    override fun onMainDrawerOpened() {
        Log.d(TAG, ".onMainDrawerOpened ")
        showBottomSheetContainerByType(BottomSheetState.NONE)
        closeGraphBottomSheetIfNeeded()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        options = PolylineOptions().width(model.setting.lineSize.toFloat()).color(model.setting.color).geodesic(true)
        drawAllLocations()
        startObservingLocationChange()
        showBottomSheetContainerByType(initBottomSheetState)
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
        graphSheetBehavior = BottomSheetBehavior.from(graphBottomSheetLayout)
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
        northBtn.setOnClickListener(this::onMostNorthButtonClicked)
        southBtn.setOnClickListener(this::onMostSouthButtonClicked)
        westBtn.setOnClickListener(this::onWestSouthButtonClicked)
        eastBtn.setOnClickListener(this::onEastSouthButtonClicked)
    }


    override fun onMarkerClick(marker: Marker?): Boolean {
        val index = marker?.tag as Int
        this.showBottomSheetContainerByType(BottomSheetState.MARKER_DETAILS)
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
            graphBottomSheetLayout = findViewById(R.id.statistics_bottom_sheet)
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

    private fun onMostNorthButtonClicked(view: View) {
        if (!model.locationsList.isEmpty()) {
            val mostNorthLocation = LocationHelper.mostNorth(model.locationsList)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
        }
    }

    private fun onMostSouthButtonClicked(view: View) {
        if (!model.locationsList.isEmpty()) {
            val mostNorthLocation = LocationHelper.mostSouth(model.locationsList)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
        }
    }

    private fun onWestSouthButtonClicked(view: View) {
        if (!model.locationsList.isEmpty()) {
            val mostNorthLocation = LocationHelper.mostWest(model.locationsList)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
        }
    }

    private fun onEastSouthButtonClicked(view: View) {
        if (!model.locationsList.isEmpty()) {
            val mostNorthLocation = LocationHelper.mostEast(model.locationsList)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LocationHelper.locationToLatLng(mostNorthLocation)))
        }
    }

    private fun drawLocation(location: Location) {
        with(location) {
            addSetMarker(this)
            addLine(this)
            currentLocation = this
        }
    }

    private fun bottomSheetContainerVisibility(container: BottomSheetState) {
        when (container) {
            BottomSheetState.NAV_ACTIONS -> {
                actionsContainer.visibility = View.VISIBLE
                markerDetailContainer.visibility = View.GONE
            }
            BottomSheetState.MARKER_DETAILS -> {
                actionsContainer.visibility = View.GONE
                markerDetailContainer.visibility = View.VISIBLE
            }
            BottomSheetState.NONE -> {
            }
            BottomSheetState.GRAPH -> {
                actionsContainer.visibility = View.GONE
                markerDetailContainer.visibility = View.VISIBLE
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
        if (sheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun openGraphBottomSheetIfNeeded() {
        if (graphSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            graphSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun closeGraphBottomSheetIfNeeded() {
        if (graphSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            graphSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
            map.addMarker(
                MarkerOptions()
                    .position(current)
                    .title(resources.getString(R.string.marker_title))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_place_black))
            ).tag = model.locationsList.size - 1
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


    private fun showBottomSheetContainerByType(event: BottomSheetState) {
        when (event) {
            BottomSheetState.NONE -> {
                closeBottomSheetIfNeeded()
                closeGraphBottomSheetIfNeeded()
                bottomSheetState = event
                return
            }
            BottomSheetState.MARKER_DETAILS -> bottomSheetState = when (bottomSheetState) {
                BottomSheetState.MARKER_DETAILS -> {
                    closeBottomSheetIfNeeded()
                    BottomSheetState.NONE
                }
                BottomSheetState.NAV_ACTIONS, BottomSheetState.NONE, BottomSheetState.GRAPH -> {
                    closeBottomSheetIfNeeded()
                    closeGraphBottomSheetIfNeeded()
                    bottomSheetContainerVisibility(BottomSheetState.MARKER_DETAILS)
                    openBottomSheetIfNeeded()
                    BottomSheetState.MARKER_DETAILS
                }
            }
            BottomSheetState.NAV_ACTIONS -> bottomSheetState = when (bottomSheetState) {
                BottomSheetState.NAV_ACTIONS -> {
                    closeBottomSheetIfNeeded()
                    BottomSheetState.NONE
                }
                BottomSheetState.MARKER_DETAILS, BottomSheetState.NONE, BottomSheetState.GRAPH -> {
                    closeBottomSheetIfNeeded()
                    closeGraphBottomSheetIfNeeded()
                    bottomSheetContainerVisibility(BottomSheetState.NAV_ACTIONS)
                    openBottomSheetIfNeeded()
                    BottomSheetState.NAV_ACTIONS
                }
            }
            BottomSheetState.GRAPH -> bottomSheetState = when (bottomSheetState) {
                BottomSheetState.GRAPH -> {
                    closeBottomSheetIfNeeded()
                    BottomSheetState.NONE
                }
                BottomSheetState.MARKER_DETAILS, BottomSheetState.NONE, BottomSheetState.NAV_ACTIONS -> {
                    closeBottomSheetIfNeeded()
                    bottomSheetContainerVisibility(BottomSheetState.NONE)
                    prepareAndSetGraphData()
                    openGraphBottomSheetIfNeeded()
                    BottomSheetState.GRAPH
                }
            }
        }
    }

    private fun onActionFabClicked(view: View) {
        showBottomSheetContainerByType(BottomSheetState.NAV_ACTIONS)
    }

    private fun onLocationFabClicked(view: View) {
        moveCameraToBounds()
    }

    private fun prepareAndSetGraphData() {
        val mostResentAccuracy = mutableListOf<Float>()
        model.locationsList.takeLast(10).forEach {
            mostResentAccuracy.add(it.accuracy)
            it.accuracy
        }

        barChart.setData(ArrayList<Float>(mostResentAccuracy))
    }

    enum class BottomSheetState : Serializable {
        NAV_ACTIONS, MARKER_DETAILS, GRAPH, NONE
    }
}