package com.example.itrack.fragments

import android.os.Bundle
import android.view.View
import com.example.itrack.common.base.BaseFragment
import com.skydoves.colorpickerpreference.ColorPickerView
import android.widget.LinearLayout
import com.example.itrack.R
import com.skydoves.colorpickerpreference.ColorEnvelope
import com.skydoves.colorpickerpreference.ColorListener



class SettingFragment: BaseFragment(){


   private lateinit var colorPickerView : ColorPickerView
    override fun onMainDrawerOpened() {
        //do nothing
    }

    override fun getXmlResource(): Int {
        return R.layout.setting_fragment_layout
    }

    override fun initializeViews() {
    }

    override fun initializeModel() {
        //TODO init viemodel
    }

    override fun referenceView(view: View) {
        colorPickerView = view.findViewById(com.example.itrack.R.id.colorPickerView)
        colorPickerView.setColorListener(ColorListener { colorEnvelope ->
          /*  val linearLayout = view.findViewById(com.example.itrack.R.id.linearLayout)
            linearLayout.setBackgroundColor(colorEnvelope.color)*/
        })
    }

    override fun initParams(savedInstanceState: Bundle?) {
        //TODO probably wont need
    }


}