package com.credenceid.sample.epassport

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Configuration

import com.credenceid.biometrics.BiometricsManager
import com.credenceid.biometrics.DeviceFamily
import com.credenceid.biometrics.DeviceType

@SuppressLint("StaticFieldLeak")
class App : Application() {
    companion object {
        val TAG = "MIKA"
        /**
         * CredenceSDK biometrics object used to interface with APIs.
         */
        var BioManager: BiometricsManager? = null
        /**
         * Stores which Credence family of device's this app is running on.
         */
        var DevFamily = DeviceFamily.InvalidDevice
        /**
         * Stores which specific device this app is running on.
         */
        var DevType = DeviceType.InvalidDevice

        fun Context.isPortrait() : Boolean {
            val orientation = this.resources.configuration.orientation
            return orientation == Configuration.ORIENTATION_PORTRAIT
        }
    }



}
