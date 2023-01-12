package ru.ssau.arwalls.ui.screen.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import ru.ssau.arwalls.common.Settings
import ru.ssau.arwalls.rawdepth.R
import ru.ssau.arwalls.rawdepth.databinding.FragmentSettingsBinding

class SettingsFragment: Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentSettingsBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.accuracy.title.setText(R.string.accuracy)
        binding.accuracy.slider.run {
            value = Settings.minConfidence
            valueFrom = Settings.MinAccuracy
            valueTo = Settings.MaxAccuracy
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}

                override fun onStopTrackingTouch(slider: Slider) {
                    Settings.minConfidence = slider.value
                }
            })
        }
        binding.verticalRadius.title.setText(R.string.vertical_radius)
        binding.verticalRadius.slider.run {
            value = Settings.scanVerticalRadius
            valueFrom = Settings.MinVerticalRadius
            valueTo = Settings.MaxVerticalRadius
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}

                override fun onStopTrackingTouch(slider: Slider) {
                    Settings.scanVerticalRadius = slider.value
                }
            })
        }
        binding.heightOffset.title.setText(R.string.height_offset)
        binding.heightOffset.slider.run {
            value = Settings.heightOffset
            valueFrom = Settings.MinHeightOffset
            valueTo = Settings.MaxHeightOffset
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {}

                override fun onStopTrackingTouch(slider: Slider) {
                    Settings.heightOffset = slider.value
                }
            })
        }
        binding.maxRenderPoints.title.setText(R.string.max_render_points)
        binding.maxRenderPoints.slider.run {
            value = Settings.maxNumberOfPointsToRender
            valueFrom = Settings.MinRenderPoints
            valueTo = Settings.MaxRenderPoints
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) { }

                override fun onStopTrackingTouch(slider: Slider) {
                    Settings.maxNumberOfPointsToRender = slider.value
                }
            })
        }
        binding.markerVisibility.title.setText(R.string.marker_visibility)
        binding.markerVisibility.value.isChecked = Settings.markerVisibility
        binding.markerVisibility.value.setOnCheckedChangeListener { _, isChecked ->
            Settings.markerVisibility = isChecked
        }
        binding.save.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}