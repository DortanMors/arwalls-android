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
        binding.mapScale.title.setText(R.string.map_scale)
        binding.maxRenderPoints.slider.run {
            value = Settings.mapScale
            valueFrom = Settings.MinMapScale
            valueTo = Settings.MaxMapScale
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) { }

                override fun onStopTrackingTouch(slider: Slider) {
                    Settings.mapScale = slider.value
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