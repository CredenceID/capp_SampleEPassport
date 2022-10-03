package com.credenceid.sample.epassport.eco.mrzscanner.helpers

/**
 * Callback of the scan MRZ process
 */
interface IcaoMrzResultCallback {

    /**
     * Called when decoder has successfully decoded the MRZ Zone
     *
     * @param result Encapsulates the result of decoded MRZ image
     */
    fun onSuccess(result: IcaoMrzResult)
}
