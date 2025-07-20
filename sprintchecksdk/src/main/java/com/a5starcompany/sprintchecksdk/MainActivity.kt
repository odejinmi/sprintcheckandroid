package com.a5starcompany.sprintchecksdk

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.a5starcompany.sprintchecksdk.fragment.LivenessCheck
import com.a5starcompany.sprintchecksdk.fragment.Success
import com.a5starcompany.sprintchecksdk.fragment.Verificationdetail
import com.a5starcompany.sprintchecksdk.util.CheckoutMethod
import com.a5starcompany.sprintchecksdk.util.KYCResult
import com.a5starcompany.sprintchecksdk.util.KYCVerificationManager
import java.util.Locale

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

        viewModel = ViewModelProvider(this)[KYCVerificationViewModel::class.java]

        setupObservers()

        if(KYCVerificationManager.getInstance().transactiontype == CheckoutMethod.facial){
            viewModel.startVerificationProcess("00000000000")
        }else {
            // Start with verification details screen
            showVerificationDetailsFragment()
        }

    }

    private fun setupObservers() {
        viewModel.verificationResult.observe(this) { result ->
            // Report result through manager and finish
            KYCVerificationManager.getInstance().reportResult(result)
            finish()
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
            .commit()
    }

    private fun showScoreFragment(isSuccess: Boolean) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, Success.newInstance(isSuccess, ""))
            .commit()
    }

    private fun showFaceRecognitionErrorFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, Success.newInstance(false, "Invalid ${KYCVerificationManager.getInstance().transactiontype.toString().uppercase(Locale.ROOT)} provided"))
            .commit()
    }

    override fun onBackPressed() {
        // Handle back press as cancellation
        KYCVerificationManager.getInstance().reportResult(KYCResult.Cancelled)
        super.onBackPressed()
    }
}


