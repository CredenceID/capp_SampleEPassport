package com.credenceid.sample.epassport.eco.mrzscanner.domains

import com.credenceid.sample.epassport.eco.mrzscanner.controllers.IcaoMrzScannerController
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.IcaoMrzResult
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.IcaoMrzResultCallback
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.ScannerOverlayImpl
import com.credenceid.sample.epassport.eco.mrzscanner.ui.ScanIcaoMrzFragment
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume

class ReadIcaoMrzUseCase: KoinComponent {
    private val icaoMrzScannerController = IcaoMrzScannerController()

    suspend fun scanDocument(owner: ScanIcaoMrzFragment,
                             olFragmentMrzScanner: ScannerOverlayImpl): IcaoMrzResult {
        return suspendCancellableCoroutine { continuation ->
            icaoMrzScannerController.startCamera(owner, olFragmentMrzScanner, object :
                IcaoMrzResultCallback {
                override fun onSuccess(result: IcaoMrzResult) {
                    continuation.resume(result)
                }
            })
        }
    }

}
