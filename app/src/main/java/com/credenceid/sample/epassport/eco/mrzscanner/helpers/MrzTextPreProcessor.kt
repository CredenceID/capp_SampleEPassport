package com.credenceid.sample.epassport.eco.mrzscanner.helpers

import com.credenceid.sample.epassport.eco.mrzscanner.helpers.MrzTextPreProcessor.MIN_SIZE_THRESHOLD
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.MrzTextPreProcessor.replaceWithinRange
import com.innovatrics.mrz.types.MrzFormat
import timber.log.Timber
import java.util.*

private val regexK = Regex("<K+<")
private val regexS = Regex("<S+<")
private val regexE = Regex("<E+<")
private val regexC = Regex("<C+<")
private val nonValidMrzChar = Regex("[^\n<\\dA-Z]")

private fun String.correctWrongFiller(vararg regexs : Regex) : String {
    var processed = this
    regexs.forEach { r ->
        while(processed.contains(r)) {
            processed = processed.replace(r) { match ->
                "<".repeat(match.value.length)
            }
        }
    }
    return processed
}

private fun List<String>.fillToMinimumSize(minSize : Int) : List<String>{
    return map { inputString ->
        if(inputString.length in minSize - MIN_SIZE_THRESHOLD until minSize) {
            inputString + "<".repeat(minSize - inputString.length)
        } else {
            inputString
        }
    }
}


private fun String.correctLines() : String {
    fun List<String>.fillToMinimumSizeAndJoinTogether(minSize : Int) : String {
        return fillToMinimumSize(minSize).joinToString(separator = "\n")
    }

    try {
        val lines = ArrayList(split("\n"))
        return when(MrzFormat.get(this)) {
            MrzFormat.MRTD_TD1 -> {
                lines[1] = lines[1].replaceWithinRange(0, 6, "O","0")
                lines.fillToMinimumSizeAndJoinTogether(30)
            }
            MrzFormat.FRENCH_ID -> {
                lines[1] = lines[1].replaceWithinRange(27, 33, "O","0")
                lines.fillToMinimumSizeAndJoinTogether(36)
            }
            MrzFormat.MRV_VISA_B -> {
                lines[1] = lines[1].replaceWithinRange(13, 19, "O","0")
                lines.fillToMinimumSizeAndJoinTogether(36)
            }
            MrzFormat.MRTD_TD2 -> {
                lines[1] = lines[1].replaceWithinRange(13, 19, "O","0")
                lines.fillToMinimumSizeAndJoinTogether(36)
            }
            MrzFormat.MRV_VISA_A -> {
                lines[1] = lines[1].replaceWithinRange(13, 19, "O","0")
                lines.fillToMinimumSizeAndJoinTogether(44)
            }
            MrzFormat.PASSPORT -> {
                lines[1] = lines[1].replaceWithinRange(13, 19, "O","0")
                lines.fillToMinimumSizeAndJoinTogether(44)
            }
            MrzFormat.SLOVAK_ID_234 -> {
                lines[1] = lines[1].replaceWithinRange(13, 19, "O","0")
                lines.fillToMinimumSizeAndJoinTogether(34)
            }
            else -> {
                this
            }
        }
    } catch (e : Exception) {
        Timber.w(e, "Failed to correct field values from $this")
        return this
    }
}

object MrzTextPreProcessor {

    fun process(raw: String): String? {
        val lineBreaks = raw.filter { c -> c == '\n' }.count()
        if (lineBreaks < 1)
            return null

        val preProcessed = raw.replace(" ", "")
            .toUpperCase(Locale.ENGLISH)
            .replace(nonValidMrzChar, "<")
            .split("\n")
            .filter { it.length in MIN_POSSIBLE_CHAR_LENGTH_PER_LINE..MAX_POSSIBLE_CHAR_LENGTH_PER_LINE }
            .joinToString(separator = "\n")
            .correctWrongFiller(
                regexS,
                regexK,
                regexE,
                regexC
            )
            .correctLines()

        return if(checkIfQualifiedForMrzParsing(preProcessed)) {
            preProcessed
        } else {
            null
        }
    }

    private fun checkIfQualifiedForMrzParsing(text: String): Boolean {
        val lineBreaks = text.filter { c -> c == '\n' }.count()

        fun stringsSameLength(text: String): Boolean {
            val list = text.split("\n")
            val firstLength = list.first().length
            return list.all { it.length == firstLength }
        }

        return lineBreaks in 1..2 && stringsSameLength(text)
    }

    fun String.replaceWithinRange(fromIndex: Int, toIndex : Int, toFind : String, replaceWith : String) : String{
        return substring(0, fromIndex) + substring(fromIndex, toIndex).replace(toFind, replaceWith) + substring(toIndex)
    }

    fun String.replaceWithinRange(fromIndex: Int, toFind : String, replaceWith : String) : String{
        return replaceWithinRange(fromIndex, length, toFind, replaceWith)
    }


    const val MIN_SIZE_THRESHOLD = 4
    private const val MIN_CHAR_LENGTH_PER_LINE = 30
    const val MIN_POSSIBLE_CHAR_LENGTH_PER_LINE = MIN_CHAR_LENGTH_PER_LINE - MIN_SIZE_THRESHOLD
    private const val MAX_POSSIBLE_CHAR_LENGTH_PER_LINE = 44
}
