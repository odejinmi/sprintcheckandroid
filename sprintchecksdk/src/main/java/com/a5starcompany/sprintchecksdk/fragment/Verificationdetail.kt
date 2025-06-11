package com.a5starcompany.sprintchecksdk.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.a5starcompany.sprintchecksdk.KYCVerificationViewModel
import com.a5starcompany.sprintchecksdk.databinding.FragmentVerificationdetailBinding
import com.a5starcompany.sprintchecksdk.util.KYCVerificationManager
import java.util.Locale


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [Verificationdetail.newInstance] factory method to
 * create an instance of this fragment.
 */
class Verificationdetail : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var viewModel: KYCVerificationViewModel
    private var _binding: FragmentVerificationdetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerificationdetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[KYCVerificationViewModel::class.java]
        binding.rxchz65u9lu.text = run {
            KYCVerificationManager.getInstance().transactiontype.toString().uppercase(Locale.ROOT)
        }
//        binding.rgteuu0nyanh.setText("22314756491")
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.rz7zlryzx1c.setOnClickListener {
            viewModel.cancelVerification()
        }
        binding.rvytzup7ri4.setOnClickListener {
            val bvn = binding.rgteuu0nyanh.text.toString().trim()
            if (validateBVN(bvn) && binding.r5r8dr82u45c.isChecked) {
                viewModel.startVerificationProcess(bvn)
            } else {
                if (!validateBVN(bvn)) {
                        Toast.makeText(
                            context,
                            "Error: Invalid Bvn",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                    Toast.makeText(
                        context,
                        "Error: check Box before proceeding",
                        Toast.LENGTH_SHORT
                    ).show()
                }
//                binding.tilBvn.error = "Please enter a valid BVN"
            }
        }
    }

    private fun validateBVN(bvn: String): Boolean {
        return bvn.length == 11 && bvn.all { it.isDigit() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Verificationdetail.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Verificationdetail().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}