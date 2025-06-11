package com.a5starcompany.sprintchecksdk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.a5starcompany.sprintchecksdk.util.CheckoutMethod
import com.a5starcompany.sprintchecksdk.util.KYCResult
import com.a5starcompany.sprintchecksdk.util.KYCVerificationManager
import com.a5starcompany.sprintchecksdk.util.Logger
import com.nibsssdk.services.HttpJsonParser
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.detection.request.OutputImageCrop
import com.regula.facesdk.detection.request.OutputImageParams
import com.regula.facesdk.enums.ImageType
import com.regula.facesdk.enums.OutputImageCropAspectRatio
import com.regula.facesdk.model.MatchFacesImage
import com.regula.facesdk.model.results.matchfaces.MatchFacesResponse
import com.regula.facesdk.model.results.matchfaces.MatchFacesSimilarityThresholdSplit
import com.regula.facesdk.request.MatchFacesRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.toString

class KYCVerificationViewModel : ViewModel() {

    private val _currentStep = MutableLiveData<VerificationStep>()
    val currentStep: LiveData<VerificationStep> = _currentStep

    private val _verificationResult = MutableLiveData<KYCResult>()
    val verificationResult: LiveData<KYCResult> = _verificationResult

    private val _verificationScore = MutableLiveData<Int>()
    val verificationScore: LiveData<Int> = _verificationScore

    private val _bvnimage = MutableLiveData<Bitmap>()
    val bvnimage: LiveData<Bitmap> = _bvnimage

    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> = _isProcessing

    private var bvn: String = ""
    private var reference: String = ""
    private var confidencelevel: String = ""
    var enrollmentdata: String = ""

    fun startVerificationProcess(bvn: String) {
        this.bvn = bvn
        _currentStep.value = VerificationStep.LIVENESS_CHECK
        CoroutineScope(Dispatchers.IO).launch {
            val params = mapOf(
                "number" to bvn
            )
            val jsonObject =
                HttpJsonParser().makeRetrofitRequest(
                    KYCVerificationManager.getInstance().baseUrl + KYCVerificationManager.getInstance().transactiontype.toString(), "POST", params,
                    KYCVerificationManager.getInstance().config?.apiKey
                )
            Logger().i("jsonObject", jsonObject.toString())
            withContext(Dispatchers.Main) {
                _isProcessing.value = false
                if (jsonObject != null && jsonObject.getInt("success") == 1) {

                    _bvnimage.value =
                        base64ToBitmap(jsonObject.getJSONObject("data").getString("image"))
                    reference = jsonObject.getJSONObject("data").getString("reference")
                    confidencelevel = jsonObject.getString("confidence_level")
                } else {
                    _currentStep.value = VerificationStep.FACE_RECOGNITION_ERROR
                }
            }
        }
    }


    internal fun matchFaces(first: Bitmap, context:Context) {
        val firstImage = MatchFacesImage(first, ImageType.LIVE, true)
        val secondImage = MatchFacesImage(bvnimage.value, ImageType.LIVE, true)
        val matchFacesRequest = MatchFacesRequest(arrayListOf(firstImage, secondImage))

        val crop = OutputImageCrop(
            OutputImageCropAspectRatio.OUTPUT_IMAGE_CROP_ASPECT_RATIO_3X4
        )
        val outputImageParams = OutputImageParams(crop, Color.WHITE)
        matchFacesRequest.outputImageParams = outputImageParams

        FaceSDK.Instance().matchFaces(context, matchFacesRequest) { matchFacesResponse: MatchFacesResponse ->
            val split = MatchFacesSimilarityThresholdSplit(matchFacesResponse.results, 0.75)
            val similarity = if (split.matchedFaces.isNotEmpty()) {
                split.matchedFaces[0].similarity
            } else if (split.unmatchedFaces.isNotEmpty()){
                split.unmatchedFaces[0].similarity
            } else {
                null
            }

            processLivenessCheck((similarity?.times(100))?.toInt() ?: 0,first)
//            val text = similarity?.let {
//                "Similarity: " + String.format("%.2f", it * 100) + "%"
//            } ?: matchFacesResponse.exception?.let {
//                "Similarity: " + it.message
//            } ?: "Similarity: "

//            textViewSimilarity.text = text

//            faceBitmaps = arrayListOf()
//
//            for(matchFaces in matchFacesResponse.detections) {
//                for (face in matchFaces.faces)
//                    face.crop?.let {
//                        faceBitmaps.add(it) }
//            }
//
//            val l = faceBitmaps.size
//            if (l > 0) {
//                buttonSee.text = "Detections ($l)"
//                buttonSee.visibility = View.VISIBLE
//            } else {
//                buttonSee.visibility = View.GONE
//            }

//            buttonMatch.isEnabled = true
//            buttonClear.isEnabled = true
        }
    }

    fun processLivenessCheck(score: Int, bitmap: Bitmap) {
//        CoroutineScope(Dispatchers.IO).launch {
            _isProcessing.value = true

            try {
                // Simulate API call for liveness check
//                val score = performLivenessCheck(faceImageData)
                _verificationScore.value = score
                CoroutineScope(Dispatchers.IO).launch {
                    val params = mapOf(
                        "number" to bvn,
                        "reference" to reference,
                        "confidence" to "$score",
                        "identifier" to KYCVerificationManager.getInstance().identifier,
                        "image" to bitmapToBase64(bitmap)
                    )

                    val jsonObject =
                        HttpJsonParser().makeRetrofitRequest(
                            KYCVerificationManager.getInstance().baseUrl + KYCVerificationManager.getInstance().transactiontype.toString(), "PUT", params,
                            KYCVerificationManager.getInstance().config?.apiKey
                        )
                    withContext(Dispatchers.Main) {
                        _isProcessing.value = false
                        Logger().i("jsonObject", jsonObject.toString())
                        if (jsonObject != null && jsonObject.getInt("success") == 1) {

                            enrollmentdata = jsonObject.getString("data")
                            when {
                                score >= confidencelevel.toInt() -> _currentStep.value =
                                    VerificationStep.SCORE_SUCCESS

                                score == 0 -> _currentStep.value =
                                    VerificationStep.FACE_RECOGNITION_ERROR

                                else -> _currentStep.value = VerificationStep.SCORE_FAILURE
                            }
                        } else {
                            VerificationStep.SCORE_FAILURE
                        }
                    }

            }
            } catch (e: Exception) {
//                _isProcessing.value = false
                _verificationResult.value = KYCResult.Failure(
                    errorCode = "PROCESSING_ERROR",
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            } finally {
//                _isProcessing.value = false
            }
//        }
    }

    fun retryVerification() {
        _currentStep.value = VerificationStep.LIVENESS_CHECK
    }

    fun restartVerification() {
        _currentStep.value = VerificationStep.DETAILS
    }

    fun completeVerification() {
        val score = _verificationScore.value ?: 0
        _verificationResult.value = KYCResult.Success(
            reference = reference,
            confidenceLevel = score,
            message = "Verification Completed",
            status = score > 0,
            method = KYCVerificationManager.getInstance().transactiontype,
            name = enrollmentdata,
            verify = true,
            bvn = if(KYCVerificationManager.getInstance().transactiontype == CheckoutMethod.bvn)  this.bvn else "",
            nin = if(KYCVerificationManager.getInstance().transactiontype == CheckoutMethod.nin)  this.bvn else ""
        )
    }

    fun cancelVerification() {
        _verificationResult.value = KYCResult.Cancelled
    }


    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}


// VerificationStep.kt
enum class VerificationStep {
    DETAILS,
    LIVENESS_CHECK,
    SCORE_SUCCESS,
    SCORE_FAILURE,
    FACE_RECOGNITION_ERROR
}