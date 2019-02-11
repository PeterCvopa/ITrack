package com.example.itrack

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.itrack.common.base.BaseFragment
import com.example.itrack.fragments.MapFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {


    private lateinit var navigationView: NavigationView
    private lateinit var mDrawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        attachFragment(savedInstanceState)
        navigationView = findViewById(R.id.navigation_main)
        mDrawerLayout = findViewById(R.id.drawer)
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_history -> {
                    //mDrawerLayout.openDrawer(GravityCompat.START)
                    Log.d(TAG, "history_item_clicked")
                    true
                }
                R.id.nav_stats -> {
                    //mDrawerLayout.openDrawer(GravityCompat.START)
                    Log.d(TAG, "stats_item_clicked")
                    true
                }
                else -> throw IllegalArgumentException("Does not know menu item with id: ${it.itemId}")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_history -> {
                //TODO show history fragment
                //mDrawerLayout.openDrawer(GravityCompat.START)
                Log.d(TAG, "history_item_clicked")
                true
            }
            R.id.nav_stats -> {
                //TODO show stats fragment
                //mDrawerLayout.openDrawer(GravityCompat.START)
                Log.d(TAG, "stats_item_clicked")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getFragment(): BaseFragment {
        return MapFragment()
    }

    private fun attachFragment(savedInstanceState: Bundle?) {
        val fragment: BaseFragment
        if (savedInstanceState == null) {
            fragment = getFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_container, fragment, TAG)
                .disallowAddToBackStack()
                .commit()
        } else {
            fragment = supportFragmentManager.findFragmentByTag(TAG) as BaseFragment
            supportFragmentManager
                .beginTransaction()
                .attach(fragment)
                .disallowAddToBackStack()
                .commit()
        }
    }

    fun reloadFragment(fragment: BaseFragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_container, fragment, TAG)
            .addToBackStack(null)
            .commit()
    }

    fun popFromStack() {
        supportFragmentManager.popBackStackImmediate()
        val f = this.supportFragmentManager.findFragmentById(R.id.main_container)
    }

    companion object {
        private val TAG = MainActivity::class.simpleName
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    }
}

