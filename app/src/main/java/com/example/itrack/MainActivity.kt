package com.example.itrack

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.itrack.common.base.BaseFragment
import com.example.itrack.fragments.FragmentCommunicator
import com.example.itrack.fragments.MapFragment
import com.example.itrack.fragments.SettingFragment
import com.example.itrack.fragments.StatisticsFragment
import com.google.android.material.navigation.NavigationView
import java.io.Serializable

class MainActivity : AppCompatActivity() {

    private val CURRENT_FRAGMENT_KEY = "CURRENT_FRAGMENT_KEY"
    private lateinit var navigationView: NavigationView
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var fragmentComm: FragmentCommunicator
    private var currentFragment: FragmentType = FragmentType.MAP
    private lateinit var drawerSubTitleView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        attachFragment(savedInstanceState)
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
                    Log.d(TAG, "maps_item_clicked")
                    true
                }
                R.id.nav_stats -> {
                    with(FragmentType.STATS) {
                        setFragmentIfNeeded(this)
                        drawerSubTitleView.setText(this.titleResource)
                    }

                    mDrawerLayout.closeDrawers()
                    Log.d(TAG, "stats_item_clicked")
                    true
                }
                R.id.nav_settings -> {
                    with(FragmentType.SETTINGS) {
                        setFragmentIfNeeded(this)
                        drawerSubTitleView.setText(this.titleResource)
                    }
                    mDrawerLayout.closeDrawers()
                    Log.d(TAG, "setting_item_clicked")
                    true
                }
                R.id.nav_history -> {
                    //TODO may show stats in bottom sheet   old-> setFragmentIfNeeded(FragmentType.HISTORY)
                    Log.d(TAG, "history_item_clicked")
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

    private fun getFragment(): BaseFragment {
        return MapFragment()
    }

    private fun attachFragment(savedInstanceState: Bundle?) {
        val fragment: BaseFragment
        if (savedInstanceState == null) {
            fragment = getFragment()
            fragmentComm = fragment
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_container, fragment, TAG)
                .disallowAddToBackStack()
                .commit()
        } else {
            fragment = supportFragmentManager.findFragmentByTag(TAG) as BaseFragment
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

    companion object {
        private val TAG = MainActivity::class.simpleName
    }

    override fun onBackPressed() {
        if (currentFragment == FragmentType.MAP) {
            super.onBackPressed()
        } else {
            setFragment(FragmentType.MAP)
        }
    }

    enum class FragmentType(val fragment: BaseFragment, val titleResource: Int) : Serializable {
        MAP(MapFragment(), R.string.map_menu_text),
        STATS(StatisticsFragment(), R.string.stats_menu_text),
        SETTINGS(SettingFragment(), R.string.settings_menu_text)
    }
}

