package com.example.itrack.fragments

import android.os.Bundle
import android.view.View
import com.example.itrack.R
import com.example.itrack.common.base.BaseFragment

class StatisticsFragment : BaseFragment(){
    override fun onMainDrawerOpened() {
        //do nothing
    }

    override fun getXmlResource(): Int {
      return R.layout.statistics_fragment_layout
    }

    override fun initializeViews() {
        //TODO do later
    }

    override fun initializeModel() {
        //TODO init viemodel
    }

    override fun referenceView(view: View) {
        //TODO do later
    }

    override fun initParams(savedInstanceState: Bundle?) {
        //TODO probably wont need
    }

}