package ru.ssau.arwalls.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
            RawMapStore.clear()
            binding.map.clear()
            binding.map.centralize()
        }
        binding.saveMap.setOnClickListener {
            lifecycleScope.launch {
                saveImageToGallery()
            }
        }
        lifecycleScope.launchWhenResumed {
            Log.d("HARDCODE", "launchWhenResumed")
            lifecycleScope.launch {
                viewModel.newMapPointsState.collect {
//                    Log.d("HARDCODE", "collect")
                    binding.map.setMapState(it)
                }
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            /* context = */ requireContext(),
            /* permission = */ Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            /* activity = */ requireActivity(),
            /* permissions = */ arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            /* requestCode = */ 1,
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            lifecycleScope.launch {
                saveImageToGallery()
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
