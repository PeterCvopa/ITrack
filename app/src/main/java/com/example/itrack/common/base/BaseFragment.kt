package com.example.itrack.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.itrack.fragments.FragmentCommunicator

abstract class BaseFragment : Fragment(), FragmentCommunicator{
    private val DATA_LOADED_KEY = "DATA_LOADED_KEY"

    protected abstract fun getXmlResource(): Int

    protected abstract fun initializeViews()

    protected abstract fun initializeModel()

    protected abstract fun referenceView(view: View)

    protected abstract fun initParams(savedInstanceState: Bundle?)


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            //TODO
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.initializeModel()
        this.initParams(savedInstanceState)
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getXmlResource(), container, false)
        if (view != null) {
            referenceView(view)
            initializeViews()
        }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
}