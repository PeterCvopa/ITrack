package com.example.itrack.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.example.itrack.fragments.FragmentCommunicator

abstract class BaseFragment<MODEL : ViewModel> : Fragment(), FragmentCommunicator {

    protected lateinit var model: MODEL

    protected abstract fun getXmlResource(): Int

    protected abstract fun initializeViews()

    protected abstract fun initializeModel(): MODEL

    protected abstract fun referenceView(view: View)



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null) {
            //TODO
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = this.initializeModel()
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
}