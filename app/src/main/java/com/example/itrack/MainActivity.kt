package com.example.itrack

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.itrack.common.base.BaseFragment
import com.example.itrack.fragments.MapFragment

class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "oncreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        attachFragment(savedInstanceState)
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

