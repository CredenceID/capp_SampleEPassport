package com.credenceid.sample.epassport

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.credenceid.biometrics.Biometrics
import com.credenceid.biometrics.Biometrics.ResultCode.*
import com.credenceid.biometrics.BiometricsManager
import com.credenceid.biometrics.DeviceFamily
import com.credenceid.sample.epassport.modules.useCaseModule
import com.credenceid.sample.epassport.modules.viewModelModule
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class LaunchActivity : Activity() {


    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        startKoin {
            // declare used Android context
            androidContext(this@LaunchActivity)
            modules(viewModelModule)
            modules(useCaseModule)
        }

        // Initialize a new instance of ManagePermissions class
        checkPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            this.initBiometrics()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun initBiometrics() {

        /*  Create new biometrics object. */
        App.BioManager = BiometricsManager(this)

        /* Initialize object, meaning tell CredenceService to bind to this application. */
        App.BioManager!!.initializeBiometrics { rc: Biometrics.ResultCode,
                                                _: String,
                                                _: String ->

            when (rc) {
                OK -> {
                    Toast.makeText(this, getString(R.string.bio_init), LENGTH_SHORT).show()

                    App.DevFamily = App.BioManager!!.deviceFamily
                    App.DevType = App.BioManager!!.deviceType

                    val intent = when (App.DevFamily) {
                        DeviceFamily.CredenceTAB ->
                            Intent(this, com.credenceid.sample.epassport.ctab.MRTDActivity::class.java)
                        DeviceFamily.CredenceTwo ->
                            Intent(this, com.credenceid.sample.epassport.ctwo.MRZActivity::class.java)
                        DeviceFamily.CredenceThree ->
                            Intent(this, com.credenceid.sample.epassport.eco.mrtd.MRTDActivity::class.java)
                        DeviceFamily.CredenceECO ->
                            Intent(this, com.credenceid.sample.epassport.eco.mrtd.MRTDActivity::class.java)
                        else -> return@initializeBiometrics
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    this.finish()

                }
                INTERMEDIATE -> {
                    /* This code is never returned for this API. */
                }
                FAIL -> Toast.makeText(this, getString(R.string.bio_ini_fail), LENGTH_LONG).show()
            }
        }
    }


    // Check permissions at runtime
    fun checkPermissions() {
        if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@LaunchActivity, list.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            this.initBiometrics()
        }
    }

    val list = if (android.os.Build.VERSION.SDK_INT <= 30) {
        listOf(
            Manifest.permission.CAMERA,
        )
    } else {
        listOf(
            Manifest.permission.CAMERA,
        )
    }


    // Check permissions status
    private fun isPermissionsGranted(): Int {
        var counter = 0;
        for (permission in list) {
            counter += ContextCompat.checkSelfPermission(this, permission)
        }
        return counter
    }
}
