package ru.ssau.arwalls.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ssau.arwalls.data.BitmapSaver.saveBitmapToGallery
import ru.ssau.arwalls.data.RawMapStore
import ru.ssau.arwalls.rawdepth.R
import ru.ssau.arwalls.rawdepth.databinding.FragmentMapBinding
import java.util.UUID

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
            binding.map.centralize()
        }
        binding.redrawMap.setOnClickListener {
            lifecycleScope.launch {
                RawMapStore.clear()
                binding.map.clear()
                binding.map.centralize()
            }
        }
        binding.saveMap.setOnClickListener {
            lifecycleScope.launch {
                saveImageToGallery()
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.newMapPointsState.collect {
                    binding.map.setMapState(it)
                }
            }
        }
    }

    private suspend fun saveImageToGallery() {
        binding.saveMap.isEnabled = false
        withContext(Dispatchers.IO) {
            Log.i("HARDOCDE", "Map creating....")
            val bitmap = RawMapStore.getBitmap()
            Log.i("HARDOCDE", "Map bitmap created")
            saveBitmapToGallery(
                requireContext(), bitmap, UUID.randomUUID().toString()
            )
            Log.i("HARDOCDE", "Map image saved")
        }
        withContext(Dispatchers.Main) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
            }
            binding.saveMap.isEnabled = true
        }
    }
}
