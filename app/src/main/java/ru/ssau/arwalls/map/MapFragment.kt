package ru.ssau.arwalls.map

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.ssau.arwalls.data.RawMapStore
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (hasPermissions()) {
                        Log.i("HARDCODE", "has permissions")
//                    saveImageToGallery()
                    } else {
                        Log.i("HARDCODE", "has no permissions")
//                    requestPermissions()
                    }
                    saveImageToGallery()
                } else {
                    saveImageToGallery()
                }
            }
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

    private fun saveImageToGallery() {
        val bitmap = RawMapStore.getBitmap()

        val filename = "${UUID.randomUUID()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val contentResolver = requireActivity().contentResolver
        val uri = requireActivity().contentResolver.insert(
            /* url = */ MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            /* values = */ contentValues,
        )
        uri?.let {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            } ?: Log.w(javaClass.simpleName, "outputStream не получен")
        } ?: Log.w(javaClass.simpleName, "uri не получен")
    }
}
