package com.a5starcompany.sprintchecksdk.fragment

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.a5starcompany.sprintchecksdk.KYCVerificationViewModel
import com.a5starcompany.sprintchecksdk.databinding.FragmentSuccessBinding
import com.a5starcompany.sprintchecksdk.util.KYCVerificationManager
import com.a5starcompany.sprintchecksdk.util.Logger
import java.util.Locale


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [Success.newInstance] factory method to
 * create an instance of this fragment.
 */
class Success : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: Boolean? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getBoolean(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    private lateinit var viewModel: KYCVerificationViewModel

    private var _binding: FragmentSuccessBinding? = null
    private val binding get() = _binding!!
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSuccessBinding.inflate(inflater, container, false)
        return binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[KYCVerificationViewModel::class.java]

        setupUI()
        observeScore()
        setupClickListeners()
        binding.timerText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.timerText.text = "Closing in " + millisUntilFinished / 1000 + "secs"
            }

            override fun onFinish() {
                // Navigate to home screen
                viewModel.completeVerification()
//                finish()
            }
        }.start()
    }

    private fun setupUI() {
        binding.rvgvivwlqoag.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        if (param1 == true) {
            binding.tvScoreStatus.text = "Your Score is Good"
            binding.tvScoreStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            binding.btnPrimary.text = "Go Home"
            binding.btnSecondary.visibility = View.GONE
            binding.tvCompletionMessage.text = "KYC Verification Completed"
            binding.tvDiscriptionMessage.text = "We have verified that the ID belongs to you (${viewModel.enrollmentdata}). Thanks for your cooperation."
            binding.tvCompletionMessage.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
        } else {
            binding.tvScoreStatus.text = "Your Score is BAD"
            binding.tvScoreStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
            binding.tvCompletionMessage.text =
                "KYC Verification Failed"
            binding.tvCompletionMessage.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            android.R.color.holo_red_dark
                        )
                        )
            if (param2?.isEmpty() != false) {
                binding.btnPrimary.text = "Retry Face Recognition"
                binding.btnSecondary.text = "Start Over"
                binding.btnSecondary.visibility = View.VISIBLE
                binding.tvDiscriptionMessage.text =
                    "Your face did not match the ${KYCVerificationManager.getInstance().transactiontype.toString().uppercase(Locale.ROOT)} provided"
            }else{
                binding.textView.visibility = View.VISIBLE
                binding.btnPrimary.text = "Start Over"
                binding.tvDiscriptionMessage.text = "Invalid ${KYCVerificationManager.getInstance().transactiontype.toString().uppercase(Locale.ROOT)} provided"
            }
        }
    }

    private fun observeScore() {
        viewModel.verificationScore.observe(viewLifecycleOwner) { score ->
            animateScore(score)
        }
    }

    private fun animateScore(targetScore: Int) {
        val animator = ValueAnimator.ofInt(0, targetScore)
        animator.duration = 2000
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val currentScore = animation.animatedValue as Int
            binding.tvScore.text = currentScore.toString()

            // Update progress indicator color based on score
            val progressColor = when {
                currentScore >= 70 -> ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                currentScore >= 40 -> ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                else -> ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
            }
            binding.scoreProgressIndicator.setColorFilter(progressColor)
        }

        animator.start()
    }

    private fun setupClickListeners() {
        binding.btnPrimary.setOnClickListener {
            if (param1 == true) {
                viewModel.completeVerification()
            } else {
                if (param2?.isEmpty() != false) {
                    viewModel.retryVerification()
                }else{
                    viewModel.restartVerification()
                }
            }
            countDownTimer?.cancel()
        }

        binding.btnSecondary.setOnClickListener {
            viewModel.restartVerification()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        countDownTimer?.cancel()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Success.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Boolean, param2: String) =
            Success().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}