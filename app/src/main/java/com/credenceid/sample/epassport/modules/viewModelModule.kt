package com.credenceid.sample.epassport.modules

import com.credenceid.sample.epassport.eco.mrzscanner.ui.ScanIcaoMrzViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule by lazy {
    module {
        viewModel { ScanIcaoMrzViewModel() }
    }
}
