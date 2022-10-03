package com.credenceid.sample.epassport.eco.mrzscanner.helpers

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate

@Parcelize
data class IcaoMrzResult(
        val idNumber : String,
        val issuingCountry : String,
        val givenNames : String,
        val sureName : String,
        val birthDate : LocalDate,
        val expirationDate : LocalDate,
        val nationality : String,
        val gender : String?,
        val nameNeedCorrection : Boolean,
        val scannedAddress : String?) : Parcelable
