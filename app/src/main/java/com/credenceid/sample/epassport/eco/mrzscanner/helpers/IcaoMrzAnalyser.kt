package com.credenceid.sample.epassport.eco.mrzscanner.helpers

import android.graphics.Bitmap
import com.credenceid.sample.epassport.App.Companion.TAG
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.MrzTextPreProcessor.MIN_POSSIBLE_CHAR_LENGTH_PER_LINE
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.innovatrics.mrz.MrzParser
import com.innovatrics.mrz.types.MrzDate
import com.innovatrics.mrz.types.MrzSex
import timber.log.Timber
import java.time.LocalDate
import java.util.*

class IcaoMrzAnalyser(scannerOverlay: ScannerOverlay)
    : ImageAnalyser<ImageAnalyserOutput>(scannerOverlay){

    private val gmsTextRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private var addressText : String? = null

    override fun onBitmapPrepared(bitmap: Bitmap) {
        val textBlocks = detectTextBlocks(bitmap)

        textBlocks.forEach { block ->

            val detectedText = block.text.replace("<K<", "<<<")
                .replace("<c<", "<<<")
                .replace(" ", "")

            if(isPossibleMrzBlock(block.text)) {
                MrzTextPreProcessor.process(detectedText)?.let { processed ->
                    try {
                        val parsed = parseMrz(processed)
                        if(parsed) {
                            Timber.tag(TAG).d("IcaoMrzAnalyser - Text block content = %s", detectedText)
                            return@forEach
                        }
                    } catch (e : Exception) {
                        Timber.tag(TAG).e(e, "Parsing MRZ failed")
                    }
                }
            }
        }
    }

    private fun detectTextBlocks(bitmap: Bitmap) : List<BlockWrapper> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val result = Tasks.await(gmsTextRecognizer.process(inputImage))
        if(result.text.isNotBlank()) {
            Timber.d("GMS scanned raw text: ${result.text}")
        }
        return result.textBlocks.map { BlockWrapper(gmsTextBLock = it) }
    }

    private fun isPossibleMrzBlock(txt : String) : Boolean {
        return (txt.filter { c -> c == '\n' }.isNotEmpty()
                && txt.filter { c -> c == '<' }.count() > 10
                && txt.split("\n").filter { it.length >= MIN_POSSIBLE_CHAR_LENGTH_PER_LINE }.size > 1)
    }

    private fun parseMrz(processed : String) : Boolean {
        val record = MrzParser.parse(processed)

        val documentNumber = record.documentNumber
        val givenNames = record.givenNames
        val sureName = record.surname
        val birthDate = record.dateOfBirth
        val nationality = record.nationality
        val gender = record.sex
        val issuingCountry = record.issuingCountry
        val expirationDate = record.expirationDate

        val nameNeedCorrection = processed.last().isLetter()
                || givenNames.contains(PARSER_FILLER_REPLACEMENT)
                || sureName.contains(PARSER_FILLER_REPLACEMENT)

        if(record.validDocumentNumber && !givenNames.isNullOrBlank()
            && !sureName.isNullOrBlank()
            && birthDate?.isDateValid == true
            && expirationDate.isDateValid
            && !nationality.isNullOrBlank()) {

            val idResult = IcaoMrzResult(idNumber = documentNumber,
                issuingCountry = issuingCountry,
                givenNames = givenNames.replace(PARSER_FILLER_REPLACEMENT, ""),
                sureName = sureName.replace(PARSER_FILLER_REPLACEMENT, ""),
                birthDate = addCenturyToBirthDate(birthDate),
                expirationDate = addCenturyToExpirationDate(expirationDate),
                nationality = nationality,
                gender = if(gender != null && gender != MrzSex.Unspecified) gender.name.uppercase(
                    Locale.ENGLISH
                ) else null,
                nameNeedCorrection = nameNeedCorrection, scannedAddress = addressText)

            postResult(idResult)
            return true
        }
        return false
    }

    private fun addCenturyToBirthDate(date : MrzDate) : LocalDate {
        //first try with 1900
        val twentiethCenturyDate = LocalDate.of(1900 + date.year, date.month, date.day)
        return if(LocalDate.now().year - twentiethCenturyDate.year > 100) {
            LocalDate.of(2000 + date.year, date.month, date.day)
        } else twentiethCenturyDate
    }

    private fun addCenturyToExpirationDate(date : MrzDate) : LocalDate {
        //first try with 2000
        val twentiethCenturyDate = LocalDate.of(2000 + date.year, date.month, date.day)
        return if(twentiethCenturyDate.year - LocalDate.now().year > 100) {
            LocalDate.of(1900 + date.year, date.month, date.day)
        } else twentiethCenturyDate
    }

    override fun close() {
        gmsTextRecognizer.close()
    }

    companion object {
        private const val PARSER_FILLER_REPLACEMENT = ", " //mrz parser replaces all "<<" with ", " within recognized name fields
    }
}
