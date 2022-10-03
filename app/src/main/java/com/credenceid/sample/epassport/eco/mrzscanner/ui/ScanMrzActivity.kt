package com.credenceid.sample.epassport.eco.mrzscanner.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.credenceid.sample.epassport.R

class ScanMrzActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_mrz)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ScanIcaoMrzFragment.newInstance())
                .commitNow()
        }
    }
}