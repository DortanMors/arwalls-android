package ru.ssau.arwalls.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import ru.ssau.arwalls.rawdepth.databinding.FragmentMapBinding
import kotlinx.coroutines.launch

class MapFragment: Fragment() {
    private val viewModel: MapViewModel by viewModels()
    private lateinit var binding: FragmentMapBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentMapBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("HARDCODE", "onViewCreated")
        binding.map.post {
            binding.map.clear()
        }
        binding.redrawMap.setOnClickListener {
            binding.map.clear()
        }
        lifecycleScope.launchWhenResumed {
            Log.d("HARDCODE", "launchWhenResumed")
            lifecycleScope.launch {
                viewModel.newMapPointsState.collect {
                    Log.d("HARDCODE", "collect")
                    binding.map.setMapState(it)
                }
            }
        }
    }
}
