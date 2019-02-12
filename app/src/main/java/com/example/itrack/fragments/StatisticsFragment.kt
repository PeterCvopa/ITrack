package com.example.itrack.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.example.itrack.R
import com.example.itrack.common.base.BaseFragment
import com.example.itrack.viemodel.MapsViewModel

class StatisticsFragment : BaseFragment<MapsViewModel>() {
    override fun onMainDrawerOpened() {
        //do nothing
    }

    override fun getXmlResource(): Int {
        return R.layout.statistics_fragment_layout
    }

    override fun initializeViews() {
        //TODO do later
    }

    override fun initializeModel(): MapsViewModel {
        return ViewModelProviders.of(this.activity!!).get(MapsViewModel::class.java)
    }

    override fun referenceView(view: View) {
        //TODO do later
    }

    override fun initParams(savedInstanceState: Bundle?) {
        //TODO probably wont need
    }
}