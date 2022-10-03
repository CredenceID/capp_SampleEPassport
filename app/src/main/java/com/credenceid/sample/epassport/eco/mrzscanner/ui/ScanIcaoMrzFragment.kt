package com.credenceid.sample.epassport.eco.mrzscanner.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.Preview
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.credenceid.sample.epassport.App.Companion.TAG
import com.credenceid.sample.epassport.R
import com.credenceid.sample.epassport.databinding.FragmentScanIcaoMrzBinding
import com.credenceid.sample.epassport.eco.mrzscanner.helpers.ScannerOverlayImpl
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalQueries.localDate


class ScanIcaoMrzFragment : Fragment() {

    companion object {
        fun newInstance() = ScanIcaoMrzFragment()
    }

    private var _binding: FragmentScanIcaoMrzBinding? = null
    private val viewModelReadIcaoMrzViewModel: ScanIcaoMrzViewModel by viewModel()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val surfaceScannerOverlayImpl: ScannerOverlayImpl
        get() = binding.olFragmentMrzScanner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentScanIcaoMrzBinding.inflate(inflater, container, false)

        viewModelReadIcaoMrzViewModel.scanDocument(this, surfaceScannerOverlayImpl)

        viewModelReadIcaoMrzViewModel.dataRead.observe(viewLifecycleOwner) { result ->
            // Use the Kotlin extension in the fragment-ktx artifact
           Timber.tag(TAG).d("Fragment Result = %s", result)
            val intent = Intent()
            intent.putExtra(getString(R.string.birth_date_key), mrtdDateFormat(result.birthDate))
            intent.putExtra(getString(R.string.expiration_date_key), mrtdDateFormat(result.expirationDate))
            intent.putExtra(getString(R.string.doc_number_key), result.idNumber)
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish() //finishing activity

        }
        return binding.root
    }

    fun mrtdDateFormat(date: LocalDate):String{
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyMMdd")
        return date.format(formatter)
    }

    val surfaceProvider: Preview.SurfaceProvider
        get() = binding.viewFinder.surfaceProvider
}
