package com.a5starcompany.sprintchecksdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.a5starcompany.sprintchecksdk.fragment.LivenessCheck
import com.a5starcompany.sprintchecksdk.fragment.Success
import com.a5starcompany.sprintchecksdk.fragment.Verificationdetail
import com.a5starcompany.sprintchecksdk.util.KYCResult

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: KYCVerificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, Verificationdetail.newInstance("",""))
//                .commitNow()
//        }

        viewModel = ViewModelProvider(this)[KYCVerificationViewModel::class.java]

        setupObservers()

        // Start with verification details screen
        showVerificationDetailsFragment()
    }

    private fun setupObservers() {
        viewModel.verificationResult.observe(this) { result ->
            when (result) {
                is KYCResult.Success -> {
                    setResult(RESULT_OK, Intent().apply {
                        putExtra("reference", result.reference)
                        putExtra("message", result.message)
                        putExtra("name", result.name)
                        putExtra("bvn", result.bvn)
                        putExtra("nin", result.nin)
                        putExtra("confidenceLevel", result.confidenceLevel)
                        putExtra("status", result.status)
                        putExtra("verify", result.verify)
                        putExtra("method", result.method)
                    })
                    finish()
                }
                is KYCResult.Failure -> {
                    setResult(RESULT_CANCELED, Intent().apply {
                        putExtra("error_code", result.errorCode)
                        putExtra("error_message", result.errorMessage)
                    })
                    finish()
                }
                is KYCResult.Cancelled -> {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }

        viewModel.currentStep.observe(this) { step ->
            when (step) {
                VerificationStep.DETAILS -> showVerificationDetailsFragment()
                VerificationStep.LIVENESS_CHECK -> showLivenessCheckFragment()
                VerificationStep.SCORE_SUCCESS -> showScoreFragment(true)
                VerificationStep.SCORE_FAILURE -> showScoreFragment(false)
                VerificationStep.FACE_RECOGNITION_ERROR -> showFaceRecognitionErrorFragment()
            }
        }
    }

    private fun showVerificationDetailsFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, Verificationdetail())
            .commit()
    }

    private fun showLivenessCheckFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, LivenessCheck())
            .addToBackStack(null)
            .commit()
    }

    private fun showScoreFragment(isSuccess: Boolean) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, Success.newInstance(isSuccess,""))
            .commit()
    }

    private fun showFaceRecognitionErrorFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, Success.newInstance(false,"Invalid BVN provided"))
            .commit()
    }
}


