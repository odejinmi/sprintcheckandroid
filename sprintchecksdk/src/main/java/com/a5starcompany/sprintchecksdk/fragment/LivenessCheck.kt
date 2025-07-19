package com.a5starcompany.sprintchecksdk.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.a5starcompany.sprintchecksdk.KYCVerificationViewModel
import com.a5starcompany.sprintchecksdk.databinding.FragmentLivenessCheckBinding
import com.a5starcompany.sprintchecksdk.util.KYCVerificationManager
import com.a5starcompany.sprintchecksdk.util.LoadingDialog
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.exception.InitException
import com.regula.facesdk.model.results.LivenessResponse
import java.util.Locale


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [LivenessCheck.newInstance] factory method to
 * create an instance of this fragment.
 */
class LivenessCheck : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var loadingDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    private var _binding: FragmentLivenessCheckBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: KYCVerificationViewModel
    private var captureimage: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLivenessCheckBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[KYCVerificationViewModel::class.java]
        FaceSDK.Instance().initialize(requireContext()) { status: Boolean, e: InitException? ->
            if (!status) {
                Toast.makeText(
                    requireContext(),
                    "Init finished with error: " + if (e != null) e.message else "",
                    Toast.LENGTH_LONG
                ).show()
                return@initialize
            }
            Log.d("MainActivity", "FaceSDK init completed successfully")
        }
        allPermissionsGranted()
        setupClickListeners()
        viewModel.facetitle.observe(viewLifecycleOwner) { title ->
            binding.title.text = title
        }
        showLoadingSafely("Validating ${KYCVerificationManager.getInstance().transactiontype.toString().uppercase(Locale.ROOT)}...", "Please wait while we verify your details")
        viewModel.bvnimage.observe(viewLifecycleOwner) { image ->
            if (captureimage != null){
                viewModel.matchFaces(captureimage!!, requireContext())
            }
        }
        viewModel.isProcessing.observe(viewLifecycleOwner) { image ->
            if (image){

            }else{
                hideLoading()
            }
        }
    }

    private fun setupClickListeners() {
        binding.rvytzup7ri4.setOnClickListener {
            FaceSDK.Instance().startLiveness(requireContext()) { livenessResponse: LivenessResponse ->
//                LivenessResponseUtil.response(context, livenessResponse)
                if (livenessResponse.exception != null) {
                    Toast.makeText(
                        context,
                        "Error: " + livenessResponse.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("TAG", "liveness response: ${livenessResponse.exception!!.message}")
                }else {
                    Toast.makeText(
                        context,
                        "Liveness status: " + livenessResponse.liveness.name,
                        Toast.LENGTH_SHORT
                    ).show()
                    captureimage = livenessResponse.bitmap
                    if (viewModel.bvnimage.value != null){
                        viewModel.matchFaces(captureimage!!, requireContext())
                    }
                }
            }
        }

        binding.rvgvivwlqoag.setOnClickListener {
//            requireActivity().onBackPressed()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (captureimage != null) {
            showLoadingSafely("Validating Face...", "Please wait while we verify your face")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        hideLoading()
    }

    private fun showLoadingSafely(text: String, subText: String) {
        view?.post {
            if (isAdded && !isStateSaved && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                try {
                    if (loadingDialog == null) {
                        loadingDialog = LoadingDialog.newInstance(text, subText)
                    }
                    loadingDialog?.show(parentFragmentManager, "loading")
                } catch (e: IllegalStateException) {
                    Log.e("TAG", "Could not show loading dialog: ${e.message}")
                    // Optionally show a different type of loading indicator
                }
            }
        }
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private lateinit var faceBitmaps: ArrayList<Bitmap>


    companion object {

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LivenessCheck.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LivenessCheck().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}