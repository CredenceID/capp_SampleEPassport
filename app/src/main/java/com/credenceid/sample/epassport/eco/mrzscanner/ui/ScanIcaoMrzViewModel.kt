package com.credenceid.sample.epassport.eco.mrzscanner.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.credenceid.sample.epassport.eco.mrzscanner.domains.ReadIcaoMrzUseCase
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.IcaoMrzResult
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.ScannerOverlayImpl
import com.credenceid.sample.epassport.eco.mrzscanner.ui.ScanIcaoMrzFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ScanIcaoMrzViewModel: ViewModel(), KoinComponent {
    val dataRead = MutableLiveData<IcaoMrzResult>()
    private val readIcaoMrzUseCase: ReadIcaoMrzUseCase by inject()

    fun scanDocument(owner: ScanIcaoMrzFragment, olFragmentMrzScanner: ScannerOverlayImpl): Job {
        return viewModelScope.launch {
            dataRead.postValue(readIcaoMrzUseCase.scanDocument(owner, olFragmentMrzScanner))
        }
    }
}
