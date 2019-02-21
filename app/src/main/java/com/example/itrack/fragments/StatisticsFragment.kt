package com.example.itrack.fragments

import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.example.itrack.R
import com.example.itrack.common.StringHelper
import com.example.itrack.common.base.BaseFragment
import com.example.itrack.common.LocationHelper
import com.example.itrack.viemodel.MapsViewModel

class StatisticsFragment : BaseFragment<MapsViewModel>() {


    lateinit var avgSpeedView: TextView
    lateinit var minSpeedView: TextView
    lateinit var maxSpeedView: TextView
    lateinit var avgAltView: TextView
    lateinit var maxAltView: TextView
    lateinit var minAltView: TextView
    lateinit var distanceView: TextView

    override fun getXmlResource(): Int {
        return R.layout.statistics_fragment_layout
    }

    override fun initializeViews() {
        val stats = LocationHelper.getStatisticsData(model.locationsList)
        avgSpeedView.text = StringHelper.toText(stats.avgSpeed, StringHelper.SpeedUnits.METES_PER_SECOND)
        minSpeedView.text = StringHelper.toText(stats.minSpeed, StringHelper.SpeedUnits.METES_PER_SECOND)
        maxSpeedView.text = StringHelper.toText(stats.maxSpeed, StringHelper.SpeedUnits.METES_PER_SECOND)
        maxAltView.text = StringHelper.toText(stats.maxAlt, StringHelper.DistanceUnit.METES)
        minAltView.text = StringHelper.toText(stats.minAlt, StringHelper.DistanceUnit.METES)
        avgAltView.text = StringHelper.toText(stats.avgAlt, StringHelper.DistanceUnit.METES)
        distanceView.text = StringHelper.toText(stats.distance, StringHelper.DistanceUnit.METES)
    }

    override fun initializeModel(): MapsViewModel {
        return ViewModelProviders.of(this.activity!!).get(MapsViewModel::class.java)
    }

    override fun referenceView(view: View) {
        avgSpeedView = view.findViewById(R.id.speed_avg_value)
        minSpeedView = view.findViewById(R.id.speed_min_value)
        maxSpeedView = view.findViewById(R.id.speed_max_value)
        maxAltView = view.findViewById(R.id.alt_max_value)
        minAltView = view.findViewById(R.id.alt_min_value)
        avgAltView = view.findViewById(R.id.alt_avg_value)
        distanceView = view.findViewById(R.id.distance_total_value)
    }
}