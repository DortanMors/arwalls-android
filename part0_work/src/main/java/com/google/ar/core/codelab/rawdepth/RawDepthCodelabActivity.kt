/*
 * Copyright 2021 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.core.codelab.rawdepth

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.InstallStatus
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.codelab.common.helpers.CameraPermissionHelper
import com.google.ar.core.codelab.common.helpers.DisplayRotationHelper
import com.google.ar.core.codelab.common.helpers.FullScreenHelper
import com.google.ar.core.codelab.common.helpers.SnackBarUseCase
import com.google.ar.core.codelab.common.helpers.SnackBarUseCase.showSnackBar
import com.google.ar.core.codelab.domain.OpenGLRendererUseCase
import com.google.ar.core.codelab.rawdepth.databinding.ActivityMainBinding
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.coroutines.launch

/**
 * This is a simple example that shows how to create an augmented reality (AR) application using the
 * ARCore Raw Depth API. The application will show 3D point-cloud data of the environment.
 */
class RawDepthCodelabActivity : AppCompatActivity() {
    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: Session
    private var installRequested = false
    private val snackBarUseCase = SnackBarUseCase
    private lateinit var displayRotationHelper: DisplayRotationHelper
    private lateinit var openGLRendererUseCase: OpenGLRendererUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        displayRotationHelper = DisplayRotationHelper( /*context=*/this)
        openGLRendererUseCase = OpenGLRendererUseCase(
            context = this@RawDepthCodelabActivity,
            displayRotationHelper = displayRotationHelper,
        )

        // Set up renderer.
        binding.surfaceView.run {
            preserveEGLContextOnPause = true
            setEGLContextClientVersion(2)
            setEGLConfigChooser(8, 8, 8, 8, 16, 0) // Alpha used for plane blending.
            setRenderer(openGLRendererUseCase)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            setWillNotDraw(false)
        }
        installRequested = false

        lifecycleScope.launchWhenResumed {
            lifecycleScope.launch {
                snackBarUseCase.snackBarFlow.collect {
                    showSnackBar(it)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::session.isInitialized) {
            var exception: Exception? = null
            var message: String? = null
            try {
                when (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    InstallStatus.INSTALL_REQUESTED -> {
                        installRequested = true
                        return
                    }
                    InstallStatus.INSTALLED -> {}
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this)
                    return
                }

                // Creates the ARCore session.
                session = Session( /* context = */this).also { newSession ->
                    if (!newSession.isDepthModeSupported(Config.DepthMode.RAW_DEPTH_ONLY)) {
                        message = "This device does not support the ARCore Raw Depth API. See https://developers.google.com/ar/devices for a list of devices that do."
                    }
                }
            } catch (e: UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableUserDeclinedInstallationException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableApkTooOldException) {
                message = "Please update ARCore"
                exception = e
            } catch (e: UnavailableSdkTooOldException) {
                message = "Please update this app"
                exception = e
            } catch (e: UnavailableDeviceNotCompatibleException) {
                message = "This device does not support AR"
                exception = e
            } catch (e: Exception) {
                message = "Failed to create AR session"
                exception = e
            }
            message?.let { errorMessage ->
                snackBarUseCase.showError(errorMessage)
                Log.e(TAG, "Exception creating session", exception)
                return
            }
        }
        try {
            session.run {
                configure(
                    config.apply {
                        depthMode = Config.DepthMode.RAW_DEPTH_ONLY
                        focusMode = Config.FocusMode.AUTO
                        updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    }
                )
                resume()
            }
        } catch (e: CameraNotAvailableException) {
            snackBarUseCase.showError("Camera not available. Try restarting the app.")
            return
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        binding.surfaceView.onResume()
        displayRotationHelper.onResume()
        snackBarUseCase.showMessage("Waiting for depth data...")
    }

    public override fun onPause() {
        super.onPause()
        // If Session is paused before GLSurfaceView, GLSurfaceView may
        // still call session.update() and get a SessionPausedException.
        displayRotationHelper.onPause()
        binding.surfaceView.onPause()
        session.pause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(
                this, "Camera permission is needed to run this application",
                Toast.LENGTH_LONG
            ).show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
    }

    companion object {
        private val TAG = RawDepthCodelabActivity::class.java.simpleName
    }
}
