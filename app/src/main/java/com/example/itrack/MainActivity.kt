package com.example.itrack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
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
import com.google.android.gms.location.LocationResult
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import java.io.Serializable

class MainActivity : AppCompatActivity(), LocationChangeCallBack{

    companion object {
        private val TAG = MainActivity::class.simpleName
        private const val CURRENT_FRAGMENT_KEY = "CURRENT_FRAGMENT_KEY"
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1
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
        tracker = TrackerGPS(this)
        initTracking()
    }

    private fun initTracking() {
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            tracker.startLocationUpdates(this, model.setting.sampleInterval.value!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tracker.stopLocationUpdatesIfExist()
    }

    override fun onLocationReceived(locationResult: LocationResult) {
        Log.d(TAG, "new location -> ${locationResult.lastLocation}")
        model.currentLocation.value = locationResult.lastLocation
    }

    private fun initModel() {
        model = ViewModelProviders.of(this).get(MapsViewModel::class.java)
        model.setting.sampleInterval.observe(this, createSampleRateObserver())
    }

    private fun createSampleRateObserver(): Observer<Int> {
        return Observer {
            Log.d(MainActivity.TAG, "sample rate observer rate: $it ")
            tracker.startLocationUpdates(this, model.setting.sampleInterval.value!!)
        }
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
        if (currentFragment == FragmentType.MAP) {
            super.onBackPressed()
        } else {
            setFragment(FragmentType.MAP)
        }
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
                    tracker.startLocationUpdates(this, model.setting.sampleInterval.value!!)

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

    enum class FragmentType(val fragment: BaseFragment<*>, val titleResource: Int) : Serializable {
        MAP(MapFragment(), R.string.map_menu_text),
        STATS(StatisticsFragment(), R.string.stats_menu_text),
        SETTINGS(SettingFragment(), R.string.settings_menu_text)
    }
}

