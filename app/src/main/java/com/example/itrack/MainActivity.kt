package com.example.itrack

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import com.example.itrack.common.base.BaseFragment
import com.example.itrack.fragments.FragmentCommunicator
import com.example.itrack.fragments.MapFragment
import com.example.itrack.fragments.SettingFragment
import com.example.itrack.fragments.StatisticsFragment
import com.example.itrack.location.LocationChangeCallBack
import com.example.itrack.location.Tracker
import com.example.itrack.location.TrackerGPS
import com.example.itrack.viemodel.MapsViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.io.Serializable

class MainActivity : AppCompatActivity(), LocationChangeCallBack, OnFailureListener {

    companion object {
        private val TAG = MainActivity::class.simpleName
        private const val CURRENT_FRAGMENT_KEY = "CURRENT_FRAGMENT_KEY"
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 0x1
    }

    private lateinit var tracker: Tracker

    private lateinit var navigationView: NavigationView
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var fragmentComm: FragmentCommunicator
    private var currentFragment: FragmentType = FragmentType.MAP
    private lateinit var drawerSubTitleView: TextView
    private lateinit var model: MapsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, ".oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        attachFragment(savedInstanceState)
        initModel()
        initializeViews()
        initTracker()
        initTracking()
    }

    override fun onLocationReceived(locationResult: LocationResult) {
        Log.d(TAG, "new location -> ${locationResult.lastLocation}")
        model.currentLocation.value = locationResult.lastLocation
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(CURRENT_FRAGMENT_KEY, currentFragment)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.let {
            currentFragment = it.getSerializable(CURRENT_FRAGMENT_KEY) as FragmentType
        }
    }

    override fun onBackPressed() {
        when (currentFragment) {
            FragmentType.MAP -> super.onBackPressed()
            FragmentType.SETTINGS -> {
                if ((currentFragment.fragment as SettingFragment).isSettingChanged()) {
                    initTracking()
                }
                setFragment(FragmentType.MAP)
            }
            else -> setFragment(FragmentType.MAP)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(MainActivity.TAG, "onRequestPermissionResult")
        if (requestCode == MainActivity.REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(MainActivity.TAG, "User interaction was cancelled.")
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) ->
                    tracker.startLocationUpdates(model.setting.sampleInterval, this, this)

                else -> {
                    showSnackBar(
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

    override fun onFailure(e: Exception) {
        val statusCode = (e as ApiException).statusCode
        when (statusCode) {
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                onSettingNeedResolution(e)
            }
            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                onSettingFail()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tracker.stopLocationUpdatesIfExist()
    }

    private fun initTracker() {
        tracker = TrackerGPS(this)
    }

    private fun initTracking() {
        Log.d(TAG, ".initializing tracking")
        if (checkPermissions()) {
            tracker.startLocationUpdates(model.setting.sampleInterval, this, this)
        } else {
            requestPermissions()
        }
    }

    private fun initModel() {
        model = ViewModelProviders.of(this).get(MapsViewModel::class.java)
    }

    private fun initializeViews() {
        navigationView = findViewById(R.id.navigation_main)
        mDrawerLayout = findViewById(R.id.drawer)
        drawerSubTitleView = navigationView.getHeaderView(0).findViewById(R.id.drawer_subtitle)
        drawerSubTitleView.setText(currentFragment.titleResource)
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_map -> {
                    with(FragmentType.MAP) {
                        setFragmentIfNeeded(this)
                        drawerSubTitleView.setText(this.titleResource)
                    }
                    mDrawerLayout.closeDrawers()
                    true
                }
                R.id.nav_stats -> {
                    with(FragmentType.STATS) {
                        setFragmentIfNeeded(this)
                        drawerSubTitleView.setText(this.titleResource)
                    }

                    mDrawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    with(FragmentType.SETTINGS) {
                        setFragmentIfNeeded(this)
                        drawerSubTitleView.setText(this.titleResource)
                    }
                    mDrawerLayout.closeDrawers()
                    true
                }
                R.id.nav_history -> {
                    with(FragmentType.MAP) {
                        setFragmentIfNeeded(this)
                        drawerSubTitleView.setText(this.titleResource)
                    }
                    fragmentComm.onAccGraphItemMenuClicked()
                    mDrawerLayout.closeDrawers()
                    true
                }
                else -> throw IllegalArgumentException("Does not know menu item with id: ${it.itemId}")
            }
        }
        mDrawerLayout.addDrawerListener(
            object : DrawerLayout.DrawerListener {
                override fun onDrawerStateChanged(newState: Int) {
                    //do nothing
                }

                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    //do nothing
                }

                override fun onDrawerClosed(drawerView: View) {
                    //do nothing
                }

                override fun onDrawerOpened(drawerView: View) {
                    fragmentComm.onMainDrawerOpened()
                }
            })
    }

    private fun getFragment(): BaseFragment<*> {
        return MapFragment()
    }

    private fun attachFragment(savedInstanceState: Bundle?) {
        val fragment: BaseFragment<*>
        if (savedInstanceState == null) {
            fragment = getFragment()
            fragmentComm = fragment
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_container, fragment, TAG)
                .disallowAddToBackStack()
                .commit()
        } else {
            fragment = supportFragmentManager.findFragmentByTag(TAG) as BaseFragment<*>
            fragmentComm = fragment
            supportFragmentManager
                .beginTransaction()
                .attach(fragment)
                .disallowAddToBackStack()
                .commit()
        }
    }

    private fun setFragmentIfNeeded(fragmentType: FragmentType) {
        if (currentFragment != fragmentType) {
            setFragment(fragmentType)
        }
    }

    private fun setFragment(fragmentType: FragmentType) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_container, fragmentType.fragment, TAG)
            .disallowAddToBackStack()
            .commit()
        currentFragment = fragmentType
    }

    /*   Permissions section*/

    private fun checkPermissions() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PermissionChecker.PERMISSION_GRANTED


    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            Log.i(MainActivity.TAG, "Displaying permission rationale to provide additional context.")
        } else {
            Log.i(MainActivity.TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            MainActivity.REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun showSnackBar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackBar = Snackbar.make(
            this.findViewById(android.R.id.content)!!, getString(snackStrId),
            Snackbar.LENGTH_INDEFINITE
        )
        if (actionStrId != 0 && listener != null) {
            snackBar.setAction(getString(actionStrId), listener)
        }
        snackBar.show()
    }


    private fun onSettingNeedResolution(e: Exception) {
        Log.d(TAG, "Location settings are not satisfied. Attempting to upgrade " + "location settings ")
        try {
            val rae = e as ResolvableApiException
            rae.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
        } catch (sie: IntentSender.SendIntentException) {
            Log.e(TAG, sie.message)
        }
    }

    private fun onSettingFail() {
        Toast.makeText(this, this.resources.getString(R.string.insufficient_setting_message), Toast.LENGTH_LONG)
            .show()
    }

    enum class FragmentType(val fragment: BaseFragment<*>, val titleResource: Int) : Serializable {
        MAP(MapFragment(), R.string.map_menu_text),
        STATS(StatisticsFragment(), R.string.stats_menu_text),
        SETTINGS(SettingFragment(), R.string.settings_menu_text)
    }
}

