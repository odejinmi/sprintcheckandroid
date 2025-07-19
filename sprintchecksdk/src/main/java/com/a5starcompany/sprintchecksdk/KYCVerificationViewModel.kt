package com.a5starcompany.sprintchecksdk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.util.Log
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
import java.io.InputStream
import java.net.URL
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

    private val _facetitle = MutableLiveData<String>()
    val facetitle: LiveData<String> = _facetitle

    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> = _isProcessing

    private var bvn: String = ""
    private var reference: String = ""
    private var confidencelevel: String = ""
    var enrollmentdata: String = ""

    fun startVerificationProcess(bvn: String) {
        if (KYCVerificationManager.getInstance().transactiontype == CheckoutMethod.facial){
            _facetitle.value = "Face Verification"
        }else{
            _facetitle.value = "Face Recognition"
        }
        this.bvn = bvn
        _currentStep.value = VerificationStep.LIVENESS_CHECK
        CoroutineScope(Dispatchers.IO).launch {
            val params = mapOf(
                "number" to bvn,
                "identifier" to KYCVerificationManager.getInstance().identifier
            )
            Logger().i("params", params.toString())
            val jsonObject =
                HttpJsonParser().makeRetrofitRequest(
                    KYCVerificationManager.getInstance().baseUrl + KYCVerificationManager.getInstance().transactiontype.toString(), "POST", params,
                    KYCVerificationManager.getInstance().config?.apiKey
                )
            Logger().i("jsonObject", jsonObject.toString())
            withContext(Dispatchers.Main) {
                _isProcessing.value = false
                if (jsonObject != null && jsonObject.getInt("success") == 1) {
                    val imageStr = jsonObject.getJSONObject("data").getString("image")
                    if (isUrl(imageStr)) {
                        // Switch to IO for network
                        CoroutineScope(Dispatchers.IO).launch {
                            val bitmap = urlToBitmap(imageStr)
                            withContext(Dispatchers.Main) {
                                _bvnimage.value = bitmap
                            }
                        }
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            val bitmap = base64ToBitmap(imageStr)
                            withContext(Dispatchers.Main) {
                                _bvnimage.value = bitmap
                            }
                        }
                    }
                    reference = jsonObject.getJSONObject("data").getString("reference")
                    confidencelevel = jsonObject.getString("confidence_level")
                } else {
                    _currentStep.value = VerificationStep.FACE_RECOGNITION_ERROR
                }
            }
        }
    }

    fun isUrl(str: String): Boolean {
        return str.startsWith("http://") || str.startsWith("https://")
    }

    // Make this a suspend function and always call on IO
    suspend fun urlToBitmap(imageUrl: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection()
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.getInputStream()
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    internal fun matchFaces(first: Bitmap, context:Context) {
        Logger().d("first", first.toString())
        val firstImage = MatchFacesImage(first, ImageType.LIVE, true)
        val secondImage = MatchFacesImage(bvnimage.value, ImageType.LIVE, true)
        Logger().d("firstImage", firstImage.toString())
        val matchFacesRequest = MatchFacesRequest(arrayListOf(firstImage, secondImage))

        val crop = OutputImageCrop(
            OutputImageCropAspectRatio.OUTPUT_IMAGE_CROP_ASPECT_RATIO_3X4
        )
        val outputImageParams = OutputImageParams(crop, Color.WHITE)
        matchFacesRequest.outputImageParams = outputImageParams
        Logger().d("matchFacesRequest", matchFacesRequest.toString())
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
        _isProcessing.postValue(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Logger().d("score", score.toString())
                _verificationScore.postValue(score)
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
                        _currentStep.value = VerificationStep.SCORE_FAILURE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isProcessing.value = false
                    _verificationResult.value = KYCResult.Failure(
                        errorCode = "PROCESSING_ERROR",
                        errorMessage = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
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

    suspend fun base64ToBitmapSuspend(base64String: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
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