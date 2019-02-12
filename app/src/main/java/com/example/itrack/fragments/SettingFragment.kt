package com.example.itrack.fragments

import android.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProviders
import com.example.itrack.R
import com.example.itrack.common.base.BaseFragment
import com.example.itrack.viemodel.MapsViewModel
import com.skydoves.colorpickerpreference.ColorPickerDialog


class SettingFragment : BaseFragment<MapsViewModel>() {

    companion object {
        private val TAG = SettingFragment::class.simpleName
    }

    private lateinit var lineWidthSpinner: Spinner
    private lateinit var samplingIntervalSpinner: Spinner
    private lateinit var colorImageView: View
    private lateinit var colorRowView: ConstraintLayout


    private var colorPickerDialog: AlertDialog? = null

    override fun initializeViews() {
        colorRowView.setOnClickListener(this::onColorRowClicked)
        colorImageView.setBackgroundColor(model.setting.color)
        @Suppress("DEPRECATION") val builder =
            ColorPickerDialog.Builder(activity, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
        with(builder) {
            setTitle(R.string.color_picker_dialog_tile)
            setPreferenceName("MyColorPickerDialog")
            setPositiveButton(getString(R.string.confirm)) { colorEnvelope ->
                Log.d(TAG, "Selected color :${colorEnvelope.color}")
                model.setting.color = colorEnvelope.color
                colorImageView.setBackgroundColor(model.setting.color)
            }
            setNegativeButton(
                getString(R.string.cancel)
            ) { dialogInterface, i -> dialogInterface.dismiss() }
            colorPickerDialog = create()
        }
        with(lineWidthSpinner) {
            adapter = ArrayAdapter<LineSize>(
                activity,
                android.R.layout.simple_spinner_dropdown_item, LineSize.values()
            )
        }
        lineWidthSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.setting.lineSize = (parent?.getItemAtPosition(position) as LineSize).value
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        with(samplingIntervalSpinner) {
            adapter = ArrayAdapter<SamplingIntervals>(
                activity,
                android.R.layout.simple_spinner_dropdown_item, SamplingIntervals.values()
            )
        }
        samplingIntervalSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.setting.sampleInterval.value = (parent?.getItemAtPosition(position) as SamplingIntervals).value
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    override fun onMainDrawerOpened() {
        // no action required here
    }

    override fun getXmlResource(): Int {
        return R.layout.setting_fragment_layout
    }

    override fun initializeModel(): MapsViewModel {
        return ViewModelProviders.of(this.activity!!).get(MapsViewModel::class.java)
    }

    override fun referenceView(view: View) {
        with(view) {
            lineWidthSpinner = this.findViewById(R.id.lineSpinner)
            colorImageView = this.findViewById(R.id.pickedColorView)
            colorRowView = this.findViewById(R.id.color_setting_container)
            samplingIntervalSpinner = this.findViewById(R.id.sample_interval_spinner)
        }
    }


    private fun onColorRowClicked(view: View) {
        colorPickerDialog?.show()
    }

    private enum class LineSize(var value: Int) {
        ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5);
        override fun toString(): String {
            return "${value}dp"
        }
    }

    private enum class SamplingIntervals(var value: Int, var displayName: String) {
        SECONDS_5(5 * 1000, "5 sec"),
        SECONDS_10(10 * 1000, "10 sec"),
        SECONDS_30(30 * 1000, "30 sec"),
        MINUTE_1(1000 * 60, "1 min"),
        MINUTE_5(1000 * 60 * 5, "5 min");
        override fun toString(): String {
            return displayName
        }
    }
}