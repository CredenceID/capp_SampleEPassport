package com.credenceid.sample.epassport.eco.mrzscanner.controllers

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.credenceid.sample.epassport.App.Companion.TAG
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.IcaoMrzAnalyser
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.IcaoMrzResultCallback
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.ImageAnalyserOutput
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.ScannerOverlayImpl
import com.credenceid.sample.epassport.eco.mrzscanner.ui.ScanIcaoMrzFragment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.Executors

class IcaoMrzScannerController(): KoinComponent {

    private val context: Context by inject()
    private val analyserExecutor = Executors.newSingleThreadExecutor()
    private lateinit var analyser : IcaoMrzAnalyser

    private fun setUp(owner: ScanIcaoMrzFragment,
                      olFragmentMrzScanner: ScannerOverlayImpl,
                      listener: IcaoMrzResultCallback
    ) {

        analyser = IcaoMrzAnalyser(olFragmentMrzScanner)
        analyser.liveData().observe(owner) { result ->
            when(result){
                is ImageAnalyserOutput.Success -> {
                    Timber.tag(TAG).d("Result = %s", result.result)
                    analyser.close()
                    listener.onSuccess(result.result)
                }
                is ImageAnalyserOutput.Error -> {
                    Timber.tag(TAG).e(result.exception.toString())
                }
                is ImageAnalyserOutput.ImagePreview -> {
                    // Keep as it might be used in final UI
                    Timber.tag(TAG).d("Preview available")
                }
            }

        }
    }

    fun startCamera(owner: ScanIcaoMrzFragment,
                    icaoMrzScannerOverlay: ScannerOverlayImpl,
                    listener: IcaoMrzResultCallback) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        setUp(owner, icaoMrzScannerOverlay, listener)

        cameraProviderFuture.addListener({

            owner.lifecycle.addObserver(analyser)
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(owner.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(analyserExecutor, analyser)
                }

            // Select back camera
            val cameraSelector = CameraSelector
                .Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(owner, cameraSelector, preview, imageAnalyzer)

            } catch (exc: Exception) {
                Timber.e(exc,"Use case binding failed")
            }

        }, ContextCompat.getMainExecutor(context))
    }
}
